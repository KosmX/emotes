package io.github.kosmx.emotes.server.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;

public interface IServerNetworkInstance extends INetworkInstance {


    default EmotePacket.Builder getS2CConfigPacket() {
        NetData configData = new EmotePacket.Builder().configureToConfigExchange(true).build().data;
        if (trackPlayState()) {
            configData.versions.put((byte)0x80, (byte)0x01);
        }
        return new EmotePacket.Builder(configData);
    }

    /**
     * Server closes connection with instance
     */
    default void closeConnection(){}

    default boolean trackPlayState() {
        return true;
    }

    EmotePlayTracker getEmoteTracker();

}
