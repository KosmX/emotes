package io.github.kosmx.emotes.executor;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * To hold information about network
 */
public interface INetworkInstance {
    HashMap<Byte, Byte> getVersions();
    void setVersions(HashMap<Byte, Byte> map);
    boolean sendPlayerID();
    void sendMessage(EmotePacket.Builder builder, @Nullable IEmotePlayerEntity target) throws IOException;
    @Deprecated
    void sendMessage(byte[] bytes, @Nullable IEmotePlayerEntity target);
    void sendConfigCallback();

    /**
     * Is the network instance active
     * @return is this channel working
     */
    boolean isActive();


    @Deprecated
    default void sendMessage(ByteBuffer byteBuffer, @Nullable IEmotePlayerEntity target){
        sendMessage(byteBuffer.array(), target);
    }
}
