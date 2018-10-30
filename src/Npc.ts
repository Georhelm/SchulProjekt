import  {Player} from "./Player";
import {GameUpdate} from "./Game";

export class Npc extends Player  {

    constructor(){
        super("NPC");
    }

    public async init() {
        await this.loadEquipment();
    }

    public async sendGameUpdate(update: GameUpdate) {
        console.log("NPC recieved gameupdate");
    }

    public setPlayerReadyListener(fn: () => void) {
        this.ready = true;
    }
}