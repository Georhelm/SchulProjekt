package de.schule.georhelm.schulprojekt;

import android.util.JsonReader;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ConnectionSocket {
    Socket socket;
    String token;
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

    public boolean init(){
        try{
            Manager manager = new Manager();
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

            }).on("event", new Emitter.Listener() { //Unsere Events

                @Override
                public void call(Object... args) {}

            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() { //Disconnect event

                @Override
                public void call(Object... args) {}

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

    public void playerReady(){
        socket.emit("player_ready");
    }

    public void startSingleplayerGame(final MenuActivity menuActivity){
        socket.once("found_game", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                System.out.println((JSONObject)args[0]);
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
                    if(type.equals("countdown")){
                        gameView.countDown(jsonObject.getInt("value"));
                    }else if(type.equals("partialUpdate")){
                        JSONObject value = jsonObject.getJSONObject("value");
                        JSONObject player = value.getJSONObject("player1");
                        JSONObject enemy = value.getJSONObject("player2");

                        int playerPos = player.getInt("position");
                        int enemyPos = enemy.getInt("position");
                        gameView.setPlayerPositions(playerPos,enemyPos);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        });
    }
}
