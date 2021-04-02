package io.github.kosmx.emotes.main.network;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.INetworkInstance;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.logging.Level;

/**
 * I don't use final methods to stay compatible with Java 1.8
 */
@SuppressWarnings("DeprecatedIsStillUsed")
public interface IClientNetwork extends INetworkInstance {

    /**
     * If you want the mod to send the UUID make it true
     * @return If the client will send player UUID
     */
    @Override
    default boolean sendPlayerID(){
        return false;
    }

    /**
     * Invoke this if you want to send a config
     */
    @Override
    default void sendConfigCallback(){
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.setVersion(this.getVersions());
        try {
            this.sendMessage(packetBuilder.build().write().array(), null);
        }
        catch (Exception e){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "Error while writing packet: " + e.getMessage(), true);
            if(EmoteInstance.config.showDebug.get()){
                e.printStackTrace();
            }
        }
    }

    //You have to override one of the sendPacketToServer functions
    //If your packet sending is slow, use a thread to send the packet

    /**
     * Send customizable emotePackets
     * @param builder packetBuilder
     * @param target target player
     */
    @Override
    default void sendMessage(EmotePacket.Builder builder, @Nullable IEmotePlayerEntity target) throws IOException {
        this.sendMessage(builder.build().write(), target);    //everything is happening on the heap, there won't be any memory leak
    }

    @Deprecated
    @Override
    default void sendMessage(byte[] bytes, @Nullable IEmotePlayerEntity target){}

    @Deprecated
    @Override
    default void sendMessage(ByteBuffer byteBuffer, @Nullable IEmotePlayerEntity target){
        sendMessage(byteBuffer.array(), target);
    }

    /**
     * When you receive a message
     * @param bytes the received byte array
     * @param player if you don't use custom player identification api, null
     */
    default void receiveMessage(byte[] bytes, UUID player){
        receiveMessage(ByteBuffer.wrap(bytes), player);
    }

    default void receiveMessage(byte[] bytes){
        receiveMessage(ByteBuffer.wrap(bytes), null);
    }

    /**
     * When you receive a message
     * @param byteBuffer the received ByteBuffer
     * @param player if you don't use custom player identification api, null
     */
    default void receiveMessage(ByteBuffer byteBuffer, UUID player){
        ClientPacketManager.receiveMessage(byteBuffer, player, this);
    }
    default void receiveMessage(ByteBuffer byteBuffer){
        receiveMessage(byteBuffer, null);
    }

}
