package com.kosmx.emotecraftCommon;

/**
 * static channel to access constant from everywhere in the mod.
 * Including Fabric and Bukkit code.
 */
public class EmotecraftConstants {
    public static boolean isLoaded = false; //to detect if the mod loads twice...

    public static final String MOD_ID = "emotecraft";
    public static final String MOD_NAME = "Emotecraft";


    /**
     * ver 1: older versions
     * ver 2: no network discovery, repeating and bending exists
     * ver 3: network discovery
     */
    public static final int networkingVersion = 3;

    //bidirectional, Emote playing or repeating
    public static final String playEmoteID = "playemote";
    //bidirectional, Emote stop request
    public static final String stopEmoteID = "stopemote";
    //bidirectional, client-server version exchange
    public static final String discoverEmoteID = "discovery";

    public static String getIDAsString(String channel){
        return MOD_ID + ":" + channel;
    }

}
