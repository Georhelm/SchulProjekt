import {Router, Request, Response, NextFunction} from "express";
import auth = require("basic-auth");
import {Overview} from "./Overview";

export function createAdminRouter(): Router {
    const AdminRouter: Router = Router();

    AdminRouter.get("/", authAdmin, async (req: Request, resp: Response) => {
        resp.status(400).send(Overview.getHtml());
    });
    
    return AdminRouter;
}

function authAdmin(req: Request, res: Response, next: NextFunction) {
    const credentials = auth(req);

    if (!credentials || credentials.name !== admin.user || credentials.pass !== admin.password) {
        res.statusCode = 401
        res.setHeader('WWW-Authenticate', 'Basic realm="example"')
        res.end('Access denied')
    } else {
        next()
    }
}

const admin = {user: "gameapp", password: "georhelm"};