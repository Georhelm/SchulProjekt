import {DatabaseConnection} from "./DatabaseConnector";

export class Mount {

    private id: number;
    private name: string;
    private maxSpeed: number;
    private acceleration: number;
    private speed: number;

    constructor(id: number, name: string, maxSpeed: number, acceleration: number) {
        this.id = id;
        this.name = name;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.speed = 0;
    }

    public accelerate(delta: number) {
        this.speed = this.speed + this.acceleration * delta;
        if (this.speed > this.maxSpeed) {
            this.speed = this.maxSpeed;
        }
    }

    public getSpeed(){
        return this.speed;
    }

    public getId(): number {
        return this.id;
    }

}