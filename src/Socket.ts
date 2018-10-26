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

    private async checkAuth(socket: Socket.Socket,next: (err?: any) => void) {
        if (!socket.handshake.query.token) {
            return next(new Error("authentication error"));
        }
    
        try {
            const result = await DatabaseConnection.getDatabaseConnection().checkAuthToken(socket.handshake.query.token);
            const newPlayer = new Player(result.name, result.id);
            await newPlayer.init(socket);
            return next();
        }catch(error) {
            socket.disconnect();
            return next(new Error("unauthorized"));
        }
    }

    private onConnected(client: Socket.Socket) {
        console.log("new client:");
        const player = Player.getPlayerBySocketId(client.id)
        if (player === null) {
            client.disconnect();
            return;
        }
        console.log(player.getLogObj());
        console.log("playercount: " + Player.getPlayerCount());
        this.registerEvents(client);
    }

    private onDisconnected(client: Socket.Socket) {
        console.log("client disconnected");
        console.log(Player.getPlayerBySocketId(client.id));
        Player.removePlayerBySocketId(client.id);
        console.log("playercount: " + Player.getPlayerCount());
    }

    private async startSinglePlayer(client: Socket.Socket)  {
        console.log("Singleplayer game started");
        const player = Player.getPlayerBySocketId(client.id)
        if (player === null)  {
            client.disconnect();
            return;
        }
        console.log(player.getLogObj());
        try {
            const npc = new Npc();
            await npc.init();
            const game = await DatabaseConnection.getDatabaseConnection().createGame(player, npc, "singleplayer");
            console.log(game.getLogObj());
            client.emit("found_game", game.getStartGameState()); 
            game.startGameCountdown();
        }catch(error) {
            client.disconnect();
            return;
        }
    }

    private async startMultiplayer(client: Socket.Socket) {     
        const player = Player.getPlayerBySocketId(client.id)
        console.log("Multiplayer search started");       
        if (player === null)  {
            client.disconnect();
            return;
        }
        console.log(player.getLogObj());
    }

    private async getPlayerEquipment(client: Socket.Socket) {
        const player = Player.getPlayerBySocketId(client.id);
        console.log("Getting equipment"); 
        if (player === null)  {
            client.disconnect();
            return;
        }
        console.log(player.getLogObj());
    }

    private registerEvents(socket: Socket.Socket) {
        socket.on("disconnect", this.onDisconnected.bind(this, socket));;
        socket.on("start_singleplayer", this.startSinglePlayer.bind(this, socket));
        socket.on("start_multiplayer", this.startMultiplayer.bind(this, socket));
        socket.on("get_equipment", this.getPlayerEquipment.bind(this, socket));
    }


}