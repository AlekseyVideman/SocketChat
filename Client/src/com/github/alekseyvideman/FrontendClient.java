package com.github.alekseyvideman;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class FrontendClient {
    private final Socket socket = new Socket();
	private volatile boolean active = true;
    private String nickname;
    private final Thread messagesFetcher = new Thread(() -> {
        while (active) {
            try {
				if (socket.getInputStream().available() == 0) {
					Thread.yield();
				}

                BufferedReader fromServer = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8));
                String msg = fromServer.readLine();
                if (msg == null) {
                    close();
                }
                System.out.println("==> " + msg);
            } catch (SocketException e) {
                // ...
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    public static void main(String[] args) {
        FrontendClient it = new FrontendClient();
        it.setupNickname();
        it.tryConnect();
        it.messagesFetcher.start();
        it.startMessageSending();
    }

    public void startMessageSending() {
        try (PrintWriter toServer = new PrintWriter(socket.getOutputStream(), true,
                StandardCharsets.UTF_8);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in,
                     StandardCharsets.UTF_8))) {

            System.out.printf("Вы подключились к серверу как '%s'%n", nickname);


            while (true) {
                String msg = consoleInput.readLine();

                if (msg.isEmpty()) {
                    consoleInput.close();
                    close();
                    break;
                }

                final int msgLengthLimit = 50;
                if (msg.length() > msgLengthLimit) {
                    System.out.printf("Ваше сообщение слишком длинное! (%d/%d)%n", msg.length(),
                            msgLengthLimit);
                    continue;
                }

                toServer.println(nickname + "|" + msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        active = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Отключено.");
    }
    
    private void setupNickname() {
        System.out.print("Укажите Ваш псевдоним: ");

        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in,
                StandardCharsets.UTF_8));
        try {
            String nick = consoleInput.readLine();
            if (!nick.isEmpty()) {
                nickname = nick;
            } else if (nick.length() > 20) {
                System.out.printf("Длина ника слишком длинная! (%d/20)", nick.length());
            } else {
                nickname = "Аноним" + new Random().nextInt(0, 10);
            }
        } catch (IOException e) {
            //...
        }
    }

    private void tryConnect() {
        try {
            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), 25565));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
