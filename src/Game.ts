import {Player, PlayerGameData, PlayerGameUpdate} from "./Player";

export class Game {

    private id: number;
    private player1: Player;
    private player2: Player;
    private gameWidth: number;
    private timeOfLastUpdate: number;
    private gameStartTime: number;

    constructor(id: number, player1: Player, player2: Player) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.gameWidth = 10000;

        this.player1.setPlayerReadyListener(this.playerReady.bind(this));
        this.player2.setPlayerReadyListener(this.playerReady.bind(this));
        this.player1.startGame(0);
        this.player2.startGame(this.gameWidth);
    }

    public getFullGameState(): FullGameState {
        const result: FullGameState = {
            player1: this.player1.getGameData(),
            player2: this.player2.getGameData(),
            gameWidth: this.gameWidth
        }

        return result;
    }

    
    private playerReady() {
        console.log("player ready");
        if (this.player1.isReady() && this.player2.isReady()) {
            this.startGameCountdown();
        }
    }

    private async startGameCountdown() {
        this.gameStartTime = Date.now();
        console.log("game started");
        this.timeOfLastUpdate = Date.now();
        this.player1.initGameInputListeners();
        this.player2.initGameInputListeners();
        this.doCountdown(3);    
    }

    private async doCountdown(time: number) {
        const newTime = Date.now();
        this.timeOfLastUpdate = newTime;
        const update: GameUpdate = {
            "type": "countdown",
            "value": time
        }
        this.player1.sendGameUpdate(update);
        this.player2.sendGameUpdate(update);
        if (time > 0) {
            setTimeout(() => {
                this.doCountdown(--time);
            }, 1000);
        }else {
            this.startGame();
        }
    }

    private getGameUpdate(): UpdatedGameState {
        return {
            player1: this.player1.getUpdatedGameState(),
            player2: this.player2.getUpdatedGameState()
        }
    }

    private async startGame() {
        this.updateGame();
    }

    private updateGame() {
        const newTime = Date.now();
        const timeDelta = (newTime - this.timeOfLastUpdate) / 1000;
        this.timeOfLastUpdate = newTime;
        if (this.player1.getPosition() >= this.player2.getPosition()){
            this.endGame();
            return;
        }

        this.player1.mount.accelerate(timeDelta);
        this.player2.mount.accelerate(timeDelta);

        this.player1.updatePosition(timeDelta);
        this.player2.updatePosition(-timeDelta);

        this.player1.updateWeaponPosition(timeDelta);
        this.player2.updateWeaponPosition(timeDelta);

        const gameUpdate: GameUpdate = {
            type: "partialUpdate",
            value: this.getGameUpdate()
        }

        this.player1.sendGameUpdate(gameUpdate);
        this.player2.sendGameUpdate(gameUpdate);

        setTimeout(() => {
            this.updateGame();
        }, 33);
    }

    private endGame() {
        const endGameUpdate: GameUpdate = {
            type: "gameEnd",
            value: ""
        }
        this.player1.sendGameUpdate(endGameUpdate);
        this.player2.sendGameUpdate(endGameUpdate);
        console.log("game ended");
    }

    public getLogObj(): any {
        const logObj = {
            id: this.id,
            player1: this.player1.getLogObj(),
            player2: this.player2.getLogObj(),
            width: this.gameWidth
        };
        return logObj;
    }
}

export interface FullGameState {
    player1: PlayerGameData,
    player2: PlayerGameData,
    gameWidth: number
}

export interface UpdatedGameState {
    player1: PlayerGameUpdate,
    player2: PlayerGameUpdate
}

export interface GameUpdate {
    type: string,
    value: any
}