package de.deleteboys.clientapi.packetsystem;

import com.google.gson.JsonObject;
public abstract class Packet {

    public String packetName;

    public abstract void read(JsonObject jsonObject);

    public abstract JsonObject write();

    public String getPacketName() {
        return packetName;
    }

    public Packet(String packetName) {
        this.packetName = packetName;
    }

    public JsonObject createBasePacket() {
        JsonObject packet = new JsonObject();
        packet.addProperty("packet", getPacketName());
        return packet;
    }

}