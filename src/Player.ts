
import {Socket} from "socket.io";
import { Constants } from "./Constants";
import {DatabaseConnection} from "./DatabaseConnector";
import {IGameUpdate} from "./Game";
import {Mount} from "./Mount";
import {Weapon} from "./Weapon";

export class Player {

//#region protected properties

    protected mount: Mount;
    protected weapon: Weapon;
    protected ready: boolean;
    protected isLiftingWeapon: boolean;

//#endregion protected properties

//#region private properties

    private position: number;
    private startPosition: number;
    private onPlayerReady: () =>  void;
    private socket: Socket;
    private username: string;
    private farPlayer: boolean;
    private databaseId: number;
    private hitpoints: number;
    private hasLeft: boolean;
    private inputListener: (data: string) =>  void;
    private readyStateListener: () => void;

//#endregion private properties

//#region contructor

    /**
     * creates a new player for one game
     * @param username the players name
     * @param position the startposition of the player
     * @param farPlayer if the player starts at the far position
     * @param databaseId the id of the player
     */
    constructor(username: string, position: number, farPlayer: boolean, databaseId: number) {
        this.position = position;
        this.startPosition = position;
        this.ready = false;
        this.isLiftingWeapon = false;
        this.username = username;
        this.farPlayer = farPlayer;
        this.databaseId = databaseId;
        this.hasLeft = false;
        this.hitpoints = 100;
    }

//#endregion constructor

//#region getters

    public getPosition(): number {
        return this.position;
    }

    public isReady(): boolean {
        return this.ready;
    }

    public getUsername(): string {
        return this.username;
    }

    public getWeapon(): Weapon {
        return this.weapon;
    }

    public getSpeed(): number {
        return this.mount.getSpeed();
    }

    public getMount(): Mount {
        return this.mount;
    }

    public getHitpoints(): number {
        return this.hitpoints;
    }

    public getLeftState(): boolean {
        return this.hasLeft;
    }

//#endregion getters

//#region setters

    /**
     * sets the players hitpoints to a minimum of 0
     * @param hitpoints the hitpoints to set the player to
     */
    public setHitpoints(hitpoints: number) {
        this.hitpoints = Math.max(hitpoints, 0);
    }

//#endregion setters

//#region public methods

    /**
     * resets the player for the next round
     */
    public reset() {
        this.mount.setSpeed(0);
        this.weapon.reset();
        this.ready = false;
        this.position = this.startPosition;
    }

    /**
     * gets all relevant gamedata about the player
     * @returns the players gamedata
     */
    public getGameData(): IPlayerGameData {
        const result: IPlayerGameData = {
            hitpoints: this.hitpoints,
            mountHeight: this.mount.getHeight(),
            mountId: this.mount.getId(),
            position: Math.round(this.position),
            username: this.username,
            weaponId: this.weapon.getId(),
        };

        return result;
    }

    /**
     * updates the players position and weapon angle
     * @param timeDelta the time since the last update
     */
    public update(timeDelta: number) {
        this.mount.accelerate(timeDelta);
        if (this.farPlayer) {
            this.updatePosition(- timeDelta);
        } else {
            this.updatePosition(timeDelta);
        }
        this.updateWeaponAngle(timeDelta);
    }

    /**
     * initializes the player
     * @param playerReadyListener listener for the player_ready event
     * @param socket the socket the player connected on
     */
    public init(playerReadyListener: () => void, socket: Socket) {
        this.socket = socket;
        this.setPlayerReadyListener(playerReadyListener);
    }

    /**
     * thats the left state for the player
     */
    public leaveGame() {
        this.hasLeft = true;
        this.ready = true;
        this.onPlayerReady();
    }

    /**
     * send the roundEnd update to the player
     * @param playerHit the point the player hit the enemy
     * @param enemyHit the point the enemy hit the player
     * @param enemy the enemy player
     */
    public endRound(playerHit: HitPoint, enemyHit: HitPoint, enemy: Player) {
        this.sendRoundEndUpdate(enemy, playerHit, enemyHit, "roundEnd");
    }

    /**
     * sends the gameEnd event to the player
     * removes the listeners for the players events
     * @param enemy the enemy player
     * @param playerHit the point the player hit the enemy
     * @param enemyHit the point the enemy hit the player
     * @param victory if the player won the game
     */
    public endGame(enemy: Player, playerHit: HitPoint, enemyHit: HitPoint, victory: boolean) {

        this.sendRoundEndUpdate(enemy, playerHit, enemyHit, "gameEnd", victory);
        this.socket.removeListener("player_ready", this.readyStateListener);
        this.socket.removeListener("game_input", this.inputListener);
    }

