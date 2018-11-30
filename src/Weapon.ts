

export class Weapon {

    public static LANCELENGTH = 467;//530;
    private id: number;
    private name: string;
    private liftSpeed: number;
    private fallSpeed: number;
    private minAngle: number;
    private maxAngle: number;
    private angle: number; // 0° vertical pointing up 90° horizontal front of player

    constructor(id: number, name: string, liftSpeed: number, fallSpeed: number) {
        this.id = id;
        this.name = name;
        this.liftSpeed = liftSpeed;
        this.fallSpeed = fallSpeed;
        this.minAngle = 0;
        this.maxAngle = 180;
        this.reset();
    }


    public getId(): number {
        return this.id;
    }

    public updateAngle(timeDelta: number, lifting: boolean){
        if (lifting) {
            const newAngle = this.angle - this.liftSpeed * timeDelta;
            if (newAngle < this.minAngle) {
                this.angle = this.minAngle
            }else {
                this.angle = newAngle;
            }
        }else {
            const newAngel = this.angle + this.fallSpeed * timeDelta;
            if(newAngel > this.maxAngle) {
                this.angle = this.maxAngle;
            }else {
                this.angle = newAngel;
            }
        }
    }

    public reset() {
        this.angle = 90;
    }

    public getAngle(): number {
        return this.angle;
    }
}