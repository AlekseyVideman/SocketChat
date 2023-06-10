package com.github.alekseyvideman.client;

public class Message {
    private final String text;
    private final String senderName;

    public Message(String senderName, String txt) {
        this.senderName = senderName;
        this.text = txt;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", senderName, text);
    }
}
