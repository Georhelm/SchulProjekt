import {DatabaseConnection} from "./DatabaseConnector";
import {Mount} from "./Mount";
import {Weapon} from "./Weapon";
import {GameUpdate} from "./Game";
import {Socket} from "socket.io";


export class Player {

    public socket: Socket;
    private databaseId: number;
    public username: string;
    public mount: Mount;
    public weapon: Weapon;
    private position: number;
    private static playerList: Player[];

    constructor( username: string, databaseId: number = -1) {
        this.username = username;
        this.position = 0;
        this.databaseId = databaseId;
        
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
        this.mount = await DatabaseConnection.getDatabaseConnection().getMountById(1);
        this.weapon = await DatabaseConnection.getDatabaseConnection().getWeaponById(1);
    }

    public getGameData(): PlayerGameData {
        const result: PlayerGameData = {
            username: this.username,
            mountId: this.mount.getId(),
            weaponId: this.weapon.getId(),
            position: this.position
        }

        return result;
    }

    public async sendGameUpdate(update: GameUpdate) {
        this.socket.emit("gameUpdate", update);
    }

    public getDatabaseId(): number {
        return this.databaseId;
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

    public static getPlayerCount(): number {
        return Player.playerList.length;
    }

    public getLogObj(): any {
        const logObj = {
            name: this.username
        };
        return logObj;
    }

}

export interface PlayerGameData {
    username: string,
    mountId: number,
    weaponId: number,
    position: number
} 