import Socket = require("socket.io");
import {NextFunction} from "express";
import {DatabaseConnection} from "./DatabaseConnector";
import { Server } from "http";
import {User} from "./User";
import {Game} from "./Game";

export class GameSocket {

//#region properties

    private socket: Socket.Server;

//#endregion properties

//#region static properties

    private static gameQueue: User[];

//#endregion static properties

//#region constructor

    /**
     * creates a new gamesocket
     * @param server the http server to listen on
     */
    constructor(server: Server){
        this.socket = Socket(server);
        if(GameSocket.gameQueue === undefined) {
            GameSocket.gameQueue = [];
        }
    }

//#endregion constructor

//#region public methods

    /**
     * initializes the socket with authentication
     */
    public init() {
        this.socket.use(this.checkAuth.bind(this));
        this.socket.on("connect", this.onConnected.bind(this));
    }

//#endregion public methods

//#region private async methods

    /**
     * checks the authentication token of the connecting socket
     * creates a new user if that token is correct
     * disconnects the socket if no correct token was sent
     * @param socket the connecting socket
     * @param next the next middleware to be executed
     * @returns a promise that resolves after all middlewares are done
     */
    private async checkAuth(socket: Socket.Socket, next: NextFunction) {
        if (!socket.handshake.query.token) {
            return next(new Error("authentication error"));
        }
    
        try {
            const result = await DatabaseConnection.getDatabaseConnection().checkAuthToken(socket.handshake.query.token);
            new User(result.name, result.id, socket);
            return next();
        }catch(error) {
            socket.disconnect();
            return next(new Error("unauthorized"));
        }
    }


    /**
     * checks if the user is authenticated
     * registers socket events
     * @param client the connecting socket
     */
    private async onConnected(client: Socket.Socket) {
        const player = User.getUserBySocketId(client.id)
        if (player === null) {
            client.disconnect();
            return;
        }
        this.registerEvents(client);
    }

    /**
     * checks if the user authenticated
     * ends all games containing that user 
     * removes the user from the list of online users
     * @param client the connecting client
     */
    private async onDisconnected(client: Socket.Socket) {
        const player = User.getUserBySocketId(client.id)
        if (player === null) {
            return;
        }
        Game.endGamesContainingUser(player);
        const playerIndex = GameSocket.gameQueue.indexOf(player);
        if(playerIndex > -1) {
            GameSocket.gameQueue.splice(playerIndex, 1);
        }
        User.removeUserBySocketId(client.id);
    }

    /**
     * checks if the users is authenticated and not already in a game
     * then starts a singleplayer game for the user
     * @param client the connecting client
     */
    private async startSinglePlayer(client: Socket.Socket)  {
        const player = User.getUserBySocketId(client.id)
        if (player === null)  {
            client.disconnect();
            return;
        }
        if(Game.isUserInGame(player)){
            return;
        }
        try {
            await DatabaseConnection.getDatabaseConnection().createGame(player, null, "singleplayer");
        }catch(error) {
            client.disconnect();
            return;
        }
    }

    /**
     * checks if the user is authenticated and not already in a game
     * then adds the user to the gamequeue or starts a game with another user from the gamequeue
     * @param client the connecting client
     */
    private async startMultiplayer(client: Socket.Socket) {     
        const player = User.getUserBySocketId(client.id);    
        if (player === null)  {
            client.disconnect();
            return;
        }
        if(Game.isUserInGame(player)){
            return;
        }
        if(GameSocket.gameQueue.length > 0) {
            try {
                const enemy = GameSocket.gameQueue.pop();
                if(enemy === undefined) {
                    return;
                }
                await DatabaseConnection.getDatabaseConnection().createGame(player, enemy, "multiplayer");
            }catch(error) {
                client.disconnect();
                return;
            }
        }else {
            GameSocket.gameQueue.push(player);
            client.emit("searching_multiplayer");
            client.once("cancel_search", this.cancelSearch.bind(this, client));
        }
    }

    /**
     * checks if the user is authenticated
     * removes the user from the gamequeue
     * @param client the connecting client
     */
    private async cancelSearch(client: Socket.Socket) {
        const player = User.getUserBySocketId(client.id);
        if(player === null) {
            client.disconnect();
            return;
        }
        const index = GameSocket.gameQueue.indexOf(player);
        if(index > -1) {
            GameSocket.gameQueue.splice(index, 1);
        }
    }

    /**
     * checks if the user is authenticated
     * retrieves all available and the selected equipment from the database
     * returns it through the acknowledge function
     * @param client the connecting client
     * @param ack the acknowledge function
     */
    private async getPlayerEquipment(client: Socket.Socket, ack: (equipment: any) => void) {
        const player = User.getUserBySocketId(client.id);
        if (player === null)  {
            client.disconnect();
            return;
        }
        const mounts = await DatabaseConnection.getDatabaseConnection().getAllMounts();
        const selectedMount = await DatabaseConnection.getDatabaseConnection().getEquippedMount(player.getDatabaseId());
        const message = {
            mounts,
            selectedMount: selectedMount.getId()
        }
        ack(message);
    }

    /**
     * checks if the user is authenticated
     * sets the users equipment
     * @param client the connecting client
     * @param data the equipment for the player
     */
    private async setEquipment(client: Socket.Socket, data: EquipmentData) {
        const player = User.getUserBySocketId(client.id);
        if (player === null)  {
            client.disconnect();
            return;
        }
        try{
            DatabaseConnection.getDatabaseConnection().setMountOfUser(data.mountId, player.getDatabaseId());
        }catch {
            console.error("Could not set equipment");
        }
    }

    /**
     * checks if the user is authenticated
     * ends all games containing that user
     * @param client the connecting client
     */
    private async leaveGame(client: Socket.Socket) {
        const player = User.getUserBySocketId(client.id)
        if (player === null) {
            return;
        }
        Game.endGamesContainingUser(player);
    }

    /**
     * checks if the user is authenticated
     * returns the number of wins that user has through the acknowledge function
     * @param client the connecting client
     * @param ack the acknowledge function
     */
    private async getWins(client: Socket.Socket, ack: (wins: number) => void) {
        const player = User.getUserBySocketId(client.id);
        if(player === null) {
            client.disconnect();
            return;
        }
        const wins = await DatabaseConnection.getDatabaseConnection().getUserWins(player.getDatabaseId());
        ack(wins);
    }

    /**
     * registers all events to a socket
     * @param socket the socket to register the events to
     */
    private async registerEvents(socket: Socket.Socket) {
        socket.on("disconnect", this.onDisconnected.bind(this, socket));
        socket.on("start_singleplayer", this.startSinglePlayer.bind(this, socket));
        socket.on("start_multiplayer", this.startMultiplayer.bind(this, socket));
        socket.on("get_equipment", this.getPlayerEquipment.bind(this, socket));
        socket.on("set_equipment", this.setEquipment.bind(this, socket));
        socket.on("leave_game", this.leaveGame.bind(this, socket));
        socket.on("get_wins", this.getWins.bind(this, socket));
    }

//#endregion private async methods

    
//#region public static methods

    /**
     * get the amount of people queuing
     * @returns amount of people in queue
     */
    public static getQueueLength(): number {
        return GameSocket.gameQueue.length;
    }

//#endregion public static methods

}

//#region interfaces

/**
 * interface for equipmentdata
 */
interface EquipmentData {
    mountId: number;
}

//#endregion interfaces