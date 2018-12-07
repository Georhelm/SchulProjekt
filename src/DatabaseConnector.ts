import Crypto = require("crypto");
import mysql = require("mysql");
import * as config from "../config.json";
import {Game} from "./Game";
import { Mount } from "./Mount";
import {User} from "./User";
import {Weapon} from "./Weapon";

export class DatabaseConnection {

//#region public static methods

    /**
     * gets the current database connection
     */
    public static getDatabaseConnection() {
        return DatabaseConnection.connection;
    }

//#endregion public static methods

//#region static properties

    private static connection: DatabaseConnection;

//#endregion static properties

//#region properties

    private username: string;
    private password: string;
    private server: string;
    private database: string;
    private connectionPool: mysql.Pool;

//#endregion properties

//#region constructor

    /**
     * creates a new database connection using a mysql connection pool
     * sets it as the active database connection
     * @param server the address of the server to connect to
     * @param username the username to use to connect to the server
     * @param password the password to use to connect to the server
     * @param database the database to use to store and get data
     */
    constructor(server: string, username: string, password: string, database: string) {
        this.username = username;
        this.server = server;
        this.password = password;
        this.database = database;
        const connectionInfo: mysql.PoolConfig = {
            connectionLimit: 10,
            database: this.database,
            host: this.server,
            password: this.password,
            user: this.username,
        };

        this.connectionPool = mysql.createPool(connectionInfo);
        DatabaseConnection.connection = this;
    }

//#endregion constructor

//#region public async methods

    /**
     * registers a user if he doesnt exists
     * hashes the users password with the hashkey from the config file and sha256
     * @param user the username
     * @param pass the users password
     * @returns a promise that resolves with a user_exists or a success result
     */
    public async registerUser(user: string, pass: string): Promise<ICommunicationData> {

        const userExists: any[] = await this.query("Select * from users where name=?", [user]);

        if (userExists.length !== 0) {
            const data: ICommunicationData = {
                method: "register",
                msg: "user_exists",
                result: "error",
            };
            return data;
        }

        const hashedPass = Crypto.createHmac("sha256", config.hashkey);
        hashedPass.update(pass);

        const createUser = await this.query("Insert into users (name, password) Values (?,?)",
            [user, hashedPass.digest("hex")]);

        const msg: ICommunicationData = {
            method: "register",
            result: "success",
        };

        return msg;

    }

    /**
     * checks the login data and creates a new logintoken if it is correct
     * @param user the username
     * @param pass the users password
     * @returns a promise that resolves with the logintoke or a wrong_login message
     */
    public async loginUser(user: string, pass: string): Promise<ICommunicationData> {

        const hashedPass = Crypto.createHmac("sha256", config.hashkey);
        hashedPass.update(pass);

        const userCorrect = await this.query("Select * from users where name=? and password=?",
            [user, hashedPass.digest("hex")]);

        if (userCorrect.length !== 1) {
            const data: ICommunicationData = {
                method: "login",
                msg: "wrong_login",
                result: "error",
            };
            return data;
        }

        const hmac = Crypto.createHmac("sha256", config.hashkey);
        hmac.update(userCorrect[0].name + Date.now());
        const hashToken = hmac.digest("hex");
        await this.query("Update users set token=? where name=?", [hashToken, userCorrect[0].name]);

        const message: ICommunicationData = {
            method: "login",
            result: "success",
            token: hashToken,
        };

        return message;

    }

    /**
     * checks the authtoken and gets the corresponding user
     * @param token the users authtoken
     * @returns a promise that resolves with the username and id or rejects if the token is incorrect
     */
    public async checkAuthToken(token: string): Promise<{name: string, id: number}> {

        const result: Array<{name: string, id: number}> = await this.query("Select id, name from users where token=?",
            [token]);

        if (result.length === 0) {
            throw new Error("Auth token wrong");
        }

        return result[0];
    }

    /**
     * gets a mount by its id
     * @param mountId the mounts id
     * @returns a promise that resolves to the requested mount or rejects if the mount does not exist
     */
    public async getMountById(mountId: number): Promise<Mount> {
        const result = await this.query("Select id, name, maxSpeed, acceleration, height from mounts where id=?",
            [mountId]);
        if (result.length === 0) {
            throw new Error("Mount does not exist");
        }

        return new Mount(result[0].id, result[0].name, result[0].maxSpeed, result[0].acceleration, result[0].height);
    }

    /**
     * gets a weapon by its id
     * @param weaponId the weapons id
     * @return a promise that resolves to the requested weapon or rejects if the weapon does not exist
     */
    public async getWeaponById(weaponId: number): Promise<Weapon> {
        const result = await this.query("Select id, name, lift_speed, fall_speed from weapons where id=?", [weaponId]);
        if (result.length === 0) {
            throw new Error("Weapon does not exist");
        }

        return new Weapon(result[0].id, result[0].name, result[0].lift_speed, result[0].fall_speed);
    }

