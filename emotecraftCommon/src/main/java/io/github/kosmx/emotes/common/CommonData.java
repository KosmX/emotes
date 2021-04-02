package io.github.kosmx.emotes.common;

/**
 * static channel to access constant from everywhere in the mod.
 * Including Fabric and Bukkit code.
 */
public class CommonData {
    public static boolean isLoaded = false; //to detect if the mod loads twice...

    public static final String MOD_ID = "emotecraft";
    public static final String MOD_NAME = "Emotecraft";


    /**
     * ver 1: older versions
     * ver 2: no network discovery, repeating and bending exists
     * ver 3: network discovery
     * ver 4: not syncing head bending values
     * ver 5: boolean, easing can indicated after the move
     * ver 6: experimental sound sync
     *
     * ver 7: reserved
     *
     * --------------------------------------
     *
     * ver 8: New networking:
     * sub packet versioning, Collar network ready, sync current tick instead of repeat boolean
     * EmoteUUID in play and stop, no spamming, Eases in bytecodes instead of Strings
     * Only one network ID, not compatibly with earlier versions.
     * sub-packets and sub-versions. probably final version...
     */
    public static final int networkingVersion = 8;

    //bidirectional, Emote playing or repeating
    public static final String playEmoteID = "emote";
    ////bidirectional, Emote stop request
    //public static final String stopEmoteID = "stopemote";
    ////bidirectional, client-server version exchange
    //public static final String discoverEmoteID = "discovery";

    public static String getIDAsString(String channel){
        return MOD_ID + ":" + channel;
    }

}
