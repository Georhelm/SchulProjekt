import {Socket} from "socket.io";
import {Player} from "./Player";


export class User {

//#region static properties
    
    private static userList: User[];

//#endregion static properties

//#region properties

    private socket: Socket;
    private username: string;
    private databaseId: number;

//#endregion properties

//#region constructor

    /**
     * creates new user and adds it to the static userlist
     * @param username name of the user
     * @param databaseId id of the user
     * @param socket socket the user uses to communicate
     */
    constructor( username: string, databaseId: number, socket: Socket) {
        this.username = username;
        this.databaseId = databaseId;
        this.socket = socket;

        if (User.userList === undefined) {
            User.userList = [];
        }
        if (databaseId !== -1) {
            User.addUser(this);
        }
    }

//#endregion constructor

//#region getter

    /**
     * gets the database id of the user
     * @returns databaseid
     */
    public getDatabaseId(): number {
        return this.databaseId;
    }

//#endregion getter

//#region public async methods

    /**
     * creates a new player object from this user
     * @param position startposition of the player
     * @param readyListener listener for the player_ready event
     * @param farPlayer if the player is on the far end of the gameworld
     * @return Promise that resolves to the created player object
     */
    public async startGame(position: number, readyListener: () => void, farPlayer: boolean): Promise<Player> {
        const player = new Player(this.username, position, farPlayer, this.databaseId);
        await player.loadEquipment();
        await player.init(readyListener, this.socket);
        return player;
    }

//#endregion public async methods

//#region public static methods

    /**
     * gets current usercount
     * @returns number of online users
     */
    public static getUserCount(): number {
        if(User.userList === undefined) {
            return 0;
        }
        return User.userList.length;
    }

    /**
     * adds a user to the list of online users
     * @param user user to add
     */
    public static addUser(user: User) {
        const oldPlayer = User.getUserByDatabaseId(user.databaseId);
        if(oldPlayer !== null) {
            User.removeUserBySocketId(oldPlayer.socket.id);
        }
        User.userList.push(user);
    }

    /**
     * gets a online player by their id
     * @param id id of the player
     * @returns the user or null of none was found
     */
    public static getUserByDatabaseId(id: number): User | null {
        for (const player of User.userList) {
            if (player.databaseId == id) {
                return player;
            }
        }
        return null;
    }

    /**
     * gets a online user by their socketid
     * @param socketId socketId of the user
     * @returns the user or null of none was found
     */
    public static getUserBySocketId(socketId: string): User | null {
        for (const player of User.userList) {
            if (player.socket.id === socketId) {
                return player;
            }
        }
        return null;
    }

    /**
     * removes a user from the list of online users
     * @param socketId the users socketId
     */
    public static removeUserBySocketId(socketId: string) {
        const players = User.userList;
        for (let i = 0; i < players.length; i++) {
            if (players[i].socket.id === socketId) {
                players.splice(i, 1);
                return;
            }
        }
    }

//#endregion public static methods

//#region debug methods

    /**
     * gets data about the user in a readable format
     * @returns the relevant user data
     */
    public getLogObj(): any {
        const logObj = {
            name: this.username
        };
        return logObj;
    }

//#endregion debug methods

}
