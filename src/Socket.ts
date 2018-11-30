import Socket =require("socket.io");
import {DatabaseConnection} from "./DatabaseConnector";
import { Server } from "http";
import {User} from "./User";
import {Game} from "./Game";
import {Npc} from "./Npc";

export class GameSocket {
    private socket: Socket.Server;
    private connection: DatabaseConnection;
    private gameQueue: User[];

    constructor(server: Server, con: DatabaseConnection){
        this.socket = Socket(server);
        this.connection = con;
        this.socket.clients();
        this.gameQueue = [];
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
            const newPlayer = new User(result.name, result.id, socket);
            return next();
        }catch(error) {
            socket.disconnect();
            return next(new Error("unauthorized"));
        }
    }

    private onConnected(client: Socket.Socket) {
        console.log("new client:");
        const player = User.getPlayerBySocketId(client.id)
        if (player === null) {
            client.disconnect();
            return;
        }
        console.log(player.getLogObj());
        console.log("playercount: " + User.getPlayerCount());
        this.registerEvents(client);
    }

    private onDisconnected(client: Socket.Socket) {
        console.log("client disconnected");
        const player = User.getPlayerBySocketId(client.id)
        if (player === null) {
            return;
        }
        console.log(player.getLogObj());
        Game.endGameContainingPlayer(player);
        User.removePlayerBySocketId(client.id);
        console.log("playercount: " + User.getPlayerCount());
    }

    private async startSinglePlayer(client: Socket.Socket)  {
        console.log("Singleplayer game started");
        const player = User.getPlayerBySocketId(client.id)
        if (player === null)  {
            client.disconnect();
            return;
        }
        console.log(player.getLogObj());
        try {
            const game: Game = await DatabaseConnection.getDatabaseConnection().createGame(player, null, "singleplayer");
            console.log(game.getLogObj());
        }catch(error) {
            client.disconnect();
            return;
        }
    }

    private async startMultiplayer(client: Socket.Socket) {     
        const player = User.getPlayerBySocketId(client.id)
        console.log("Multiplayer search started");       
        if (player === null)  {
            client.disconnect();
            return;
        }
        if(this.gameQueue.length > 0) {
            try {
                const enemy = this.gameQueue.pop();
                if(enemy === undefined) {
                    return;
                }
                const game: Game = await DatabaseConnection.getDatabaseConnection().createGame(player, enemy, "multiplayer");
                console.log(game.getLogObj());
            }catch(error) {
                client.disconnect();
                return;
            }
        }else {
            this.gameQueue.push(player);
            client.emit("searchingForEnemy");
        }
        
    }

    private async getPlayerEquipment(client: Socket.Socket) {
        const player = User.getPlayerBySocketId(client.id);
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