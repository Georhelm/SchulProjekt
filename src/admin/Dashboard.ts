import { Game } from "../Game";
import { GameSocket } from "../Socket";
import { User } from "../User";

export class Dashboard {

    /**
     * creates html for the overview
     */
    public static getOverview(): string {
        const html = `<table class="table table-striped">
        <tr><td><a href="/admin/queue">Queue:</a></td><td>` + GameSocket.getQueueLength() + `</td></tr>
        <tr><td><a href="/admin/online">Online:</a></td><td>` + User.getUserCount() + `</td></tr>
        <tr><td><a href="/admin/games">Games:</a></td><td>` + Game.getGameCount() + `</td></tr>
        </table>`;
        return html;
    }

    /**
     * creates html for the queue details view
     */
    public static getQueueDetails(): string {
        const users = GameSocket.getQueue();
        let html = `<a href="/admin">Back</a>` +
        `<table class="table table-striped"><thead class="thead-dark">
        <tr><th>Playername</th></tr></thead>`;
        for (const user of users) {
            html += `<tr><td>` + user.getUsername() + `</td></tr>`;
        }
        html += `</table>`;
        return html;
    }

    /**
     * creates html for the online users view
     */
    public static getOnlineDetails(): string {
        const users = User.getUsers();
        let html = `<a href="/admin">Back</a>` +
        `<table class="table table-striped"><thead class="thead-dark">
        <tr><th>Playername</th></tr></thead>`;
        for (const user of users) {
            html += `<tr><td>` + user.getUsername() + `</td></tr>`;
        }
        html += `</table>`;
        return html;
    }

    /**
     * creates html for the game details view
     */
    public static getGameDetails(): string {
        const games = Game.getGames();
        let html = `<a href="/admin">Back</a>` +
        `<table class="table table-striped table-bordered">` +
        `<thead class="thead-dark"><tr><th>Game</th><th>Width</th>` +
        `<th>Player1</th><th>HP</th><th>Position</th><th>Mount</th><th>Speed</th>` +
        `<th>Player2</th><th>HP</th><th>Position</th><th>Mount</th><th>Speed</th></tr></thead>`;
        for (const game of games) {
            const player1 = game.getPlayer1();
            const player2 = game.getPlayer2();
            html += `<tr><td>` + game.getId() + `</td><td>` + game.getWidth() + `</td><td>` +
            player1.getUsername() + `</td><td>` + player1.getHitpoints() + `</td><td>` + Math.round(player1.getPosition()) + `</td><td>` +
            player1.getMount().getName() + `</td><td>` + Math.round(player1.getSpeed()) + `</td><td>` +
            player2.getUsername() + `</td><td>` + player2.getHitpoints() + `</td><td>` + Math.round(player2.getPosition()) + `</td><td>` +
            player2.getMount().getName() + `</td><td>` + Math.round(player2.getSpeed()) + `</td></tr>`;
        }
        html += `</table>`;
        return html;
    }

    /**
     * creates the html header includes bootstrap css
     */
    public static getHead(): string {
        const html = `<head>
        <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css">
        </head>`;

        return html;
    }
}
