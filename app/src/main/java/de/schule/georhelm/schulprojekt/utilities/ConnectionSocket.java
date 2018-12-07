package de.schule.georhelm.schulprojekt.utilities;

import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import de.schule.georhelm.schulprojekt.activities.EquipmentActivity;
import de.schule.georhelm.schulprojekt.activities.LoginActivity;
import de.schule.georhelm.schulprojekt.activities.MenuActivity;
import de.schule.georhelm.schulprojekt.views.GameView;
import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ConnectionSocket {

    //#region static properties
    private static ConnectionSocket connectionSocket;
    //#endregion static properties

    //#region properties
    private Socket socket;
    private String token;
    //#endregion properties

    //#region constructors
    public ConnectionSocket(String token) {
        this.token = token;
    }
    //#endregion constructor

    //#region static setters
    public static void setSocket(ConnectionSocket socket) {
        ConnectionSocket.connectionSocket = socket;
    }
    //#endregion static setters

    //#region static getters
    public static ConnectionSocket getSocket() {
        return ConnectionSocket.connectionSocket;
    }
    //#endregion static getters

    //#region public methods

    /**
     * Initializes socket and sets the onEventConnect event.
     * @param context The Context from which this method is called.
     * @return
     */
    public boolean init(final Context context){
        try{
            IO.Options options = new IO.Options();
            options.query = "token=" + token;
            String[] transportArray = {"websocket"};
            options.transports = transportArray;
            socket = IO.socket("http://siffers.de:1234",options);
            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                }

            });
            socket.connect();
            return true;
        }catch(Exception e){
            return false;
        }
    }

    /**
     * Disconnects the socket.
     * @return
     */
    public boolean logOut(){
        try{
            socket.disconnect();
            //socket.close();
            return true;
        }catch(Exception e){
            return false;
        }
    }

    /**
     * Creates a JSONObject containing the information for the server, that the lance should be lifted.
     * Emits that information.
     * @param lanceUp
     */
    public void playerInput(boolean lanceUp) {
        JSONObject data = new JSONObject();
        try {
            data.put("type", "lance");
            data.put("value", lanceUp);
            this.socket.emit("game_input", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Emits event to the server to notify that the player is ready
     */
    public void playerReady(){
        socket.emit("player_ready");
    }

    /**
     * Notifies server to start a singleplayergame. Starts the game.
     * @param menuActivity The menuActivity calling this method.
     */
    public void startSingleplayerGame(final MenuActivity menuActivity){
        startGame(menuActivity);
        socket.emit("start_singleplayer");
    }

    /**
     * Starts to search for a multiplayer game.
     * Listen to Server when game is found
     * @param menuActivity
     */
    public void startMultiplayerGame(final MenuActivity menuActivity){
        startGame(menuActivity);
        socket.emit("start_multiplayer", new Ack() {
            @Override
            public void call(Object... args) {
                menuActivity.showSearchingGame();
            }
        });
    }

    /**
     * Notify server that search is cancelled.
     */
    public void cancelSearch(final MenuActivity menuActivity){
        socket.emit("cancel_search", new Ack() {
            @Override
            public void call(Object... args) {
                menuActivity.resetView();
            }
        });
    }

    /**
     * call all available equipment from server.
     * @param equipmentActivity the equipmentActivity calling this method.
     */
    public void getAvailableEquipment(final EquipmentActivity equipmentActivity){
        socket.emit("get_equipment", null, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject equipment = (JSONObject)args[0];
                equipmentActivity.fillEquipment(equipment);
            }
        });
    }

    /**
     * Notify server which equipment has to be saved.
     * @param mountId
     */
    public void saveEquipment(int mountId){
        JSONObject equipment = new JSONObject();
        try {
            equipment.put("mountId",mountId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("set_equipment",equipment);
    }

    /**
     * Gets wincount of the player from server (By acknowledgement).
     * @param menu The MenuActivity calling this method.
     */
    public void getWinCount(final MenuActivity menu) {
        socket.emit("get_wins", null, new Ack() {
            @Override
            public void call(Object... args) {
                if(args[0] instanceof Integer) {
                    menu.setWins((int) args[0]);
                }
            }
        });
    }

    /**
     * Notifies the Server that player has left the game.
     */
    public void leaveGame() {
        socket.emit("leave_game");
    }

    /**
     * Initializes the main game.
     * Refreshes and activates listener for gameUpdates which update the game data.
     * @param gameView
     */
    public void initGame(final GameView gameView){
        socket.off("game_update");
        socket.on("game_update", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try{
                    JSONObject jsonObject = (JSONObject)args[0];
                    String type = jsonObject.getString("type");
                    switch(type) {
                        case "countdown":
                            gameView.countDown(jsonObject.getInt("value"));
                            break;
                        case "partialUpdate":
                            JSONObject value = jsonObject.getJSONObject("value");
                            JSONObject player = value.getJSONObject("player1");
                            JSONObject enemy = value.getJSONObject("player2");

                            int playerPos = player.getInt("position");
                            int enemyPos = enemy.getInt("position");
                            int playerLanceAngle = player.getInt("weaponAngle");
                            int enemyLanceAngle = enemy.getInt("weaponAngle");
                            gameView.setPlayerPositions(playerPos,enemyPos);
                            gameView.setLanceAngles(playerLanceAngle, enemyLanceAngle);
                            break;
                        case "gameEnd":
                            finishRound(jsonObject,gameView,true);
                            break;
                        case "roundEnd":
                            finishRound(jsonObject,gameView,false);

                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
    }
    //#endregion public methods

    //#region private methods
    /**
     * Listens to server if a game is found, then starts the game in the menu activity.
     * @param menuActivity The menuactivity in which the game should be started.
     */
    private void startGame(final MenuActivity menuActivity) {
        socket.off("found_game");
        socket.once("found_game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                menuActivity.startGame((JSONObject)args[0]);
            }
        });
    }
    /**
     * Updates player and enemy values at the end of round and starts endround animation of gameview.
     * @param jsonObject The JSONObject from the server containing the updated data.
     * @param gameView The gameview in which the current game is being drawn.
     * @param isLastRound A boolean value representing if its the last round of the game.
     */
    private void finishRound(JSONObject jsonObject, GameView gameView,boolean isLastRound){
        try{
            JSONObject values = jsonObject.getJSONObject("value");
            JSONObject playerJson = values.getJSONObject("player1");
            int playerSpeed = playerJson.getInt("speed");
            int playerHitpoints = playerJson.getInt("hitpoints");
            int playerWeaponHeight = playerJson.getInt("weaponHeight");
            int playerPointHit = playerJson.getInt("pointHit");

            JSONObject enemyJson = values.getJSONObject("player2");
            int enemySpeed = enemyJson.getInt("speed");
            int enemyHitpoints = enemyJson.getInt("hitpoints");
            int enemyWeaponHeight = enemyJson.getInt("weaponHeight");
            int enemyPointHit = enemyJson.getInt("pointHit");
            boolean gameWon = false;
            if(isLastRound){
                gameWon = values.getBoolean("victory");
            }
            gameView.endRound(enemySpeed,enemyHitpoints,enemyWeaponHeight,enemyPointHit,playerSpeed,playerHitpoints,playerWeaponHeight,playerPointHit, isLastRound, gameWon);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    //#endregion private methods
}
