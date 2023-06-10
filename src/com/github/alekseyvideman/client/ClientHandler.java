package com.github.alekseyvideman.client;

import com.github.alekseyvideman.Main;
import com.github.alekseyvideman.Server;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public final class ClientHandler {
    private static ClientHandler clientHandler;

    private final List<Socket> clientsList = Collections.synchronizedList(new ArrayList<>());
    private final LinkedBlockingQueue<Message> messagesQueue = new LinkedBlockingQueue<>();

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
        Main.sharedThreadPool.execute(() -> {
            while (!(Server.isClosed())) {
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

                        handleMessage(new Message(clientNick, msg));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }



    private void handleMessage(Message msg) {
        messagesQueue.add(msg);
        Main.log.info("[Сообщения] " + msg.toString());
    }

    private void startMessageSending() {
        /*
         * SENDING THREAD (поток отправляет сообщения)
         */
        Main.sharedThreadPool.execute(() -> {
            while (!(Server.isClosed())) {
                Message currMsg = null;

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
