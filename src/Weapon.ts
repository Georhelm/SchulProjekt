

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
        this.maxAngle = 120;
        this.reset();
    }


    public getId(): number {
        return this.id;
    }

    public updateAngle(timeDelta: number, lifting: boolean){
        if (lifting) {
            const newAngle = this.angle - this.liftSpeed * timeDelta;
            this.angle = Math.max(newAngle, this.minAngle);
        }else {
            const newAngel = this.angle + this.fallSpeed * timeDelta;
            this.angle = Math.min(newAngel, this.maxAngle);
        }
    }

    public reset() {
        this.angle = 90;
    }

    public getAngle(): number {
        return this.angle;
    }
}