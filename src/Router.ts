import {Router, Request, Response} from "express";
import mysql = require("mysql");
import {Config} from "./Config";

const config: Config = require("../config.json");

export const GameRouter: Router = Router();

GameRouter.post("/register", (req: Request, resp: Response) => {
    const con: mysql.Connection = mysql.createConnection({
        host: config.serverName,
        database: config.database,
        user: config.username,
        password: config.password,
        
    });

    console.log(config);

    con.connect((err: mysql.MysqlError) => {
        if (err) {
            console.error("Error on connection to database " + err.message);
            return;
        }

        con.query("Select * from users", (err, result) => {
            if (err) {
                console.error("Error on querying database " + err.message);
                return;
            }

            resp.send(result);
        });

    });
    
});