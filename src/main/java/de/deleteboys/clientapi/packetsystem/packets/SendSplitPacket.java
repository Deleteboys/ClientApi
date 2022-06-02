package de.deleteboys.clientapi.packetsystem.packets;

import com.google.gson.JsonObject;
import de.deleteboys.clientapi.main.ClientApi;
import de.deleteboys.clientapi.packetsystem.Packet;

public class SendSplitPacket extends Packet {

    public String uuid;
    public int index;
    public int size;
    public String originalPacket;

    public SendSplitPacket() {
        super("SplitPacket");
    }

    @Override
    public void read(JsonObject jsonObject) {
        if(jsonObject.has("id") && jsonObject.has("index") && jsonObject.has("size") && jsonObject.has("originalPacket")) {
            String id = jsonObject.get("id").getAsString();
            int index = jsonObject.get("index").getAsInt();
            int size = jsonObject.get("size").getAsInt();
            String originalPacket = jsonObject.get("originalPacket").getAsString();
            if(ClientApi.getMethods().getSplitPacket().containsKey(id)) {
                String oldInput = ClientApi.getMethods().getSplitPacket().get(id);
                String newInput = ClientApi.getRsa().decrypt(originalPacket);
                ClientApi.getMethods().getSplitPacket().put(id,oldInput+newInput);
                if(index == size) {
                    ClientApi.getMethods().handelPacketInput(ClientApi.getMethods().getSplitPacket().get(id));
                    ClientApi.getMethods().getSplitPacket().remove(id);
                }
            } else {
                String newInput = ClientApi.getRsa().decrypt(originalPacket);
                ClientApi.getMethods().getSplitPacket().put(id,newInput);
            }
        }
    }

    @Override
    public JsonObject write() {
        JsonObject packet = createBasePacket();
        packet.addProperty("id",uuid);
        packet.addProperty("index",index);
        packet.addProperty("size",size);
        packet.addProperty("originalPacket",originalPacket);
        return packet;
    }

    public SendSplitPacket input(String uuid, int index, int size, String originalPacket) {
        this.uuid = uuid;
        this.index = index;
        this.size = size;
        this.originalPacket = originalPacket;
        return this;
    }
}
