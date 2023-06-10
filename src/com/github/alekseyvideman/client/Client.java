package com.github.alekseyvideman.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    private final Socket socket;
    private MessagingPhase messagingPhase = MessagingPhase.SENDING;

    public Client(Socket socket) {
        this.socket = socket;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    /**
     * Переключает состояние клиента с SENDING на RECEIVING и наоборот.
     *
     * @see MessagingPhase
     */
    public void turnMessagingPhase() {
        if (messagingPhase.ordinal() == MessagingPhase.SENDING.ordinal()) {
            messagingPhase = MessagingPhase.RECEIVING;
        } else {
            messagingPhase = MessagingPhase.SENDING;
        }
    }

    public MessagingPhase messagingPhase() {
        return messagingPhase;
    }

    /**
     * Отражает одно из двух состояний клиента:
     * <ul>
     *   <li>SENDING - выдаёт сообщение обработчику</li>
     *   <li>RECEIVING - получает сообщение из очереди сообщений</li>
     * </ul>
     */
    public enum MessagingPhase {
        SENDING,
        RECEIVING
    }

}
