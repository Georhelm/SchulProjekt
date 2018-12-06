import { Constants } from "./Constants";
import { DatabaseConnection } from "./DatabaseConnector";
import { Npc } from "./Npc";
import { HitPoint, Player} from "./Player";
import {User} from "./User";

export class Game {

//#region public static methods

    /**
     * ends all games containg a specific user
     * @param user the user to search for
     */
    public static endGamesContainingUser(user: User) {
        if (Game.AllGames === undefined) {
            return;
        }
        for (const game of Game.AllGames) {
            if (game.user1 === user) {
                game.setPlayerLeft(user.getDatabaseId());
            } else if (game.user2 === user) {
                game.setPlayerLeft(user.getDatabaseId());
            }
        }
    }

    /**
     * checks if a user is in a game
     * @param user the user to search for
     * @returns if the user is in a game
     */
    public static isUserInGame(user: User): boolean {
        if (Game.AllGames !== undefined) {
            for (const game of Game.AllGames) {
                if (game.user1 === user || game.user2 === user) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * gets the amount of running games
     * @returns the amount of running games
     */
    public static getGameCount(): number {
        if (Game.AllGames === undefined) {
            return 0;
        }
        return Game.AllGames.length;
    }

//#endregion public static methods

//#region static properties

private static AllGames: Game[];

//#endregion static properties

//#region private static methods

    /**
     * removes a game from the list of running games
     * @param game the game to remove
     */
    private static removeGame(game: Game) {
        const index = Game.AllGames.indexOf(game);
        if (index >= 0) {
            Game.AllGames.splice(index, 1);
        }
    }

//#endregion private static methods

//#region properties

    private id: number;
    private user1: User;
    private user2: User | null;
    private player1: Player;
    private player2: Player;
    private gameWidth: number;
    private timeOfLastUpdate: number;
    private running: boolean;
    private wonPlayerId: number;
    private draw: boolean;

//#endregion properties

//#region constructor

    /**
     * creates a new game and saves it to the list of running games
     * @param id the database id of the game
     * @param player1 the first user
     * @param player2 the second user of null if its an npc
     */
    constructor(id: number, player1: User, player2: User | null) {
        this.id = id;
        this.user1 = player1;
        this.user2 = player2;
        this.gameWidth = Math.round(Math.random() * (Constants.MAXGAMEWIDTH - Constants.MINGAMEWIDTH) + Constants.MINGAMEWIDTH);
        this.draw = false;

        if (Game.AllGames === undefined) {
            Game.AllGames = [];
        }

        Game.AllGames.push(this);
    }

//#endregion constructor

//#region public async methods

    /**
     * initializes the game and creates the player/npc objects
     * sends the players a full gameupdate
     */
    public async init() {
        this.running = true;
        this.player1 = await this.user1.startGame(0, this.playerReady.bind(this), false);
        if (this.user2 === null) {
            this.player2 = new Npc(this.gameWidth, true);
            await (this.player2 as Npc).init();
        } else {
            this.player2 = await this.user2.startGame(this.gameWidth, this.playerReady.bind(this), true);
        }

        this.player1.sendFullGameState(this.player2, this.gameWidth);
        this.player2.sendFullGameState(this.player1, this.gameWidth);
    }

//#endregion public async methods

//#region debug

    /**
     * gets data about the game in a readable format
     */
    public getLogObj(): any {
        const logObj = {
            id: this.id,
            player1: this.player1.getLogObj(),
            player2: this.player2.getLogObj(),
            width: this.gameWidth,
        };
        return logObj;
    }

//#endregion debug

//#region private methods

    /**
     * checks if both players are ready
     * if they are starts the game countdown
     */
    private playerReady() {
        if (this.player1.isReady() && this.player2.isReady()) {
            this.startGameCountdown();
        }
    }

    /**
     * main gameloop
     * updates players and sends those to them
     * checks for the overlapping postion and ends the round if they are
     */
    private updateGame() {
        const newTime = Date.now();
        const timeDelta = (newTime - this.timeOfLastUpdate) / 1000;
        this.timeOfLastUpdate = newTime;
        if (this.player1.getPosition() >= this.player2.getPosition()) {
            this.endRound();
            return;
        }

        this.player1.update(timeDelta);
        this.player2.update(timeDelta);

        this.player1.sendPartialGameUpdate(this.player2, this.gameWidth);
        this.player2.sendPartialGameUpdate(this.player1, this.gameWidth);

        setTimeout(() => {
            this.updateGame();
        }, Math.round(1000 / Constants.TICKRATE));
    }

    /**
     * ends the round
     * calculates damgage dealt by each player
     * sends endRound updates to them
     * checks if one (or both) reach 0 hitpoints then ends game
     */
    private endRound() {
        const player1Hit = this.player1.getPointHit(this.player2);
        const player2Hit = this.player2.getPointHit(this.player1);

        if (this.player1.getHitpoints() <= 0 && this.player2.getHitpoints() <= 0) {
            this.draw = true;
        } else if (this.player1.getHitpoints() <= 0) {
            if (this.user2 !== null) {
                this.wonPlayerId = this.user2.getDatabaseId();
            }
            this.running = false;
        } else if (this.player2.getHitpoints() <= 0) {
            this.wonPlayerId = this.user1.getDatabaseId();
            this.running = false;
        }

        if (!this.running) {
            this.endGame(player1Hit, player2Hit);
            return;
        }

        this.player1.endRound(player1Hit, player2Hit, this.player2);
        this.player2.endRound(player2Hit, player1Hit, this.player1);
        this.player1.reset();
        this.player2.reset();
    }

    /**
     * ends the game
     * checks which player won (or both)
     * sends endGame updates and pushes the result to the database
     * then rempves the game from the list of running games
     * @param player1Hit the point the first player hit his enemy at
     * @param player2Hit the point the second player hit his enemy at
     */
    private endGame(player1Hit: HitPoint, player2Hit: HitPoint) {
        if (this.draw) {
            this.player1.endGame(this.player2, player1Hit, player2Hit, true);
            this.player2.endGame(this.player1, player2Hit, player1Hit, true);
            DatabaseConnection.getDatabaseConnection().setGameWinner(this.id, this.user1.getDatabaseId());
            if (this.user2 !== null) {
                DatabaseConnection.getDatabaseConnection().setGameWinner(this.id, this.user2.getDatabaseId());
            }
        } else {
            let player2Winner = true;
            if (this.wonPlayerId === this.user1.getDatabaseId()) {
                player2Winner = false;
            }
            this.player1.endGame(this.player2, player1Hit, player2Hit, !player2Winner);
            this.player2.endGame(this.player1, player2Hit, player1Hit, player2Winner);
            if (this.wonPlayerId !== null && this.wonPlayerId !== undefined) {
                DatabaseConnection.getDatabaseConnection().setGameWinner(this.id, this.wonPlayerId);
            }
            Game.removeGame(this);
        }
    }

    /**
     * forces the game to end and the player to loose
     * @param playerId the id of the player that left
     */
    private setPlayerLeft(playerId: number) {
        this.running = false;
        if (this.user1.getDatabaseId() === playerId) {
            this.player1.leaveGame();
            if (this.user2 !== null) {
                this.wonPlayerId = this.user2.getDatabaseId();
            }
        }
        if (this.user2 !== null && this.user2.getDatabaseId() === playerId) {
            this.player2.leaveGame();
            this.wonPlayerId = this.user1.getDatabaseId();
        }
    }

//#endregion private methods

//#region private async methods

    /**
     * initialzes the players input listeners
     * then starts ther gamecountdown
     */
    private async startGameCountdown() {
        this.timeOfLastUpdate = Date.now();
        this.player1.initGameInputListeners();
        this.player2.initGameInputListeners();
        this.doCountdown(3);
    }

    /**
     * sends the players an countdown update
     * starts the game if the countdown reaches 0
     * @param time remaining time of countdown
     */
    private async doCountdown(time: number) {
        const newTime = Date.now();
        this.timeOfLastUpdate = newTime;
        const update: IGameUpdate = {
            type: "countdown",
            value: time,
        };
        this.player1.sendGameUpdate(update);
        this.player2.sendGameUpdate(update);
        if (time > 0) {
            setTimeout(() => {
                this.doCountdown(--time);
            }, 1000);
        } else {
            this.startGame();
        }
    }

    /**
     * starts the gameloop
     */
    private async startGame() {
        this.updateGame();
    }

//#endregion private async methods

}

//#region interfaces

/**
 * interface for a gameupdate
 */
export interface IGameUpdate {
    type: string;
    value: any;
}

//#endregion interfaces
