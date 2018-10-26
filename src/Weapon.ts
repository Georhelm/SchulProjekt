

export class Weapon {

    private id: number;
    private name: string;

    constructor(id: number, name: string) {
        this.id = id;
        this.name = name;
    }

    public getId(): number {
        return this.id;
    }
}