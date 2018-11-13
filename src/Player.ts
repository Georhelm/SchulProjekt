import {Mount} from "./Mount";
import {Weapon} from "./Weapon";
import {DatabaseConnection} from "./DatabaseConnector";
import {Socket} from "socket.io";
import {GameUpdate} from "./Game";

export class Player {

    private position: number;
    private mount: Mount;
    private weapon: Weapon;
    protected ready: boolean;
    protected isLiftingWeapon: boolean;
    private onPlayerReady: () =>  void;
    private socket: Socket;
    private username: string;
    private farPlayer: boolean;

    constructor(username: string, position: number, farPlayer: boolean) {
        this.position = position;
        this.ready = false;
        this.isLiftingWeapon = false;
        this.username = username;
        this.farPlayer = farPlayer;
    }

    public async loadEquipment() {
        this.mount = await DatabaseConnection.getDatabaseConnection().getMountById(2);
        this.weapon = await DatabaseConnection.getDatabaseConnection().getWeaponById(1);
    }

    public getGameData(): PlayerGameData {
        const result: PlayerGameData = {
            username: this.username,
            mountId: this.mount.getId(),
            weaponId: this.weapon.getId(),
            position: Math.round(this.position)
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
        return {
            position: Math.round(this.position),
            weaponAngle: Math.round(this.weapon.getAngle())
        }
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
    position: number
}

export interface PlayerGameUpdate {
    position: number,
    weaponAngle: number
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
