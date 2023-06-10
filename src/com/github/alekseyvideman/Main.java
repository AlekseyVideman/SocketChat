package com.github.alekseyvideman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Main {
    private boolean run = true;

    public static final ExecutorService sharedThreadPool = Executors.newFixedThreadPool(3);
    public static Logger log = createFormattedLogger("Сервер");

    public static void main(String[] args) {
        Main main = new Main();
        main.startInputReceiving();
    }

    private static Logger createFormattedLogger(String name) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%n[%1$tT] %5$s");
        return Logger.getLogger(name);
    }

    private void startInputReceiving() {
        BufferedReader consoleInput = new BufferedReader(new InputStreamReader(
                System.in, StandardCharsets.UTF_8));

        System.out.println("""
                     
                     -= Вас приветствует программа SocketChat =-
                      
                Что требуется сделать? Выберите один из пунктов ниже:
                  1. Запустить сервер сообщений.
                  2. Остановить сервер сообщений и выйти.
                """);

        while (run) {
            System.out.print("Пункт [1-2]: ");
            try {
                String answer = consoleInput.readLine();
                int answerInt;

                try {
                    answerInt = Integer.parseInt(answer);
                } catch (NumberFormatException e) {
                    System.out.println("Принимаются только целочисленные значения!");
                    continue;
                }

                switch (answerInt) {
                    case 1:
                        Server.run();
                        break;
                    case 2:
                        consoleInput.close();
                        Server.close();
                        sharedThreadPool.shutdownNow();
                        run = false;
                        break;
                    default:
                        System.out.println("Неверный выбор. Повторите попытку.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
