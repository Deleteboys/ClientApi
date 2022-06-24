package de.deleteboys.clientapi.methods;

import de.deleteboys.clientapi.main.ClientApi;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Logger {

    private static ArrayList<String> loggerList = new ArrayList<>();
    private boolean packetLog = true;

    public static void info(String message) {
        if(ClientApi.getClientApi().isConsoleLog()) {
            String infoString = getCurrentTime() + " [Info] " + message;
            System.out.println(infoString);
            if (ClientApi.getClientApi().isLogInFile())
                addToLogger(infoString);
        }
    }

    public static void logPacketsSend(String message) {
        if(ClientApi.getClientApi().isConsoleLog()) {
            if (ClientApi.getClientApi().isPacketLog()) {
                String packetLogString = getCurrentTime() + " [PacketLog send] " + message;
                System.out.println(packetLogString);
                if (ClientApi.getClientApi().isLogInFile())
                    addToLogger(packetLogString);
            }
        }
    }

    public static void logPacketsGet(String message) {
        if(ClientApi.getClientApi().isConsoleLog()) {
            if (ClientApi.getClientApi().isPacketLog()) {
                String packetLogString = getCurrentTime() + " [PacketLog get] " + message;
                System.out.println(packetLogString);
                if (ClientApi.getClientApi().isLogInFile())
                    addToLogger(packetLogString);
            }
        }
    }

    public static void error(String message) {
        if(ClientApi.getClientApi().isConsoleLog()) {
            String errorString = getCurrentTime() + " [Error] " + message;
            System.out.println(errorString);
            if (ClientApi.getClientApi().isLogInFile())
                addToLogger(errorString);
        }
    }

    private static String getCurrentTime() {
        DateTimeFormatter dtf4 = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        return dtf4.format(LocalDateTime.now());
    }

    private static void addToLogger(String message) {
        if (loggerList.size() >= 1000) {
            ClientApi.getClientApi().saveCurrentLog();
            loggerList.clear();
            info("Auto save log");
            loggerList.add(message);
        } else {
            loggerList.add(message);
        }
    }

    public void saveLog() {
        String createFileName = getCurrentTime().replace("/", "-").replace(" ", "-").replace(":", ".") + ".log";
        String fileName = ClientApi.getClientApi().getLogPath().endsWith("/") ? ClientApi.getClientApi().getLogPath() + createFileName : ClientApi.getClientApi().getLogPath() + "/".concat(createFileName);
        File file = new File(fileName);
        if (file.exists()) {
            error("File already exists");
            return;
        }
        try {
            Path path = Paths.get(ClientApi.getClientApi().getLogPath());
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            if (file.createNewFile()) {
                PrintWriter printWriter = new PrintWriter(fileName, StandardCharsets.UTF_8);
                for (String s : loggerList) {
                    printWriter.write(s + "\n");
                }
                printWriter.close();
                Logger.info("Saved Log " + fileName);
            }
        } catch (IOException e) {
            error(e.getMessage());
        }

    }

    public boolean isPacketLog() {
        return packetLog;
    }

    public void setPacketLog(boolean packetLog) {
        this.packetLog = packetLog;
    }
}
