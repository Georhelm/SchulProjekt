import {Request, Response, Router} from "express";
import {DatabaseConnection} from "./DatabaseConnector";

/**
 * creates a router for authenticating to the server
 * @returns the router
 */
export function createGameRouter(): Router {

    const GameRouter: Router = Router();

    /**
     * route that matches all post requests to '/register'
     * registers a new user if none with that name exist
     * @param route route to listen to
     * @param handler handler for that route
     */
    GameRouter.post("/register", async (req: Request, resp: Response) => {
        if (req.body.user && req.body.password) {

            try {
                const result = await DatabaseConnection.getDatabaseConnection().registerUser(req.body.user, req.body.password);
                resp.send(result);
            } catch (error) {
                resp.status(500).send();
            }

        } else {
            resp.status(400).send();
        }
    });

    /**
     * route thaz matches all post requests to '/login'
     * creates an authentication token for the user if login data is correct
     * @param route route to listen to
     * @param handler handler for that route
     */
    GameRouter.post("/login", async (req: Request, resp: Response) => {
        if (req.body.user && req.body.password) {
            try {
                const result = await DatabaseConnection.getDatabaseConnection().loginUser(req.body.user, req.body.password);
                resp.send(result);
            } catch (error) {
                resp.status(500).send();
            }
        } else {
            resp.status(400).send();
        }
    });
    return GameRouter;
}
