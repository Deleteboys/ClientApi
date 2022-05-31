package de.deleteboys.clientapi.main;

import com.google.gson.JsonObject;
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

    public static Methods methods;
    private String ip;
    private int port;

    private static Socket socket;

    public PacketManager packetManager;

    private static PublicKey serverPublicKey;
    public static RSA rsa;

    public ClientApi(String ip, int port) {
        this.ip = ip;
        this.port = port;
        methods = new Methods();
        this.packetManager = new PacketManager();
        packetManager.init();
    }

    public void connectClient() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    setSocket(new Socket(ip, port));
                    rsa = new RSA();
                    String publicKey = Base64.getEncoder().encodeToString(rsa.publicKey.getEncoded());
                    packetManager.sendPacket(new RSAPacket().init(publicKey));
                    while (socket.isConnected()) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        try {
                            String line = reader.readLine();
                            if (line != null) {
                                if (methods.isJson(line)) {
                                    try {
                                        JsonObject jsonObject = methods.gson.fromJson(line, JsonObject.class);
                                        if (jsonObject.has("packet")) {
                                            for (Packet packet : getPacketManager().getPackets()) {
                                                if (packet.getPacketName().equals(jsonObject.get("packet").getAsString())) {
                                                    packet.read(jsonObject);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        socket.close();
                                        e.printStackTrace();
                                        break;
                                    }
                                } else {

                                }
                            } else {
                                socket.close();
                            }
                        } catch (SocketException e) {
                            ClientApi.methods.consoleLog("Your connection got disconnected");
                            socket.close();
                            break;
                        }
                    }
                } catch (
                        IOException e) {
                    ClientApi.methods.consoleLog("Can not connect to server");
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

    public synchronized PacketManager getPacketManager() {
        return packetManager;
    }
}
