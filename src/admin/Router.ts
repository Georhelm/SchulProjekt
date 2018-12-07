import auth = require("basic-auth");
import {NextFunction, Request, Response, Router} from "express";
import {Dashboard} from "./Dashboard";

export function createAdminRouter(): Router {
    const AdminRouter: Router = Router();

    /**
     * adds middleware to require authentication to access admin panels
     */
    AdminRouter.use(async (req: Request, resp: Response, next: NextFunction) => {
        authAdmin(req, resp, next);
    });

    /**
     * adds the middleware for the html head
     */
    AdminRouter.use(async (req: Request, resp: Response, next: NextFunction) => {
        resp.write(Dashboard.getHead());
        next();
    });

    /**
     * adds route for overview
     */
    AdminRouter.get("/", async (req: Request, resp: Response) => {
        resp.status(400).end(Dashboard.getOverview());
    });

    /**
     * adds route for the queue overview
     */
    AdminRouter.get("/queue", async (req: Request, resp: Response) => {
        resp.status(400).end(Dashboard.getQueueDetails());
    });

    /**
     * adds route for the online users overview
     */
    AdminRouter.get("/online", async (req: Request, resp: Response) => {
        resp.status(400).end(Dashboard.getOnlineDetails());
    });

    /**
     * adds route for the running games overview
     */
    AdminRouter.get("/games", async (req: Request, resp: Response) => {
        resp.status(400).end(Dashboard.getGameDetails());
    });

    /**
     * adds redirect for all invalid routes to overview
     */
    AdminRouter.get("/*", authAdmin, async (req: Request, resp: Response) => {
        resp.redirect("/");
    });

    return AdminRouter;
}

/**
 * authentication middleware to be used in all requests to /admin/*
 */
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
