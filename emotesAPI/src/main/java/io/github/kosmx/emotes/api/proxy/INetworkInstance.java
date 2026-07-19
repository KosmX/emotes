package io.github.kosmx.emotes.api.proxy;

import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketConfig;
import io.github.kosmx.emotes.common.network.objects.NetData;

import java.util.Map;

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
    Map<Byte, Byte> getVersions();

    /**
     * Receive (and save) versions from the other side
     * @param map map
     */
    void setVersions(Map<Byte, Byte> map);

    /**
     * When the network instance disconnects...
     */
    void disconnect();

    default void sendMessage(NetData data, boolean updateVersions) {
        sendMessage(new EmotePacket.Builder(data.copy()), updateVersions);
    }

    /**
     * The Proxy controller ask you to send the message,
     * only if {@link #isActive()} is true
     * @param builder packet builder
     */
    void sendMessage(EmotePacket.Builder builder, boolean updateVersions);

    /**
     * Client is sending config message to server. Vanilla clients will answer to the server configuration phase message.
     * This might get invoked multiple times on the same network instance.
     */
    default EmotePacket.Builder createConfigurationPacket(boolean allowTracking) {
        return createConfigPacket(allowTracking && isTrackingPlayState());
    }

    static EmotePacket.Builder createConfigPacket(boolean isTrackingPlayState) {
        EmotePacket.Builder builder = new EmotePacket.Builder().configureToConfigExchange();
        if (isTrackingPlayState) {
            NetData configData = builder.build().data;
            configData.versions.put(PacketConfig.SERVER_TRACK_EMOTE_PLAY, (byte)0x01);
            return new EmotePacket.Builder(configData);
        }
        return builder;
    }

    /**
     * Is the other side is available
     * your send won't be invoke if you return false
     *
     * @return is this channel working
     */
    boolean isActive();

    /**
     * Does the other side track the emote play state of every player -> true
     * The client has to resend the emote if a new player get close -> false
     */
    boolean isTrackingPlayState();

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