    /**
     * sends the full game state to the player
     * @param enemy the enemy player
     * @param gameWidth the width of the gameworld
     */
    public sendFullGameState(enemy: Player, gameWidth: number) {
        const update = this.getFullGameState(enemy, gameWidth);
        this.sendMessage("found_game", update);
    }

    /**
     * sends a partial gameupdate to the player
     * @param enemy the enemy player
     * @param gameWidth the width of the gameworld
     */
    public sendPartialGameUpdate(enemy: Player, gameWidth: number) {
        const gameUpdate: IGameUpdate = {
            type: "partialUpdate",
            value: this.getGameUpdate(enemy, gameWidth),
        };
        this.sendGameUpdate(gameUpdate);
    }

    /**
     * initializes the listener for the game_input event
     */
    public initGameInputListeners() {
        this.inputListener = this.onGameInput.bind(this);
        this.socket.on("game_input", this.inputListener);
    }

    /**
     * calculates the point the player hits the enemy at
     * @param enemy the enemy player
     * @returns the point the player hit the enemy at
     */
    public getPointHit(enemy: Player): HitPoint {
        let pointHit = HitPoint.Missed;
        if (this.getWeaponHeight() >= enemy.getMount().getHeight() + 75 && this.getWeaponHeight() < enemy.getMount().getHeight() + 175) {

            pointHit = HitPoint.Head;
            enemy.setHitpoints(enemy.getHitpoints() - Constants.HEADHITDAMAGE);

        } else if (this.getWeaponHeight() < enemy.getMount().getHeight() + 75 &&
        this.getWeaponHeight() > enemy.getMount().getHeight() - 25) {

            pointHit = HitPoint.Body;
            enemy.setHitpoints(enemy.getHitpoints() - Constants.BODYHITDAMAGE);
        }
        return pointHit;
    }

//#endregion public methods

//#region public async methods

    /**
     * loads the equiped equipment for the player
     */
    public async loadEquipment() {
        this.mount = await DatabaseConnection.getDatabaseConnection().getEquippedMount(this.databaseId);
        this.weapon = await DatabaseConnection.getDatabaseConnection().getEquippedWeapon(this.databaseId);
    }

    /**
     * sends a gameupdate to the client
     */
    public async sendGameUpdate(update: IGameUpdate) {
        this.sendMessage("game_update", update);
    }

    /**
     * sends a message to the player if he has not left
     * @param type the type of the message
     * @param payload the message to send
     */
    public async sendMessage(type: string, payload: any) {
        if (!this.hasLeft) {
            this.socket.emit(type , payload);
        }
    }

//#endregion public async methods

//#region debug methods

    /**
     * gets data about the player in a readable format
     * @returns the relevant player data
     */
    public getLogObj() {
        return {
            lifting: this.isLiftingWeapon,
            position: this.position,
            username: this.username,
            weaponAngle: this.weapon.getAngle(),
        };
    }

//#endregion debug methods

//#region private methods

    /**
     * updates the players position
     * @param timeDelta the time since the last update
     */
    private updatePosition(timeDelta: number) {
        this.position += this.mount.getSpeed() * timeDelta;
    }

    /**
     * sets the listener for the player_ready event
     * @param playerReadyListener the listener for the player_ready event
     */
    private setPlayerReadyListener(playerReadyListener: () => void) {
        this.onPlayerReady = playerReadyListener;
        this.readyStateListener = this.playerReady.bind(this);
        this.socket.on("player_ready", this.readyStateListener);
    }

    /**
     * executes the listener for the player_ready event if the player has not left
     */
    private playerReady() {
        if (!this.hasLeft) {
            this.ready = true;
            if (this.onPlayerReady !== undefined) {
                this.onPlayerReady();
            }
        }
    }

    /**
     * handles the players game inputs
     * @param data the json sent by the player
     */
    private onGameInput(data: string) {
        if (!this.hasLeft) {
            const json: IGameInput = JSON.parse(data);
            if (json.type === "lance") {
                this.isLiftingWeapon = json.value;
            }
        }
    }

