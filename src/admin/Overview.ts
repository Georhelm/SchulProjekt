import { GameSocket } from "../Socket";
import { User } from "../User";
import { Game } from "../Game";

export class Overview{
    

    static getHtml(): string {
        const html = `<table>
        <tr><td>Queue:</td><td>` + GameSocket.getQueueLength() + `</td></tr>
        <tr><td>Online:</td><td>` + User.getUserCount() + `</td></tr>
        <tr><td>Games:</td><td>` + Game.getGameCount() + `</td></tr>
        </table>`;
        return html;
    }
}