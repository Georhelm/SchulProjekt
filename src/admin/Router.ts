import auth = require("basic-auth");
import {NextFunction, Request, Response, Router} from "express";
import {Dashboard} from "./Dashboard";

export function createAdminRouter(): Router {
    const AdminRouter: Router = Router();

    AdminRouter.get("/", authAdmin, async (req: Request, resp: Response) => {
        resp.status(400).send(Dashboard.getOverview());
    });

    AdminRouter.get("/queue", authAdmin, async (req: Request, resp: Response) => {
        resp.status(400).send(Dashboard.getQueueDetails());
    });

    AdminRouter.get("/online", authAdmin, async (req: Request, resp: Response) => {
        resp.status(400).send(Dashboard.getOnlineDetails());
    });

    AdminRouter.get("/games", authAdmin, async (req: Request, resp: Response) => {
        resp.status(400).send(Dashboard.getGameDetails());
    });

    return AdminRouter;
}

function authAdmin(req: Request, res: Response, next: NextFunction) {
    const credentials = auth(req);

    if (!credentials || credentials.name !== admin.user || credentials.pass !== admin.password) {
        res.statusCode = 401;
        res.setHeader("WWW-Authenticate", "Basic realm=\"example\"");
        res.end("Access denied");
    } else {
        next();
    }
}

const admin = {user: "gameapp", password: "georhelm"};
