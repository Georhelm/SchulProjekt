import  {Player, HitPoint} from "./Player";
import {GameUpdate} from "./Game";
import {DatabaseConnection} from "./DatabaseConnector";

export class Npc extends Player  {

    constructor(position: number, farPlayer: boolean){
        super("NPC", position, farPlayer, -1);
    }

    public async loadEquipment() {
        this.mount = await DatabaseConnection.getDatabaseConnection().getRandomMount();
        this.weapon = await DatabaseConnection.getDatabaseConnection().getWeaponById(1);
    }

    public async init() {
        await this.loadEquipment();
        this.ready = true;
    }

    public reset() {
        super.reset();
        this.ready = true;
    }

    public async sendGameUpdate(update: GameUpdate) {
        
    }

    public async sendMessage(type: string) {
        
    }

    public initGameInputListeners() {
        this.randomChange();
    }

    public endGame(enemy: Player, playerHit: HitPoint, enemyHit: HitPoint) {

    }

    private randomChange() {
        this.isLiftingWeapon = !this.isLiftingWeapon;
        const rand = Math.random() * 1000;
        setTimeout(this.randomChange.bind(this), rand);
    }
}