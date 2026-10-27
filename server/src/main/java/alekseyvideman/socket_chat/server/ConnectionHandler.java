package alekseyvideman.socket_chat.server;

import java.io.IOException;
import java.net.ServerSocket;

public final class ConnectionHandler {
    private static ServerSocket serverSocket;

    public static boolean isClosed() {
        return serverSocket.isClosed();
    }

    public static void run() {
        try {
            serverSocket = new ServerSocket(25565);
            System.out.println("Сервер запустился на порту 25565");
        } catch (IOException e) {
            // ...
        }

        /*
         * CONNECTING THREAD
         */
        Server.sharedThreadPool.execute(() -> {
            try {
                while (!(serverSocket.isClosed())) {
                    ClientHandler.getInstance().handleClient(serverSocket.accept());
                    Server.log.info("Присоединился ещё один участник");
                }
            } catch (IOException e) {
                // ...
            }
        });
    }

    public static void close() {
        try {
            serverSocket.close();
            Server.log.info("Сервер выключен.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // ...
        }
    }

}
