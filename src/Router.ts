import {Router, Request, Response} from "express";
import Crypto = require("crypto");
import mysql = require("mysql");
import {Config} from "./Config";

const config: Config = require("../config.json");

export const GameRouter: Router = Router();

const conntectionInfo = {
    host: config.serverName,
    database: config.database,
    user: config.username,
    password: config.password,

};

GameRouter.post("/register", (req: Request, resp: Response) => {
    if (req.body.user && req.body.password) {

        const con: mysql.Connection = mysql.createConnection(conntectionInfo);

        con.connect((err: mysql.MysqlError) => {
            if (err) {
                console.error("Error on connection to database " + err.message);
                return;
            }

            con.query("Select * from users where name=?", [req.body.user], (err, result) => {
                if (err) {
                    PrintQueryError(err, resp);
                    return;
                }

                if (result.length === 0)  {
                    const hashedPass = Crypto.createHmac("sha256", config.hashkey);
                    hashedPass.update(req.body.password);
                    con.query("Insert into users (name, password) Values (?,?)", [req.body.user, hashedPass.digest("hex")], (err, response) => {
                        if (err) {
                            PrintQueryError(err, resp);
                            return;
                        }

                        const msg = {
                            "result": "success",
                        };

                        resp.send(msg);
                    });
                }else {
                    const msg = {
                        "result": "error",
                        "msg": "user_exists"
                    }
                    resp.send(msg);
                }

            });

        });
    } else {
        resp.status(400).send();
    }
    
});

GameRouter.post("/login", (req: Request, resp: Response) => {
    if (req.body.user && req.body.password) {
        const con = mysql.createConnection(conntectionInfo);

        con.connect((err: mysql.MysqlError) => {
            const hashedPass = Crypto.createHmac("sha256", config.hashkey);
            hashedPass.update(req.body.password);

            con.query("Select * from users where name=? and password=?", [req.body.user, hashedPass.digest("hex")], (err, result) => {
                if (err) {
                    PrintQueryError(err, resp);
                    return;
                }
                if (result.length > 0) {
                    const hmac = Crypto.createHmac("sha256", config.hashkey);
                    hmac.update(result[0].name + Date.now());
                    const hashToken = hmac.digest("hex");
                    con.query("Update users set token=SHA(?) where name=?", [hashToken, result[0].name], (err, result) => {
                        if(err) {
                            PrintQueryError(err, resp);
                            return;
                        }

                        const msg = {
                            "result": "success",
                            "token": hashToken
                        }
    
                        resp.send(msg);
                    });

                    
                }else {
                    const msg = {
                        "result": "error",
                        "msg": "wrong_login"
                    }

                    resp.send(msg);
                }
            });
        });
    } else {
        resp.status(400).send();
    }
});

function PrintQueryError(err: mysql.MysqlError, resp: Response) {
    console.error("Error on querying database " + err.message);
    resp.status(500).send();
}