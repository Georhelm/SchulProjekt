import mysql = require("mysql");
import Crypto = require("crypto");
import {Config} from "./Config";
import {Player} from "./Player";
import { Mount } from "./Mount";
import {Weapon} from "./Weapon";
import {Game} from "./Game";

const config: Config = require("../config.json");

export class DatabaseConnection {
    
    private username: string;
    private password: string;
    private server : string;
    private database: string;
    private connectionPool : mysql.Pool;
    private static connection: DatabaseConnection;

    constructor(server: string, username: string, password: string, database: string) {
        this.username = username;
        this.server = server;
        this.password = password;
        this.database = database;
        const connectionInfo: mysql.PoolConfig = {
            host: this.server,
            database: this.database,
            user: this.username,
            password: this.password,
            connectionLimit: 10
        }

        this.connectionPool = mysql.createPool(connectionInfo);
        DatabaseConnection.connection = this;
    }

    public static getDatabaseConnection() {
        return DatabaseConnection.connection;
    }

    private query(query: string, args: Array<any>): Promise<any> {
        return new Promise((resolve, reject) => {
            this.connectionPool.query(query, args, (err, result) => {
                if (err) {
                    console.error("error", err);
                    throw err;
                }
                resolve(result);
            });
        });
    }


    public async registerUser(user: string, pass: string): Promise<CommunicationData> {

        const userExists: any[] = await this.query("Select * from users where name=?", [user]); 

        if (userExists.length !== 0) {
            const msg: CommunicationData = {
                "method": "register",
                "result": "error",
                "msg": "user_exists"
            }
            return msg;
        }

        const hashedPass = Crypto.createHmac("sha256", config.hashkey);
        hashedPass.update(pass);

        const createUser = await this.query("Insert into users (name, password) Values (?,?)", [user, hashedPass.digest("hex")]);

        const msg: CommunicationData = {
            "method": "register",
            "result": "success"
        };

        return msg;

    }

    public async loginUser(user: string, pass: string): Promise<CommunicationData> {

        const hashedPass = Crypto.createHmac("sha256", config.hashkey);
        hashedPass.update(pass);

        const userCorrect = await this.query("Select * from users where name=? and password=?", [user, hashedPass.digest("hex")]); 
        
        if (userCorrect.length !== 1) {
            const msg: CommunicationData = {
                "method": "login",
                "result": "error",
                "msg": "wrong_login"
            }
            return msg;
        }

        const hmac = Crypto.createHmac("sha256", config.hashkey);
        hmac.update(userCorrect[0].name + Date.now());
        const hashToken = hmac.digest("hex");
        const token = await this.query("Update users set token=? where name=?", [hashToken, userCorrect[0].name]);
        
        const msg: CommunicationData = {
            "method": "login",
            "result": "success",
            "token": hashToken
        };

        return msg;
            
    }

    public async checkAuthToken(token: string): Promise<{name: string, id: number}> {

        const result: {name: string, id: number}[] = await this.query("Select id, name from users where token=?", [token]);
        
        if (result.length === 0) {
            throw new Error("Auth token wrong");
        }

        return result[0];
    }

    public async getMountById(mountId: number): Promise<Mount> {
        const result = await this.query("Select id, name, maxSpeed, acceleration from mounts where id=?", [mountId]);
        if (result.length === 0) {
            throw new Error("Mount does not exist");
        }

        return new Mount(result[0].id, result[0].name, result[0].maxSpeed, result[0].acceleration);
    }

    public async getWeaponById(weaponId: number): Promise<Weapon> {
        const result = await this.query("Select id, name from weapons where id=?", [weaponId]);
        if(result.length === 0) {
            throw new Error("Weapon does not exist");
        }

        return new Weapon(result[0].id, result[0].name);
    }

    public async createGame(player1: Player, player2: Player, type: string): Promise<Game> {
        const result = await this.query("Insert into gamedata (type) Select id from gametype where name=?", [type]);
        await this.query("Insert into user_game (gameid, playerid, side) Values (?, ?, 0)", [result.insertId, player1.getDatabaseId()]);
        if (player2.getDatabaseId() != -1) {
            await this.query("Insert into user_game (game_id playerid, side) Values (?, ?, 1)", [result.insertId, player2.getDatabaseId()]);
        }
        return new Game(result.insertId, player1, player2);
        
    }

}

export interface CommunicationData {
    method: string;
    result: string;
    [data: string]: any;
}

        