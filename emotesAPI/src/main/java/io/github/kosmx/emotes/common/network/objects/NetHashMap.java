package io.github.kosmx.emotes.common.network.objects;

import java.util.HashMap;

public class NetHashMap extends HashMap<Byte, AbstractNetworkPacket> {
    public void put(AbstractNetworkPacket packet){
        put(packet.getID(), packet);
    }
}
