package io.github.kosmx.emotes.api.proxy;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;

import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * To hold information about network
 * <p>
 * implement {@link AbstractNetworkInstance} if you want to implement only the necessary functions
 * <p>
 * use this interface if you want to do something completely different
 */
public interface INetworkInstance {
    /**
     * Get the version from the other side. null if default
     * the map doesn't have to contain information about every module. these will be added automatically.
     * <p>
     * do {@code HashMap#put(3, 0)} to disable sound streaming. NBS can't be optimized and often very large
     *
     * @return maybe null
     */
    Map<Byte, Byte> getRemoteVersions();

    /**
     * Receive (and save) versions from the other side
     * @param map map
     */
    void setVersions(Map<Byte, Byte> map);

    /**
     * Invoked after receiving the presence packet
     * {@link INetworkInstance#setVersions(Map)}
     * Used to send server-side config/emotes
     *
     * @deprecated communication changes
     */
    @Deprecated
    default void presenceResponse() {
    }

    /**
     * Do send the sender's id to the server
     * @return true means send
     */
    default boolean sendPlayerID() {
        return false;
    }

    /**
     * The Proxy controller ask you to send the message,
     * only if {@link #isActive()} is true
     * @param builder packet builder
     * @param target target to send message, if null, everyone in the view distance
     *               on server-side target will be ignored
     * @throws IOException when message write to bytes has failed
     */
    void sendMessage(EmotePacket.Builder builder, @Nullable UUID target) throws IOException;

    /**
     * Network instance has received a message, it will send it to EmoteX core to execute
     * you can set your receive event to invoke this
     * there are it's other forms in {@link AbstractNetworkInstance}
     * @param packet received buffer
     * @param player player who plays the emote, Can be NULL but only if {@link #trustReceivedPlayer()} is true or message is not play or stop
     */
    default void receiveMessage(EmotePacket packet, UUID player) {
        EmotesProxyManager.receiveMessage(packet, player, this);
    }

    /**
     * Client is sending config message to server. Vanilla clients will answer to the server configuration phase message.
     * This might get invoked multiple times on the same network instance.
     */
    default void sendC2SConfig(Consumer<EmotePacket.Builder> consumer) {
    }

    /**
     * when receiving a message, it contains a player. If you don't trust in this information, override this and return false
     *
     * @return false if received info is untrusted
     */
    default boolean trustReceivedPlayer() {
        return true;
    }

    /**
     * Is the other side is available
     * your send won't be invoke if you return false
     *
     * @return is this channel working
     */
    boolean isActive();

    /**
     * Does the track the emote play state of every player -> true
     * The client has to resend the emote if a new player get close -> false
     */
    boolean isServerTrackingPlayState();

    /**
     * Maximum size of the data what the instance can send
     * <p>
     * Defaults to {@link io.github.kosmx.emotes.common.CommonData#MAX_PACKET_SIZE}
     * @return max size of bytes[]
     */
    default int maxDataSize() {
        return CommonData.MAX_PACKET_SIZE;
    }
}
