import {Player, PlayerGameData} from "./Player";

export class Game {

    private player1: Player;
    private player2: Player;
    private gameWidth: number;

    constructor(player1: Player, player2: Player) {
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
}

export interface FullGameState {
    player1: PlayerGameData,
    player2: PlayerGameData,
    gameWidth: number
}