    /**
     * gets the equipped weapon of a user
     * @param userid the users id
     * @returns a promise that resolves to the users weapon or rejects if the user does not have a valid weapon equipped
     */
    public async getEquippedWeapon(userid: number): Promise<Weapon> {
        const result = await this.query(`Select w.id, w.name, w.lift_speed, w.fall_speed from users u join weapons w on u.weaponid = w.id where u.id = ?`, [userid]);
        if (result.length === 0) {
            throw new Error("User has not valid weapon");
        }

        return new Weapon(result[0].id, result[0].name, result[0].lift_speed, result[0].fall_speed);
    }

    /**
     * gets the equipped mount of a user
     * @param userid the users id
     * @returns a promise that resolves to the users mount or rejects if the user does not have a valid mount equipped
     */
    public async getEquippedMount(userid: number): Promise<Mount> {
        const result = await this.query(`Select m.id, m.name, m.maxSpeed, m.acceleration, m.height from users u join mounts m on u.mountid = m.id where u.id = ?`, [userid]);
        if (result.length === 0) {
            throw new Error("User has no valid mount");
        }

        return new Mount(result[0].id, result[0].name, result[0].maxSpeed, result[0].acceleration, result[0].height);
    }

    /**
     * gets a random mount
     * @returns a promise that resolves to a random mount or rejects if no mounts are in the database
     */
    public async getRandomMount(): Promise<Mount> {
        const result = await this.query("Select id, name, maxSpeed, acceleration, height from mounts order by Rand() limit 1", []);
        if (result.length === 0) {
            throw new Error("No Mount found");
        }

        return new Mount(result[0].id, result[0].name, result[0].maxSpeed, result[0].acceleration, result[0].height);
    }

    /**
     * gets all mounts
     * @returns a promise that resolves to a list of all mounts
     */
    public async getAllMounts(): Promise<Mount[]> {
        const results = await this.query("Select id, name, maxSpeed, acceleration, height from mounts", []);
        const allMounts: Mount[] = [];

        for (const result of results) {
            allMounts.push(new Mount(result.id, result.name, result.maxSpeed, result.acceleration, result.height));
        }

        return allMounts;
    }

    /**
     * creates a game and initializes it
     * saves it and its connected players to the database
     * @param user1 the first user
     * @param user2 the second user or null if its an npc
     * @param type the type of the game (currently singleplayer, multiplayer)
     * @returns a promise that resolves to the game after it is initialized
     */
    public async createGame(user1: User, user2: User | null, type: string): Promise<Game> {
        const result = await this.query("Insert into gamedata (type) Select id from gametype where name=?", [type]);
        await this.query("Insert into user_game (gameid, playerid) Values (?, ?)", [result.insertId, user1.getDatabaseId()]);
        if (user2 !== null && user2.getDatabaseId() !== -1) {
            await this.query("Insert into user_game (gameid, playerid) Values (?, ?)", [result.insertId, user2.getDatabaseId()]);
        }

        const game = new Game(result.insertId, user1, user2);
        await game.init();
        return game;

    }

    /**
     * sets a games winner
     * @param gameId the id of the game
     * @param wonPlayerId the id of the player who won
     */
    public async setGameWinner(gameId: number, wonPlayerId: number) {
        await this.query("Update user_game set won=1 where gameid=? and playerid=?", [gameId, wonPlayerId]);
    }

    /**
     * sets a users mount
     * @param mountId the id of the mount
     * @param userId the user of whom to set the mount
     * @returns a promise that resolves after updating the user
     */
    public async setMountOfUser(mountId: number, userId: number) {
        await this.query("Update users set mountid = ? where id = ?", [mountId, userId]);
    }

    /**
     * gets a users wincount
     * @param userId the id of the user
     * @returns a promise that resolves to the number of wins
     */
    public async getUserWins(userId: number) {
        const result = await this.query("Select Count(*) as wins from user_game where playerid = ? and won=1", [userId]);
        return result[0].wins;
    }

//#endregion public async methods

//#region private methods

    /**
     * querys the database for the query string with the provided arguments
     * @param query the query string to execute
     * @param args the arguments to populate the query string with
     * @returns a promise that resolves with the query result
     */
    private query(query: string, args: any[]): Promise<any> {
        return new Promise((resolve, reject) => {
            this.connectionPool.query(query, args, (err, result) => {
                if (err) {
                    throw err;
                }
                resolve(result);
            });
        });
    }

//#endregion private methods

}

//#region interfaces

/**
 * interface for communcation between webserver and database
 */
export interface ICommunicationData {
    method: string;
    result: string;
    [data: string]: any;
}

//#endregion interfaces
