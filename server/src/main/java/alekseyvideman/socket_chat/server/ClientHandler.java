package alekseyvideman.socket_chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public final class ClientHandler {
    private static ClientHandler clientHandler;

    private final List<Socket> clientsList = Collections.synchronizedList(new ArrayList<>());
    private final LinkedBlockingQueue<Map<String, String>> messagesQueue = new LinkedBlockingQueue<>();

    public ClientHandler() {
        startConnectionsHandling();
        startMessageSending();
    }

    public static ClientHandler getInstance() {
        if (clientHandler == null) {
            clientHandler = new ClientHandler();
        }
        return clientHandler;
    }

    public void handleClient(Socket clientSocket) {
        clientsList.add(clientSocket);
    }

    private void startConnectionsHandling() {
        /*
         * RECEIVING THREAD (поток получает сообщения)
         */
        Server.sharedThreadPool.execute(() -> {
            while (!(ConnectionHandler.isClosed())) {
                for (int i = 0; i < clientsList.size(); i++) {
                    Socket currClient = clientsList.get(i);

                    if (currClient.isClosed()) {
                        clientsList.remove(i);
                        continue;
                    }

                    try {
                        if (currClient.getInputStream().available() == 0) {
                            continue;
                        }

                        BufferedReader fromClient =
                                new BufferedReader(new InputStreamReader(
                                        currClient.getInputStream(), StandardCharsets.UTF_8));

                        String clientMsg = fromClient.readLine();

                        if (clientMsg == null) {
                            continue;
                        }

                        int nickMsgSeparator = clientMsg.indexOf("|");
                        String clientNick = clientMsg.substring(0, nickMsgSeparator);
                        String msg = clientMsg.substring(nickMsgSeparator + 1);

                        handleMessage(Map.of("senderName", clientNick, "text", msg));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private void handleMessage(Map<String, String> msg) {
        messagesQueue.add(msg);
        Server.log.info("[Сообщения] " + msg.toString());
    }

    private void startMessageSending() {
        /*
         * SENDING THREAD (поток отправляет сообщения)
         */
        Server.sharedThreadPool.execute(() -> {
            while (!(ConnectionHandler.isClosed())) {
                Map<String, String> currMsg = null;

                try {
                    currMsg = messagesQueue.take();
                } catch (InterruptedException e) {
                    // прерывать поток нечему
                }

                for (Socket currClient : clientsList) { // O(n^m)
                    if (currClient == null || currClient.isClosed()) {
                        continue;
                    }

                    try {
                        PrintWriter toClient = new PrintWriter(currClient.getOutputStream(),
                                true, StandardCharsets.UTF_8);
                        toClient.println(currMsg);

                    } catch (IOException e) {
                        // ...
                    }
                }
            }
        });
    }

}
