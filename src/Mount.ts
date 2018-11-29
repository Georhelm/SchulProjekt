import {DatabaseConnection} from "./DatabaseConnector";

export class Mount {

    private id: number;
    private name: string;
    private maxSpeed: number;
    private acceleration: number;
    private speed: number;
    private height: number;

    constructor(id: number, name: string, maxSpeed: number, acceleration: number, height: number) {
        this.id = id;
        this.name = name;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.height = height;
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

    public reset() {
        this.speed = 0;
    }

    public getId(): number {
        return this.id;
    }

    public getHeight(): number {
        return this.height;
    }

}