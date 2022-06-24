package de.deleteboys.clientapi.packetsystem;

import com.google.gson.JsonObject;
import de.deleteboys.clientapi.main.ClientApi;
import de.deleteboys.clientapi.packetsystem.packets.RSAPacket;
import de.deleteboys.clientapi.packetsystem.packets.SendSplitPacket;

import java.util.ArrayList;
import java.util.UUID;

public class PacketManager {

    private ArrayList<Packet> packets = new ArrayList<>();
    private int splitPacketDelay = 10;

    public void init() {
        registerPackets(RSAPacket.class);
        registerPackets(SendSplitPacket.class);
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
        JsonObject jsonPacket = packet.write();
        String packetAsString = ClientApi.getClientApi().getMethods().gson.toJson(jsonPacket);
        if (packet instanceof SendSplitPacket) {
            ClientApi.getClientApi().getMethods().sendSplitPacket(packet.write());
            return;
        }
        if (ClientApi.getClientApi().getServerPublicKey() != null) {
            if (ClientApi.getClientApi().getMethods().getNumberOfChars(packetAsString) > 117) {
                String[] splitList = packetAsString.split("(?<=\\G.{" + 117 + "})");
                String uuid = UUID.randomUUID().toString();
                if (ClientApi.getClientApi().getPacketSplitType() == PacketSplitType.DEFAULT) {
                    for (int i = 0; i < splitList.length; i++) {
                        try {
                            sendPacket(new SendSplitPacket().input(uuid, i + 1, splitList.length, splitList[i], ClientApi.getClientApi().getPacketSplitType()));
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else if (ClientApi.getClientApi().getPacketSplitType() == PacketSplitType.ONELARGE) {
                    sendPacket(new SendSplitPacket().input(uuid, ClientApi.getClientApi().getPacketSplitType(), splitList));
                }
                return;
            }
            ClientApi.getClientApi().getMethods().encryptAndSendPacket(packet.write());
        } else {
            ClientApi.getClientApi().getMethods().sendJson(packet.write());
        }
    }

    public synchronized ArrayList<Packet> getPackets() {
        return packets;
    }


    public int getSplitPacketDelay() {
        return splitPacketDelay;
    }

    public void setSplitPacketDelay(int splitPacketDelay) {
        this.splitPacketDelay = splitPacketDelay;
    }
}
