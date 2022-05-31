package de.deleteboys.clientapi.packetsystem.packets;

import com.google.gson.JsonObject;
import de.deleteboys.clientapi.main.ClientApi;
import de.deleteboys.clientapi.packetsystem.Packet;

public class RSAPacket extends Packet {

    public String key;

    public RSAPacket() {
        super("RSAPacket");
    }

    @Override
    public void read(JsonObject jsonObject) {
        if(jsonObject.has("publicKey")) {
            ClientApi.setServerPublicKey(ClientApi.getMethods().stringToPublicKey(jsonObject.get("publicKey").getAsString()));
        }
    }

    @Override
    public void write() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("packet", this.getPacketName());
        jsonObject.addProperty("publicKey", key);
        ClientApi.getMethods().sendJson(jsonObject);
    }

    public RSAPacket init(String key) {
        this.key = key;
        return this;
    }
}
