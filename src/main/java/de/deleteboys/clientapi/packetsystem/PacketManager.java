package de.deleteboys.clientapi.packetsystem;

import de.deleteboys.clientapi.packetsystem.packets.RSAPacket;

import java.util.ArrayList;

public class PacketManager {

    private ArrayList<Packet> packets = new ArrayList<>();

    public void init() {
        registerPackets(RSAPacket.class);
    }

    public Packet getPacketByName(String name) {
        for (Packet packet : packets) {
            if (packet.getPacketName().equals(name)) {
                return packet;
            }
        }
        return null;
    }

    public void registerPackets(Class<? extends Packet> clazz) {
        try {
            Packet packet = clazz.newInstance();
            if (!packets.contains(packet)) {
                packets.add(packet);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendPacket(Packet packet) {
        packet.write();
    }

    public synchronized ArrayList<Packet> getPackets() {
        return packets;
    }
}
