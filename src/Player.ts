import {Mount} from "./Mount";
import {Weapon} from "./Weapon";
import {DatabaseConnection} from "./DatabaseConnector";
import {Socket} from "socket.io";
import {GameUpdate} from "./Game";

export class Player {

    private position: number;
    protected mount: Mount;
    protected weapon: Weapon;
    protected ready: boolean;
    protected isLiftingWeapon: boolean;
    private onPlayerReady: () =>  void;
    private socket: Socket;
    private username: string;
    private farPlayer: boolean;
    private databaseId: number;
    private hitpoints: number;

    constructor(username: string, position: number, farPlayer: boolean, databaseId: number) {
        this.position = position;
        this.ready = false;
        this.isLiftingWeapon = false;
        this.username = username;
        this.farPlayer = farPlayer;
        this.databaseId = databaseId;
        this.hitpoints = 100;
    }

    public async loadEquipment() {
        this.mount = await DatabaseConnection.getDatabaseConnection().getEquippedMount(this.databaseId);
        this.weapon = await DatabaseConnection.getDatabaseConnection().getEquippedWeapon(this.databaseId);
    }

    public getGameData(): PlayerGameData {
        const result: PlayerGameData = {
            username: this.username,
            mountId: this.mount.getId(),
            weaponId: this.weapon.getId(),
            position: Math.round(this.position),
            mountHeight: this.mount.getHeight()
        }

        return result;
    }

    public update(timeDelta: number) {
        this.mount.accelerate(timeDelta);
        if (this.farPlayer) {
            this.updatePosition(- timeDelta);
        }else {
            this.updatePosition(timeDelta);
        }
        this.updateWeaponAngle(timeDelta);
    }

    private updatePosition(timeDelta: number) {
        this.position += this.mount.getSpeed() * timeDelta;
    }

    public getPosition(): number {
        return this.position;
    }

    public init(fn: () => void, socket: Socket){
        this.socket = socket;
        this.setPlayerReadyListener(fn);
    }

    private setPlayerReadyListener(fn: () => void) {
        this.onPlayerReady = fn;
        this.socket.once("player_ready", this.playerReady.bind(this));
    }

    private playerReady() {
        this.ready = true;
        if (this.onPlayerReady !== undefined) {
            this.onPlayerReady();
        }
    }

    private onGameInput(data: string) {
        const json: GameInput = JSON.parse(data);
        if (json.type === "lance") {
            this.isLiftingWeapon = json.value;
        }
    }

    public isReady(): boolean {
        return this.ready;
    }

    public endGame(enemySpeed: number) {
        const endGameUpdate: GameUpdate = {
            type: "gameEnd",
            value: {
                player1: {
                    speed: this.getSpeed(),
                    hitPoint: HitPoint.Body  // what that player hits
                },
                player2: {
                    speed: enemySpeed,
                    hitPoint: HitPoint.Head
                }
            }
        }
        this.sendGameUpdate(endGameUpdate);
        //this.socket.removeListener("game_input", this.onGameInput.bind(this));
    }

    public initGameInputListeners() {
        this.socket.on("game_input", this.onGameInput.bind(this));
    }

    private updateWeaponAngle(timeDelta: number) {
        this.weapon.updateAngle(timeDelta, this.isLiftingWeapon);
    }

    public getUpdatedGameState(): PlayerGameUpdate {
        const state = {
            position: Math.round(this.position),
            weaponAngle: Math.round(this.weapon.getAngle()),
            weaponHeight: this.getWeaponHeight()
        };
        if(this.databaseId !== -1) {
            console.log(state);
        }
        return state;
    }

    private getWeaponHeight(): number {
        return Math.round(Math.cos(this.weapon.getAngle() * Math.PI / 180) * Weapon.LANCELENGTH + this.mount.getHeight());
    }

    public async sendGameUpdate(update: GameUpdate) {
        this.socket.emit("game_update", update);
    }

    public getSpeed(): number {
        return this.mount.getSpeed();
    }

    //DEBUG 
    public getLogObj() {
        return {
            username: this.username,
            position: this.position,
            weaponAngle: this.weapon.getAngle(),
            lifting: this.isLiftingWeapon
        }
    }
}


export interface PlayerGameData {
    username: string,
    mountId: number,
    weaponId: number,
    position: number,
    mountHeight: number
}

export interface PlayerGameUpdate {
    position: number,
    weaponAngle: number,
    weaponHeight: number
}

interface GameInput {
    type: string;
    value: boolean;
}

enum HitPoint {
    Head,
    Body,
    Missed
}