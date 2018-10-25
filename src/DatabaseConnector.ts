import mysql = require("mysql");
import Crypto = require("crypto");
import {Config} from "./Config";
import * as Q from "q";

const config: Config = require("../config.json");

export class DatabaseConnection {
    
    private username: string;
    private password: string;
    private server : string;
    private database: string;
    private connectionPool : mysql.Pool;

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
    }

    public registerUser(user: string, pass: string): Q.Promise<CommunicationData> {
        const defer = Q.defer<CommunicationData>();

        this.connectionPool.getConnection((err: mysql.MysqlError, connection: mysql.PoolConnection) => {
            if (err) {
                defer.reject();
            }
            connection.query("Select * from users where name=?", [user], (err, result) => {
                if (err) {
                    connection.release();
                    defer.reject();
                }
    
                if (result.length === 0)  {
                    const hashedPass = Crypto.createHmac("sha256", config.hashkey);
                    hashedPass.update(pass);
                    this.connectionPool.query("Insert into users (name, password) Values (?,?)", [user, hashedPass.digest("hex")], (err, response) => {
                        
                        connection.release();

                        if (err) {
                            defer.reject();
                        }
    
                        const msg: CommunicationData = {
                            "method": "register",
                            "result": "success"
                        };
                        defer.resolve(msg);
                    });
                }else {
                    const msg: CommunicationData = {
                        "method": "register",
                        "result": "error",
                        "msg": "user_exists"
                    }
                    connection.release();
                    defer.resolve(msg);
                }
                
            });

        });
        

        return defer.promise;

    }

    public loginUser(user: string, pass: string): Q.Promise<CommunicationData> {

        const defer = Q.defer<CommunicationData>();

        this.connectionPool.getConnection((err: mysql.MysqlError, connection: mysql.PoolConnection) => {
            if(err) {
                defer.reject();
            }

            const hashedPass = Crypto.createHmac("sha256", config.hashkey);
            hashedPass.update(pass);
    
            connection.query("Select * from users where name=? and password=?", [user, hashedPass.digest("hex")], (err, result) => {
                if (err) {
                    connection.release();
                    defer.reject();
                }
                if (result.length > 0) {
                    const hmac = Crypto.createHmac("sha256", config.hashkey);
                    hmac.update(result[0].name + Date.now());
                    const hashToken = hmac.digest("hex");
                    this.connectionPool.query("Update users set token=? where name=?", [hashToken, result[0].name], (err, result) => {
                        connection.release();
                        if(err) {
                            defer.reject();
                        }
    
                        const msg: CommunicationData = {
                            "method": "login",
                            "result": "success",
                            "token": hashToken
                        }
    
                        defer.resolve(msg);
                    });
    
                    
                }else {
                    const msg: CommunicationData = {
                        "method": "login",
                        "result": "error",
                        "msg": "wrong_login"
                    }
                    connection.release();
                   defer.resolve(msg);
                }
            });
        });

       

        return defer.promise;
            
    }

    public checkAuthToken(token: string): Q.IPromise<{name: string}> {
        const defer = Q.defer<{name: string}>();
        this.connectionPool.getConnection((err: mysql.MysqlError, connection: mysql.PoolConnection) => {
            if(err) {
                defer.reject();
            }

            connection.query("Select * from users where token=?", [token], (err, result) => {
                if(err || result.length === 0) {
                    defer.reject();
                }
    
                defer.resolve(result[0]);
            });
        });
        
        return defer.promise;
    }

}

export interface CommunicationData {
    method: string;
    result: string;
    [data: string]: any;
}

        