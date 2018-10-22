import {Router, Request, Response} from "express";
import {Config} from "./Config";
import {DatabaseConnection} from "./DatabaseConnector";

const config: Config = require("../config.json");
let connection: DatabaseConnection;

export function createGameRouter(con: DatabaseConnection): Router {

    connection = con;

    const GameRouter: Router = Router();

    GameRouter.post("/register", (req: Request, resp: Response) => {
        if (req.body.user && req.body.password) {

            connection.registerUser(req.body.user, req.body.password).then((result) => {
                resp.send(result);
            }, (error) => {
                resp.status(500).send();
            });

        } else {
            resp.status(400).send();
        }
        
    });

    GameRouter.post("/login", (req: Request, resp: Response) => {
        if (req.body.user && req.body.password) {
            connection.loginUser(req.body.user, req.body.password).then((result) => {
                resp.send(result);
            }, (error) => {
                resp.status(500).send();
            });
        } else {
            resp.status(400).send();
        }
    });
    return GameRouter;
}


