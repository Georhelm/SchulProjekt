import { Constants } from "./Constants";


export class Weapon {

//#region properties
    private id: number;
    private name: string;
    private liftSpeed: number;
    private fallSpeed: number;
    private minAngle: number;
    private maxAngle: number;
    private angle: number; // 0° vertical pointing up 90° horizontal front of player
//#endregion private properties

//#region contructor

    /**
     * Model for weapons
     * @param id databaseid of the weapon
     * @param name name of the weapon
     * @param liftSpeed speed at which the lance gets lifted
     * @param fallSpeed speed at which the lance drops
     */
    constructor(id: number, name: string, liftSpeed: number, fallSpeed: number) {
        this.id = id;
        this.name = name;
        this.liftSpeed = liftSpeed;
        this.fallSpeed = fallSpeed;
        this.minAngle = 0;
        this.maxAngle = 120;
        this.reset();
    }

//#endregion constructor

//#region getter

    /**
     * gets the databaseid of the weapon
     * @returns the databaseid
     */
    public getId(): number {
        return this.id;
    }

    /**
     * gets the current height of the weapons tip relative to the 0° position
     * @returns tip height
     */
    public getHeight(): number {
        return Math.cos(this.getAngle() * Math.PI / 180) * Constants.LANCELENGTH;
    }

    /**
     * gets the current angle of the weapon
     * @returns the angle 
     */
    public getAngle(): number {
        return this.angle;
    }

//#endregion getter

//#region public methods

    /**
     * Updates the weapon angle
     * @param timeDelta the time since the last update
     * @param lifting if the player is currently lifting the weapon
     */
    public updateAngle(timeDelta: number, lifting: boolean){
        if (lifting) {
            const newAngle = this.angle - this.liftSpeed * timeDelta;
            this.angle = Math.max(newAngle, this.minAngle);
        }else {
            const newAngel = this.angle + this.fallSpeed * timeDelta;
            this.angle = Math.min(newAngel, this.maxAngle);
        }
    }

    /**
     * resets the wepon to default position
     */
    public reset() {
        this.angle = 90;
    }
    
//#endregion public methods
}