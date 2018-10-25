export class Player {

    public id: string;
    public username: string;
    public mountId: number;
    public weaponId: number;
    private position: number;
    private static playerList: Player[];

    constructor(id: string, username: string) {
        this.id = id;
        this.username = username;
        this.mountId = 1;
        this.weaponId = 1;
        this.position = 0;
        if (Player.playerList === undefined) {
            Player.playerList = [];
        }
        Player.playerList.push(this);
    }

    public getGameData(): PlayerGameData {
        const result: PlayerGameData = {
            username: this.username,
            mountId: this.mountId,
            weaponId: this.weaponId,
            position: this.position
        }

        return result;
    }

    public static getPlayerById(id: string) {
        for (const player of Player.playerList) {
            if (player.id === id) {
                return player;
            }
        }
        return null;
    }

    public static removePlayerById(id: string) {
        const players = Player.playerList;
        for (let i = 0; i < players.length; i++) {
            if (players[i].id === id) {
                return players.splice(i, 1);
            }
        }
        return players;
    }

    public static getPlayerCount(): number {
        return Player.playerList.length;
    }
}

export interface PlayerGameData {
    username: string,
    mountId: number,
    weaponId: number,
    position: number
} 