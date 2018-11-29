package de.schule.georhelm.schulprojekt;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.JsonReader;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ConnectionSocket {
    private Socket socket;
    private String token;
    public static ConnectionSocket getSocket() {
        return ConnectionSocket.connectionSocket;
    }
    public static void setSocket(ConnectionSocket socket) {
        ConnectionSocket.connectionSocket = socket;
    }
    private static ConnectionSocket connectionSocket;

    public ConnectionSocket(String token) {
        this.token = token;
    }

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
                    System.out.println("Event_Connect of socket triggered :)");
                }

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() { //Disconnect event

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

    public boolean logOut(){
        try{
            socket.disconnect();
            //socket.close();
            return true;
        }catch(Exception e){
            return false;
        }
    }

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

    public void playerReady(){
        socket.emit("player_ready");
    }

    public void startSingleplayerGame(final MenuActivity menuActivity){
        socket.once("found_game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                menuActivity.startSingleplayer((JSONObject)args[0]);
            }
        });
        socket.emit("start_singleplayer");
    }

    public void initGame(final GameView gameView){
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
                            int playerLanceTipYPos = player.getInt("weaponHeight");
                            int enemyLanceTipYPos = enemy.getInt("weaponHeight");

                            gameView.setPlayerPositions(playerPos,enemyPos,playerLanceTipYPos,enemyLanceTipYPos);
                            gameView.setLanceAngles(playerLanceAngle, enemyLanceAngle);
                            break;
                        case "gameEnd":
                            JSONObject endGameValues = jsonObject.getJSONObject("value");
                            JSONObject endGamePlayer = endGameValues.getJSONObject("player1");
                            int endGamePlayerSpeed = endGamePlayer.getInt("speed");
                            int endGamePlayerHitpoint = endGamePlayer.getInt("hitPoint");

                            JSONObject endGameEnemy = endGameValues.getJSONObject("player2");
                            int endGameEnemySpeed = endGameEnemy.getInt("speed");
                            int endGameEnemyHitpoint = endGameEnemy.getInt("hitPoint");
                            gameView.endGame(endGameEnemySpeed,endGameEnemyHitpoint,endGamePlayerSpeed,endGamePlayerHitpoint);
                            break;
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
    }
}
