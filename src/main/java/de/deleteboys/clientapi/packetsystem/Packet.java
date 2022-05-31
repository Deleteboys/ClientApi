package de.deleteboys.clientapi.packetsystem;

import com.google.gson.JsonObject;

public abstract class Packet {

    public String packetName;

    public abstract void read(JsonObject jsonObject);

    public abstract void write();

    public String getPacketName() {
        return packetName;
    }

    public Packet(String packetName) {
        this.packetName = packetName;
    }
}