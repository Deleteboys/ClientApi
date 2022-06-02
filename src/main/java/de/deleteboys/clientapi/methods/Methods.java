package de.deleteboys.clientapi.methods;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.deleteboys.clientapi.main.ClientApi;
import de.deleteboys.clientapi.packetsystem.Packet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class Methods {

    public Gson gson = new Gson();

    private ConcurrentHashMap<String,String> splitPacket = new ConcurrentHashMap<>();
    public void consoleLog(String message) {
        System.out.println(message);
    }

    public PublicKey stringToPublicKey(String key) {
        try {
            byte[] data = Base64.getDecoder().decode((key.getBytes()));
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory fact = KeyFactory.getInstance("RSA");
            return fact.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isJson(String input) {
        try {
            JsonObject jsonObject = gson.fromJson(input, JsonObject.class);
            return jsonObject != null;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    public void encryptAndSendPacket(JsonObject jsonObject) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ClientApi.getSocket().getOutputStream()));
            String packet = gson.toJson(jsonObject);
            String encryptedPacket = ClientApi.getRsa().encrypt(packet, ClientApi.getServerPublicKey());
            Logger.logPacketsSend("Encrypted: " + encryptedPacket + " Decrypted:" + packet);
            writer.write(encryptedPacket);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void sendSplitPacket(JsonObject jsonObject) {
        try {
            String originalPacket = jsonObject.get("originalPacket").getAsString();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ClientApi.getSocket().getOutputStream()));
            String encryptedPacket = ClientApi.getRsa().encrypt(originalPacket, ClientApi.getServerPublicKey());
            JsonObject newPacket = new JsonObject();
            newPacket.addProperty("packet",jsonObject.get("packet").getAsString());
            newPacket.addProperty("id",jsonObject.get("id").getAsString());
            newPacket.addProperty("index",jsonObject.get("index").getAsString());
            newPacket.addProperty("size",jsonObject.get("size").getAsString());
            newPacket.addProperty("originalPacket",encryptedPacket);
            String packetAsString = gson.toJson(newPacket);
            Logger.logPacketsSend(packetAsString);
            writer.write(packetAsString);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendJson(JsonObject jsonObject) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ClientApi.getSocket().getOutputStream()));
            String packet = gson.toJson(jsonObject);
            Logger.logPacketsSend(packet);
            writer.write(packet);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handelPacketInput(String input) {
        if (isJson(input)) {
            JsonObject jsonObject = gson.fromJson(input, JsonObject.class);
            if (jsonObject.has("packet")) {
                for (Packet packet : ClientApi.getPacketManager().getPackets()) {
                    if (packet.getPacketName().equals(jsonObject.get("packet").getAsString())) {
                        packet.read(jsonObject);
                    }
                }
            }
        }
    }

    public int getNumberOfChars(String input) {
        int j = 0;
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) != ' ') {
                j++;
            }
        }
        return j;
    }

    public ConcurrentHashMap<String, String> getSplitPacket() {
        return splitPacket;
    }
}
