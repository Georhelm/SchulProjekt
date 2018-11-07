import {DatabaseConnection} from "./DatabaseConnector";
import {Mount} from "./Mount";
import {Weapon} from "./Weapon";
import {GameUpdate, Game} from "./Game";
import {Socket} from "socket.io";


export class Player {

    public socket: Socket;
    private databaseId: number;
    public username: string;
    public mount: Mount;
    public weapon: Weapon;
    protected ready: boolean;
    private position: number;
    private onPlayerReady: () =>  void;
    private static playerList: Player[];
    private isLiftingWeapon: boolean;


    constructor( username: string, databaseId: number = -1) {
        this.username = username;
        this.position = 0;
        this.databaseId = databaseId;
        this.ready = false;
        this.isLiftingWeapon = false;
        
        if (Player.playerList === undefined) {
            Player.playerList = [];
        }
        if (databaseId !== -1) {
            Player.addPlayer(this);
        }
    }

    public async init(socket: Socket) {
        this.socket = socket;
        await this.loadEquipment();
    }

    protected async loadEquipment() {
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
    
    public updatePosition(timeDelta: number) {
        this.position += this.mount.getSpeed() * timeDelta;
    } 

    public async sendGameUpdate(update: GameUpdate) {
        this.socket.emit("game_update", update);
    }

    public getPosition(): number {
        return this.position;
    }

    public getDatabaseId(): number {
        return this.databaseId;
    }

    public setPlayerReadyListener(fn: () => void) {
        this.onPlayerReady = fn;
        this.socket.on("player_ready", this.playerReady.bind(this));
    }

    private playerReady() {
        this.ready = true;
        if (this.onPlayerReady !== undefined) {
            this.onPlayerReady();
        }
    }

    public initGameInputListeners() {
        this.socket.on("game_input", this.onGameInput.bind(this));
    }

    private onGameInput(data: string) {
        const json: GameInput = JSON.parse(data);
        if (json.type === "lance") {
            this.isLiftingWeapon = json.value;
        }
    }

    public setPosition(pos: number) {
        this.position = pos;
    }

    public static addPlayer(player: Player) {
        const oldPlayer = Player.getPlayerByDatabaseId(player.databaseId);
        if(oldPlayer !== null) {
            Player.removePlayerBySocketId(oldPlayer.socket.id);
        }
        Player.playerList.push(player);
    }

    public static getPlayerByDatabaseId(id: number) {
        for (const player of Player.playerList) {
            if (player.databaseId == id) {
                return player;
            }
        }
        return null;
    }

    public static getPlayerBySocketId(socketId: string) {
        for (const player of Player.playerList) {
            if (player.socket.id === socketId) {
                return player;
            }
        }
        return null;
    }

    public static removePlayerBySocketId(socketId: string) {
        const players = Player.playerList;
        for (let i = 0; i < players.length; i++) {
            if (players[i].socket.id === socketId) {
                players.splice(i, 1);
                return;
            }
        }
        return;
    }

    public updateWeaponPosition(timeDelta: number) {
        this.weapon.updateAngle(timeDelta, this.isLiftingWeapon);
    }

    public static getPlayerCount(): number {
        return Player.playerList.length;
    }

    public getLogObj(): any {
        const logObj = {
            name: this.username
        };
        return logObj;
    }

    public isReady(): boolean {
        return this.ready;
    }

    public getUpdatedGameState(): PlayerGameUpdate {
        return {
            position: Math.round(this.position),
            weaponAngle: Math.round(this.weapon.getAngle())
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