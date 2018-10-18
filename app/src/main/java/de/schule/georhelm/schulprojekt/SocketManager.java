package de.schule.georhelm.schulprojekt;

import java.net.Socket;

public class SocketManager {
    public static ConnectionSocket getSocket() {
        return socket;
    }

    public static void setSocket(ConnectionSocket socket) {
        SocketManager.socket = socket;
    }

    private static ConnectionSocket socket;



}
