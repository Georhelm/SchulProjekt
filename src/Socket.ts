import Socket =require("socket.io");
import {DatabaseConnection} from "./DatabaseConnector";
import { Server } from "http";
import {Player} from "./Player";
import {Game} from "./Game";
import {Npc} from "./Npc";

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
        this.socket.on("connect", this.onConnected.bind(this));
    }

    private checkAuth(socket: Socket.Socket,next: (err?: any) => void): void {
        if (!socket.handshake.query.token) {
            return next(new Error("authentication error"));
        }
    
        this.connection.checkAuthToken(socket.handshake.query.token).then((result) => {
            const newPlayer = new Player(socket.id, result.name);
            return next();
        }, () => {
            socket.disconnect();
            return next(new Error("unauthorized"));
        });
    }

    private onConnected(client: Socket.Socket) {
        console.log("new client:");
        console.log(Player.getPlayerById(client.id));
        console.log("playercount: " + Player.getPlayerCount());
        this.registerEvents(client);
    }

    private onDisconnected(client: Socket.Socket) {
        console.log("client disconnected");
        console.log(Player.getPlayerById(client.id));
        Player.removePlayerById(client.id);
        console.log("playercount: " + Player.getPlayerCount());
    }

    private startSinglePlayer(client: Socket.Socket)  {
        console.log("Singleplayer game started");
        const player = Player.getPlayerById(client.id)
        if (player === null)  {
            client.disconnect();
            return;
        }
        const game = new Game(player, new Npc())
        client.emit("found_game", game.getStartGameState()); 
    }

    private registerEvents(socket: Socket.Socket) {
        socket.on("disconnect", this.onDisconnected.bind(this, socket));;
        socket.on("start_singleplayer", this.startSinglePlayer.bind(this, socket));
    }


}