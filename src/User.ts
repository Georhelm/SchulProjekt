import {GameUpdate, Game} from "./Game";
import {Socket} from "socket.io";
import {Player} from "./Player";


export class User {

    public socket: Socket;
    private databaseId: number;
    public username: string;
    private static userList: User[];


    constructor( username: string, databaseId: number = -1, socket: Socket) {
        this.username = username;
        this.databaseId = databaseId;
        this.socket = socket;

        if (User.userList === undefined) {
            User.userList = [];
        }
        if (databaseId !== -1) {
            User.addPlayer(this);
        }
    }

    public async startGame(position: number, fn: () => void, farPlayer: boolean): Promise<Player> {
        const player = new Player(this.username, position, farPlayer);
        await player.loadEquipment();
        await player.init(fn, this.socket);
        return player;
    }

    public getDatabaseId(): number {
        return this.databaseId;
    }

    public static getPlayerCount(): number {
        return User.userList.length;
    }

    public getLogObj(): any {
        const logObj = {
            name: this.username
        };
        return logObj;
    }

    public static addPlayer(player: User) {
        const oldPlayer = User.getPlayerByDatabaseId(player.databaseId);
        if(oldPlayer !== null) {
            User.removePlayerBySocketId(oldPlayer.socket.id);
        }
        User.userList.push(player);
    }

    public static getPlayerByDatabaseId(id: number) {
        for (const player of User.userList) {
            if (player.databaseId == id) {
                return player;
            }
        }
        return null;
    }

    public static getPlayerBySocketId(socketId: string) {
        for (const player of User.userList) {
            if (player.socket.id === socketId) {
                return player;
            }
        }
        return null;
    }

    public static removePlayerBySocketId(socketId: string) {
        const players = User.userList;
        for (let i = 0; i < players.length; i++) {
            if (players[i].socket.id === socketId) {
                players.splice(i, 1);
                return;
            }
        }
        return;
    }

}
