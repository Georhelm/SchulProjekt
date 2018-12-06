import { Game } from "../Game";
import { GameSocket } from "../Socket";
import { User } from "../User";

export class Dashboard {

    public static getOverview(): string {
        const html = `<table>
        <tr><td><a href="/admin/queue">Queue:</a></td><td>` + GameSocket.getQueueLength() + `</td></tr>
        <tr><td><a href="/admin/online">Online:</a></td><td>` + User.getUserCount() + `</td></tr>
        <tr><td><a href="/admin/games">Games:</a></td><td>` + Game.getGameCount() + `</td></tr>
        </table>`;
        return html;
    }

    public static getQueueDetails(): string {
        const users = GameSocket.getQueue();
        let html = `<table>`;
        for (const user of users) {
            html += `<tr><td>` + user.getUsername() + `</td></tr>`;
        }
        html += `</table>`;
        return html;
    }

    public static getOnlineDetails(): string {
        const users = User.getUsers();
        let html = `<table>`;
        for (const user of users) {
            html += `<tr><td>` + user.getUsername() + `</td></tr>`;
        }
        html += `</table>`;
        return html;
    }

    public static getGameDetails(): string {
        const games = Game.getGames();
        let html = `<table>
        <tr><th>Game</th><th>Player1</th><th>Player2</th></tr>`;
        for (const game of games) {
            html += `<tr><td>` + game.getId() + `</td><td>` + game.getPlayer1().getUsername() + `</td><td>` + game.getPlayer2().getUsername() + `</tr>`;
        }
        html += `</table>`;
        return html;
    }
}
