import {DatabaseConnection} from "./DatabaseConnector";
import {IGameUpdate} from "./Game";
import {HitPoint, Player} from "./Player";

export class Npc extends Player  {

//#region properties

    private isRunning: boolean;

//#endregion properties

//#region constructor

    /**
     * creates a player object with npc data
     */
    constructor(position: number, farPlayer: boolean) {
        super("NPC", position, farPlayer, -1);
        this.isRunning = true;
    }

//#endregion constructor

//#region public async methods

    /**
     * overrides the player loadEquipment method
     * loads random equipment for the npc
     */
    public async loadEquipment() {
        this.mount = await DatabaseConnection.getDatabaseConnection().getRandomMount();
        this.weapon = await DatabaseConnection.getDatabaseConnection().getWeaponById(1);
    }

    /**
     * overrides the player init method
     * loads the npcs equipment and sets its ready state
     */
    public async init() {
        await this.loadEquipment();
        this.ready = true;
    }

    /**
     * overrides the players sendGameUpdate method to not do anything
     */
    public async sendGameUpdate(update: IGameUpdate) {
        return;
    }

    /**
     * overrides the players sendMessage method to not do anything
     */
    public async sendMessage(type: string) {
        return;
    }

//#endregion public async methods

//#region public methods

    /**
     * extends the players reset method
     * sets the npc to be ready
     */
    public reset() {
        super.reset();
        this.ready = true;
    }

    /**
     * overrides the players initGameInputListeners method
     * initializes the npcs random movement
     */
    public initGameInputListeners() {
        this.randomChange();
    }

    /**
     * overrides the players endGame method
     * clears the randomChange timer
     */
    public endGame(enemy: Player, playerHit: HitPoint, enemyHit: HitPoint, victory: boolean) {
        this.isRunning = false;
    }

//#endregion public methods

//#region private methods

    /**
     * changes the lifting of the weapon at a random interval
     */
    private randomChange() {
        this.isLiftingWeapon = !this.isLiftingWeapon;
        const rand = Math.random() * 1000;
        if (this.isRunning) {
            setTimeout(this.randomChange.bind(this), rand);
        }
    }

//#endregion private methods

}
