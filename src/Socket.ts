import Socket =require("socket.io");
import { Server } from "http";

export class GameSocket {
    private socket: Socket.Server;

    constructor(server: Server){
        this.socket = Socket(server);
    }

    public init(): void {
        this.socket.use(this.checkAuth);
        this.socket.on("connect", this.onConnected);
    }

    private checkAuth(socket: Socket.Socket,next: (err?: any) => void): void {
        console.log(socket.handshake.query);
        next();
    }

    private onConnected(client: Socket.Socket) {
        //console.log(client);
    }
}