import {DatabaseConnection} from "./DatabaseConnector";

export class Mount {

    private id: number;
    private name: string;
    private maxSpeed: number;
    private acceleration: number;

    constructor(id: number, name: string, maxSpeed: number, acceleration: number) {
        this.id = id;
        this.name = name;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
    }

    public getId(): number {
        return this.id;
    }

}