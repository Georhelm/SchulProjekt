import {Router, Request, Response} from "express";
import {Config} from "./Config";
import {DatabaseConnection} from "./DatabaseConnector";

const config: Config = require("../config.json");

export function createGameRouter(): Router {

    const GameRouter: Router = Router();

    GameRouter.post("/register", async (req: Request, resp: Response) => {
        if (req.body.user && req.body.password) {

            try {
                const result = await DatabaseConnection.getDatabaseConnection().registerUser(req.body.user, req.body.password);
                resp.send(result);
            }catch(error) {
                resp.status(500).send();
            }

        } else {
            resp.status(400).send();
        }
        
    });

    GameRouter.post("/login", async (req: Request, resp: Response) => {
        if (req.body.user && req.body.password) {
            try {
                const result = await DatabaseConnection.getDatabaseConnection().loginUser(req.body.user, req.body.password);
                resp.send(result);
            }catch(error){
                resp.status(500).send();
            }
        } else {
            resp.status(400).send();
        }
    });
    return GameRouter;
}


