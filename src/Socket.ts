import Socket =require("socket.io");
import {DatabaseConnection} from "./DatabaseConnector";
import { Server } from "http";

export class GameSocket {
    private socket: Socket.Server;
    private connection: DatabaseConnection;

    constructor(server: Server, con: DatabaseConnection){
        this.socket = Socket(server);
        this.connection = con;
    }

    public init(): void {
        this.socket.use(this.checkAuth.bind(this));
        this.socket.on("connect", this.onConnected);
    }

    private checkAuth(socket: Socket.Socket,next: (err?: any) => void): void {
        console.log(socket.handshake.query);
        if (!socket.handshake.query.token) {
            return next(new Error("authentication error"));
        }
    
        this.connection.checkAuthToken(socket.handshake.query.token).then((result) => {
            console.log(result);
            return next();
        }, (rejected) => {
            socket.disconnect();
            return next(new Error("unauthorized"));
        });
    }

    private onConnected(client: Socket.Socket) {
        console.log("connected: " + client.id);
    }
}