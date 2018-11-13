import  {Player} from "./Player";
import {GameUpdate} from "./Game";
import {Socket} from "socket.io";

export class Npc extends Player  {

    constructor(position: number, farPlayer: boolean){
        super("NPC", position, farPlayer);
    }

    public async init() {
        await this.loadEquipment();
        this.ready = true;
    }

    public async sendGameUpdate(update: GameUpdate) {
        //console.log("NPC recieved gameupdate");
    }

    public initGameInputListeners() {
        this.randomChange();
    }

    private randomChange() {
        this.isLiftingWeapon = !this.isLiftingWeapon;
        const rand = Math.random() * 1000;
        setTimeout(this.randomChange.bind(this), rand);
    }
}