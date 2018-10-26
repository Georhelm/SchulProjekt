import {Player, PlayerGameData} from "./Player";

export class Game {

    private id: number;
    private player1: Player;
    private player2: Player;
    private gameWidth: number;
    private timeOfLastUpdate: number;

    constructor(id: number, player1: Player, player2: Player) {
        this.id = id;
        this.player1 = player1;
        this.player2 = player2;
        this.gameWidth = 1000;
    }

    public getStartGameState(): FullGameState {
        const result: FullGameState = {
            player1: this.player1.getGameData(),
            player2: this.player2.getGameData(),
            gameWidth: this.gameWidth
        }

        return result;
    }

    public async startGameCountdown() {
        this.doCountdown(3);
        console.log(Date.now());
        this.timeOfLastUpdate = Date.now();
    }

    private async doCountdown(time: number) {
        const newTime = Date.now();
        console.log("newTime: " + newTime);
        console.log("timesince last update= " + (newTime - this.timeOfLastUpdate));
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

    private async startGame() {

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

export interface GameUpdate {
    type: string,
    value: any
}