    /**
     * gets the full gamestate of the gameworld from the view of the player
     * @param enemy the enemy player
     * @param gameWidth the width of the gameworld
     * @returns the full gamestate
     */
    private getFullGameState(enemy: Player, gameWidth: number): IFullGameState {

        let pos = this.position;
        let enemyPos = enemy.getPosition();
        if (this.farPlayer) {
            pos = gameWidth - pos;
            enemyPos = gameWidth - enemyPos;
        }

        const result: IFullGameState = {
            gameWidth,
            player1:   {
                hitpoints: this.hitpoints,
                mountHeight: this.mount.getHeight(),
                mountId: this.mount.getId(),
                position: Math.round(pos),
                username: this.username,
                weaponId: this.weapon.getId(),
            },
            player2: {
                hitpoints: enemy.getHitpoints(),
                mountHeight: enemy.getMount().getHeight(),
                mountId: enemy.getMount().getId(),
                position: Math.round(enemyPos),
                username: enemy.getUsername(),
                weaponId: enemy.getWeapon().getId(),
            },
        };

        return result;
    }

    /**
     * gets an updated gamestate from the view of the player
     * @param enemy the enemy player
     * @param gameWidth the width of the gameWorld
     * @returns an updated gamestate
     */
    private getGameUpdate(enemy: Player, gameWidth: number): IUpdatedGameState {

        let pos = this.position;
        let enemyPos = enemy.getPosition();

        if (this.farPlayer) {
            pos = gameWidth - pos;
            enemyPos = gameWidth - enemyPos;
        }

        return {
            player1: {
                position: Math.round(pos),
                weaponAngle: Math.round(this.weapon.getAngle()),
            },
            player2: {
                position: Math.round(enemyPos),
                weaponAngle: Math.round(enemy.getWeapon().getAngle()),
            },
        };
    }

    /**
     * sends the player an updaten with information about the result of the round
     * @param enemy enemy player
     * @param playerHit position the player hit the enemy
     * @param enemyHit position the enemy hit the player
     * @param type type of the update (currently supported roundEnd, gameEnd)
     * @param victory if the player has won (set only on the last round)
     */
    private sendRoundEndUpdate(enemy: Player, playerHit: HitPoint, enemyHit: HitPoint, type: string, victory?: boolean) {
        const endRoundUpdate: IGameUpdate = {
            type,
            value: {
                player1: {
                    hitpoints: this.hitpoints,
                    pointHit: playerHit,
                    speed: this.getSpeed(),
                    weaponHeight: this.getWeaponHeight(),
                },
                player2: {
                    hitpoints: enemy.getHitpoints(),
                    pointHit: enemyHit,
                    speed: enemy.getSpeed(),
                    weaponHeight: enemy.getWeaponHeight(),
                },
            },
        };
        if (victory !== undefined) {
            endRoundUpdate.value.victory = victory;
        }
        this.sendGameUpdate(endRoundUpdate);
    }

    /**
     * updates the players weapon angle
     * @param timeDelta time since the last update
     */
    private updateWeaponAngle(timeDelta: number) {
        this.weapon.updateAngle(timeDelta, this.isLiftingWeapon);
    }

    /**
     * calculates the height of the weapons tip in relation to the ground
     * @returns the height of the weapons tip
     */
    private getWeaponHeight(): number {
        return Math.round(this.weapon.getHeight() + this.mount.getHeight());
    }

//#endregion private methods

}

//#region interfaces

/**
 * interface for one players gamedata
 */
export interface IPlayerGameData {
    username: string;
    mountId: number;
    weaponId: number;
    position: number;
    mountHeight: number;
    hitpoints: number;
}

/**
 * interface for one player gameupdate
 */
export interface IPlayerGameUpdate {
    position: number;
    weaponAngle: number;
}

/**
 * interface for one player input
 */
interface IGameInput {
    type: string;
    value: boolean;
}

/**
 * enum for possible points to hit on a player
 */
export enum HitPoint {
    Head,
    Body,
    Missed,
}

/**
 * interface for a full gamestate
 */
export interface IFullGameState {
    player1: IPlayerGameData;
    player2: IPlayerGameData;
    gameWidth: number;
}

/**
 * interface for an updated gamestate
 */
export interface IUpdatedGameState {
    player1: IPlayerGameUpdate;
    player2: IPlayerGameUpdate;
}

//#endregion interfaces
