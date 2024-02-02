package io.github.kosmx.emotes.server.network;

import io.github.kosmx.emotes.api.proxy.INetworkInstance;

import java.nio.ByteBuffer;

public interface IServerNetworkInstance extends INetworkInstance {


    /**
     * Server closes connection with instance
     */
    default void closeConnection(){}

    default boolean trackPlayState() {
        return true;
    }

    EmotePlayTracker getEmoteTracker();

    void sendGeyserPacket(ByteBuffer buffer);

    void disconnect(String literal);
}
