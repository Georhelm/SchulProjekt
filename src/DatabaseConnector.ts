import mysql = require("mysql");
import Crypto = require("crypto");
import {Config} from "./Config";
import {User} from "./User";
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
        const result = await this.query("Select id, name, maxSpeed, acceleration, height from mounts where id=?", [mountId]);
        if (result.length === 0) {
            throw new Error("Mount does not exist");
        }

        return new Mount(result[0].id, result[0].name, result[0].maxSpeed, result[0].acceleration, result[0].height);
    }

    public async getWeaponById(weaponId: number): Promise<Weapon> {
        const result = await this.query("Select id, name, lift_speed, fall_speed from weapons where id=?", [weaponId]);
        if(result.length === 0) {
            throw new Error("Weapon does not exist");
        }

        return new Weapon(result[0].id, result[0].name, result[0].lift_speed, result[0].fall_speed);
    }

    public async getEquippedWeapon(userid: number): Promise<Weapon> {
        const result = await this.query("Select w.id, w.name, w.lift_speed, w.fall_speed from users u join weapons w on u.weaponid = w.id where u.id = ?", [userid]);
        if(result.length === 0) {
            throw new Error("User has not valid weapon");
        }

        return new Weapon(result[0].id, result[0].name, result[0].lift_speed, result[0].fall_speed);
    }

    public async getEquippedMount(userid: number): Promise<Mount> {
        const result = await this.query("Select m.id, m.name, m.maxSpeed, m.acceleration, m.height from users u join mounts m on u.mountid = m.id where u.id = ?", [userid]);
        if(result.length === 0) {
            throw new Error("User has no valid mount");
        }

        return new Mount(result[0].id, result[0].name, result[0].maxSpeed, result[0].acceleration, result[0].height);
    }

    public async getRandomMount(): Promise<Mount> {
        const result = await this.query("Select id, name, maxSpeed, acceleration, height from mounts order by Rand() limit 1", []);
        if(result.length === 0) {
            throw new Error("No Mount found");
        }

        return new Mount(result[0].id, result[0].name, result[0].maxSpeed, result[0].acceleration, result[0].height);
    }

    public async getAllMounts(): Promise<Mount[]> {
        const results = await this.query("Select id, name, maxSpeed, acceleration, height from mounts", []);
        const allMounts: Mount[] = [];

        for(const result of results) {
            allMounts.push(new Mount(result.id, result.name, result.maxSpeed, result.acceleration, result.height));
        }

        return allMounts;
    }

    public async createGame(player1: User, player2: User | null, type: string): Promise<Game> {
        const result = await this.query("Insert into gamedata (type) Select id from gametype where name=?", [type]);
        await this.query("Insert into user_game (gameid, playerid, side) Values (?, ?, 0)", [result.insertId, player1.getDatabaseId()]);
        if (player2 !== null && player2.getDatabaseId() != -1) {
            await this.query("Insert into user_game (gameid, playerid, side) Values (?, ?, 1)", [result.insertId, player2.getDatabaseId()]);
        }
        
        const game = new Game(result.insertId, player1, player2);
        await game.init();
        return game;
        
    }

    public async setGameWinner(gameId: number, wonSide: number) {
        await this.query("Update gamedata set wonside = ? where id = ?", [wonSide, gameId]);
    }

}

export interface CommunicationData {
    method: string;
    result: string;
    [data: string]: any;
}

        