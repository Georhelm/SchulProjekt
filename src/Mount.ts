
export class Mount {

//#region properties

    private id: number;
    private name: string;
    private maxSpeed: number;
    private acceleration: number;
    private speed: number;
    private height: number;

//#endregion properties

//#region constructor

    /**
     * creates a mount
     * @param id databaseId of the mount
     * @param name name of the mount
     * @param maxSpeed the maximum speed the mount can reach
     * @param acceleration the acceleration of the mount
     * @param height the height of the mount
     */
    constructor(id: number, name: string, maxSpeed: number, acceleration: number, height: number) {
        this.id = id;
        this.name = name;
        this.maxSpeed = maxSpeed;
        this.acceleration = acceleration;
        this.height = height;
        this.speed = 0;
    }

//#endregion constructor

//#region getters

    public getSpeed() {
        return this.speed;
    }

    public getId(): number {
        return this.id;
    }

    public getHeight(): number {
        return this.height;
    }

//#endregion getters

//#region setters

    public setSpeed(speed: number) {
        this.speed = speed;
    }

//#endregion setters

//#region public methods

    /**
     * accelerates the mount by his acceleration to a maximum of its maxSpeed
     * @param timeDelta time since the last update
     */
    public accelerate(timeDelta: number) {
        this.speed = this.speed + this.acceleration * timeDelta;
        if (this.speed > this.maxSpeed) {
            this.speed = this.maxSpeed;
        }
    }

    /**
     * resets the mount
     */
    public reset() {
        this.speed = 0;
    }

//#endregion public methods

}
