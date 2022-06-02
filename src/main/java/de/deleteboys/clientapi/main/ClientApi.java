package de.deleteboys.clientapi.main;

import com.google.gson.JsonObject;
import de.deleteboys.clientapi.methods.Logger;
import de.deleteboys.clientapi.methods.Methods;
import de.deleteboys.clientapi.methods.RSA;
import de.deleteboys.clientapi.packetsystem.Packet;
import de.deleteboys.clientapi.packetsystem.PacketManager;
import de.deleteboys.clientapi.packetsystem.packets.RSAPacket;

import javax.crypto.Cipher;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ClientApi {

    private static Methods methods;
    private String ip;
    private int port;

    private static Socket socket;
    public static PacketManager packetManager;
    private static PublicKey serverPublicKey;
    protected static Logger logger;
    private static RSA rsa;
    private static String logPath = "log/";
    private static boolean running = true;
    private static boolean logInFile = true;
    private static boolean consoleLog = true;
    private static Thread thread;

    public ClientApi(String ip, int port) {
        this.ip = ip;
        this.port = port;
        methods = new Methods();
        this.packetManager = new PacketManager();
        packetManager.init();
        logger = new Logger();
        rsa = new RSA();
        serverPublicKey = null;
    }

    public void connectClient() {
        thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    setSocket(new Socket(ip, port));
                    Logger.info("Connected to " + ip);
                    String publicKey = Base64.getEncoder().encodeToString(rsa.publicKey.getEncoded());
                    packetManager.sendPacket(new RSAPacket().init(publicKey));
                    while (socket.isConnected() && isRunning()) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        try {
                            String line = reader.readLine();
                            if (line != null) {
                                if (methods.isJson(line)) {
                                    try {
                                        Logger.logPacketsGet(line);
                                        methods.handelPacketInput(line);
                                    } catch (Exception e) {
                                        socket.close();
                                        Logger.info("Your connection got disconnected");
                                        Logger.error(e.getMessage());
                                        break;
                                    }
                                } else {
                                    String decryptedString = rsa.decrypt(line);
                                    Logger.logPacketsGet("Encrypted: " + line + " Decrypted: " + decryptedString);
                                    methods.handelPacketInput(decryptedString);
                                }
                            } else {
                                socket.close();
                            }
                        } catch (SocketException e) {
                            Logger.info("Your connection got disconnected");
                            socket.close();
                            break;
                        }
                    }
                } catch (IOException e) {
                    Logger.error("Can not connect to server");
                }
            }
        };
        thread.start();
    }

    private String encode(byte[] date) {
        return Base64.getEncoder().encodeToString(date);
    }

    public synchronized static void setSocket(Socket socket) {
        ClientApi.socket = socket;
    }

    public synchronized static Socket getSocket() {
        return socket;
    }

    public synchronized static PublicKey getServerPublicKey() {
        return serverPublicKey;
    }

    public synchronized static void setServerPublicKey(PublicKey serverPublicKey) {
        ClientApi.serverPublicKey = serverPublicKey;
    }

    public static synchronized PacketManager getPacketManager() {
        return packetManager;
    }

    public static Methods getMethods() {
        return methods;
    }

    public static String getLogPath() {
        return logPath;
    }

    public static void setLogPath(String logPath) {
        ClientApi.logPath = logPath;
    }

    public static void saveCurrentLog() {
        logger.saveLog();
    }

    public static void setPacketLog(boolean state) {
        logger.setPacketLog(state);
    }

    public static boolean isPacketLog() {
        return logger.isPacketLog();
    }

    public static RSA getRsa() {
        return rsa;
    }

    public synchronized static boolean isRunning() {
        return running;
    }

    public static void disconnect() {
        try {
            running = false;
            Logger.info("Disconnected");
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isLogInFile() {
        return logInFile;
    }

    public static void setLogInFile(boolean logInFile) {
        ClientApi.logInFile = logInFile;
    }

    public static boolean isConsoleLog() {
        return consoleLog;
    }

    public static void setConsoleLog(boolean consoleLog) {
        ClientApi.consoleLog = consoleLog;
    }

    public static Thread getThread() {
        return thread;
    }
}
