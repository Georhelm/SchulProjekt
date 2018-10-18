package de.schule.georhelm.schulprojekt;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ConnectionSocket {
    Socket socket;
    String token;
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
            socket.close();
            return true;
        }catch(Exception e){
            return false;
        }
    }
}
