package io.github.kosmx.emotes.common.network;

/**
 * Utility class
 * <p>
 * Definitions for packet version map keys
 */
public final class PacketConfig {
    /**
     * Max animation version supported by the other side.
     * (playeranimator)
     */
    public static final byte LEGACY_ANIMATION_FORMAT = (byte) 0;

    /**
     * Max animation version supported by the other side.
     * (playeranimlib)
     */
    public static final byte NEW_ANIMATION_FORMAT = (byte) 0x99;

    /**
     * Enable/disable NBS on this server/client.
     */
    public static final byte NBS_CONFIG = (byte) 3;

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
    // public static final byte ALLOW_EMOTE_STREAM = (byte) 0x81;

    public static final byte DISCOVERY_PACKET = (byte) 8;
    public static final byte HEADER_PACKET = 0x11;
    public static final byte ICON_PACKET = (byte) 0x12;
    public static final byte PLAYER_DATA_PACKET = (byte) 1;
    public static final byte STOP_PACKET = (byte) 10;
}
