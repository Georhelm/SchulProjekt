import Socket =require("socket.io");
import {DatabaseConnection} from "./DatabaseConnector";
import { Server } from "http";
import { SocketConstructorOpts } from "net";

export class GameSocket {
    private socket: Socket.Server;
    private connection: DatabaseConnection;

    constructor(server: Server, con: DatabaseConnection){
        this.socket = Socket(server);
        this.connection = con;
        this.socket.clients();
    }

    public init(): void {
        this.socket.use(this.checkAuth.bind(this));
        this.socket.on("connect", this.onConnected);
        this.socket.on("disconnect", this.onDisconnected);
    }

    private checkAuth(socket: Socket.Socket,next: (err?: any) => void): void {
        if (!socket.handshake.query.token) {
            return next(new Error("authentication error"));
        }
    
        this.connection.checkAuthToken(socket.handshake.query.token).then((result) => {
            return next();
        }, (rejected) => {
            socket.disconnect();
            return next(new Error("unauthorized"));
        });
    }

    private onConnected(client: Socket.Socket) {
        console.log("new client:");
        console.log(this.socket.clients().sockets);
    }

    private onDisconnected(client: Socket.Socket) {
        console.log("client disconnected");
        console.log(this.socket.clients().sockets);
    }


}