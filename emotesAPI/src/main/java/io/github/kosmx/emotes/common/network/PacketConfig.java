package io.github.kosmx.emotes.common.network;

/**
 * Utility class
 * <p>
 * Definitions for packet version map keys
 */
public final class PacketConfig {

    /**
     * Max animation version supported by the other side.
     */
    public static final byte ANIMATION_FORMAT = (byte) 0;

    /**
     * Enable/disable NBS on this server/client. defaults to true (1)
     */
    public static final byte NBS_CONFIG = (byte) 3;

    /**
     * Allow server to client emote list sync. set to negative value to disable.
     */
    public static final byte ALLOW_EMOTE_SYNC = (byte) 11;

    /**
     * Announce emote play state tracking feature. Mod and bukkit plugin Emotecraft does track state on server.
     * If the server sets it to 0 (false) the client will repeat all emote play messages if a new player is seen.
     */
    public static final byte SERVER_TRACK_EMOTE_PLAY = (byte) 0x80;

    /**
     * Whether the server allows huge emotes in play state.
     * 0: no
     * any non-zero value: yes
     */
    public static final byte ALLOW_EMOTE_STREAM = (byte) 0x81;
}
