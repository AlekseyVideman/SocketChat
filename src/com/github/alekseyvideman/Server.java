package com.github.alekseyvideman;

import com.github.alekseyvideman.client.ClientHandler;

import java.io.*;
import java.net.ServerSocket;
import java.util.logging.Logger;

public final class Server {
    private static ServerSocket serverSocket;

    public static boolean isClosed() {
        return serverSocket.isClosed();
    }

    public static void run() {
        try {
            serverSocket = new ServerSocket(25565);
            Main.log.info("Сервер запустился на порту 25565");
        } catch (IOException e) {
            // ...
        }

        /*
        * CONNECTING THREAD
        */
        Main.sharedThreadPool.execute(() -> {
            try {
                while (!(serverSocket.isClosed())) {
                    ClientHandler.getInstance().handleClient(serverSocket.accept());
                    Main.log.info("Присоединился ещё один участник");
                }
            } catch (IOException e) {
                // ...
            }
        });
    }

    public static void close() {
        try {
            serverSocket.close();
            Main.log.info("Сервер выключен.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // ...
        }
    }

}
