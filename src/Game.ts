import {User} from "./User";
import { Player, PlayerGameData, PlayerGameUpdate, HitPoint} from "./Player";
import { Npc } from "./Npc";
import { DatabaseConnection } from "./DatabaseConnector";

export class Game {

    private id: number;
    private user1: User;
    private user2: User | null;
    private player1: Player;
    private player2: Player;
    private gameWidth: number;
    private timeOfLastUpdate: number;
    private gameStartTime: number;
    private static AllGames: Game[];
    private running: boolean;
    private winSide: number;

    constructor(id: number, player1: User, player2: User | null) {
        this.id = id;
        this.user1 = player1;
        this.user2 = player2;
        this.gameWidth = 10000;

        if(Game.AllGames === undefined) {
            Game.AllGames = [];
        }

        Game.AllGames.push(this);
    }

    public async init() {
        this.player1 = await this.user1.startGame(0, this.playerReady.bind(this), false);
        if (this.user2 === null) {
            this.player2 = new Npc(this.gameWidth, true);
            await (<Npc>this.player2).init();
        }else {
            this.player2 = await this.user2.startGame(this.gameWidth, this.playerReady.bind(this), true);
        }
        
        this.player1.sendFullGameState(this.player2, this.gameWidth);
        this.player2.sendFullGameState(this.player1, this.gameWidth);
    }
    
    private playerReady() {
        console.log("player ready");
        if (this.player1.isReady() && this.player2.isReady()) {
            this.startGameCountdown();
        }
    }

    private async startGameCountdown() {
        this.running = true;
        this.gameStartTime = Date.now();
        console.log("game started");
        this.timeOfLastUpdate = Date.now();
        this.player1.initGameInputListeners();
        this.player2.initGameInputListeners();
        this.doCountdown(3);    
    }

    private async doCountdown(time: number) {
        const newTime = Date.now();
        this.timeOfLastUpdate = newTime;
        const update: GameUpdate = {
            "type": "countdown",
            "value": time
        }
        this.player1.sendGameUpdate(update);
        this.player2.sendGameUpdate(update);
        if (time > 0) {
            setTimeout(() => {
                this.doCountdown(--time);
            }, 1000);
        }else {
            this.startGame();
        }
    }

    private async startGame() {
        this.updateGame();
    }

    private updateGame() {
        const newTime = Date.now();
        const timeDelta = (newTime - this.timeOfLastUpdate) / 1000;
        this.timeOfLastUpdate = newTime;
        if (this.player1.getPosition() >= this.player2.getPosition()){
            this.endRound();
            return;
        }

        this.player1.update(timeDelta);
        this.player2.update(timeDelta);

        this.player1.sendPartialGameUpdate(this.player2, this.gameWidth);
        this.player2.sendPartialGameUpdate(this.player1, this.gameWidth);

        setTimeout(() => {
            this.updateGame();
        }, 33);
    }

    private endRound() {
        const player1Hit = this.getPointHit(this.player1.getWeaponHeight(), this.player2);
        const player2Hit = this.getPointHit(this.player2.getWeaponHeight(), this.player1);

        if(this.player1.getHitpoints() <= 0){
            this.winSide = 0;
            this.running = false;
        }else if(this.player2.getHitpoints() <= 0){
            this.winSide = 1;
            this.running = false;
        } 

        if(!this.running) {
            this.endGame(player1Hit, player2Hit);
            return;
        }

        this.player1.endRound(player1Hit, player2Hit, this.player2);
        this.player2.endRound(player2Hit, player1Hit, this.player1);
        this.player1.reset();
        this.player2.reset();
        console.log("round ended");
    }

    private endGame(player1Hit: HitPoint, player2Hit: HitPoint) {
        this.player1.endGame(this.player2, player1Hit, player2Hit);
        this.player2.endGame(this.player1, player2Hit, player1Hit);
        console.log("game ended");
        DatabaseConnection.getDatabaseConnection().setGameWinner(this.id, this.winSide);
        Game.removeGame(this);
    }

    private static removeGame(game: Game) {
        const index = Game.AllGames.indexOf(game);
        Game.AllGames.splice(index, 1);
    }

    public static endGameContainingPlayer(user: User) {
        if(Game.AllGames === undefined) {
            return;
        }
        for(const game of Game.AllGames) {
            if(game.user1 === user){
                game.running = false;
                game.winSide = 1;
            }else if(game.user2 === user) {
                game.running = false;
                game.winSide = 0;
            }
        }
    }

    public getLogObj(): any {
        const logObj = {
            id: this.id,
            player1: this.player1.getLogObj(),
            player2: this.player2.getLogObj(),
            width: this.gameWidth
        };
        return logObj;
    }

    private getPointHit(weaponHeight: number, enemy: Player): HitPoint {
        let pointHit = HitPoint.Missed;
        if(weaponHeight >= enemy.getMount().getHeight() + 75 && weaponHeight < enemy.getMount().getHeight() + 175){
            pointHit = HitPoint.Head;
            enemy.setHitpoints(enemy.getHitpoints() - 50);
        }else if(weaponHeight < enemy.getMount().getHeight() + 75 && weaponHeight > enemy.getMount().getHeight() - 25) {
            pointHit = HitPoint.Body;
            enemy.setHitpoints(enemy.getHitpoints() - 25);
        }
        return pointHit;
    }
}

export interface GameUpdate {
    type: string,
    value: any
}