import {User} from "./User";
import { Player, PlayerGameData, PlayerGameUpdate, HitPoint} from "./Player";
import { Npc } from "./Npc";

export class Game {

    private id: number;
    private user1: User;
    private user2: User | null;
    private player1: Player;
    private player2: Player;
    private gameWidth: number;
    private timeOfLastUpdate: number;
    private gameStartTime: number;

    constructor(id: number, player1: User, player2: User | null) {
        this.id = id;
        this.user1 = player1;
        this.user2 = player2;
        this.gameWidth = 10000;
    }

    public async init() {
        this.player1 = await this.user1.startGame(0, this.playerReady.bind(this), false);
        if (this.user2 === null) {
            this.player2 = new Npc(this.gameWidth, true);
            await (<Npc>this.player2).init();
        }else {
            this.player2 = await this.user2.startGame(this.gameWidth, this.playerReady.bind(this), true);
        }
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
            if (this.player1.getHitpoints() <= 0 || this.player2.getHitpoints() <= 0){
                this.endGame();
            }else {
                this.endRound();
            }
            return;
        }

        this.player1.update(timeDelta);
        this.player2.update(timeDelta);

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

    private endRound() {
        const player1Hit = this.getPointHit(this.player1.getWeaponHeight(), this.player2);
        const player2Hit = this.getPointHit(this.player2.getWeaponHeight(), this.player1);

        this.player1.endRound(player1Hit, player2Hit, this.player2);
        this.player2.endRound(player2Hit, player1Hit, this.player1);
        console.log("round ended");
    }

    private endGame() {
        this.player1.endGame(this.player2.getSpeed());
        this.player2.endGame(this.player2.getSpeed());
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

    private getPointHit(weaponHeight: number, enemy: Player): HitPoint {
        let pointHit = HitPoint.Missed;
        if(weaponHeight >= enemy.getMount().getHeight() + 75 && weaponHeight < enemy.getMount().getHeight() + 175){
            pointHit = HitPoint.Head;
            enemy.setHitpoints(enemy.getHitpoints() - 50);
        }else if(weaponHeight < enemy.getMount().getHeight() + 75 && weaponHeight > enemy.getMount().getHeight() - 25) {
            pointHit = HitPoint.Body;
            enemy.setHitpoints(enemy.getHitpoints() - 25);
        }
        return pointHit;
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