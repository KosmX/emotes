package io.github.kosmx.emotes.common.network.objects;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;

public class NetHashMap extends Byte2ObjectOpenHashMap<AbstractNetworkPacket> {
    public void put(AbstractNetworkPacket packet){
        put(packet.getID(), packet);
    }
}
