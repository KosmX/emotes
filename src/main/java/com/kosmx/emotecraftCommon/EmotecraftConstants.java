package com.kosmx.emotecraftCommon;

/**
 * static channel to access constant from everywhere in the mod.
 * Including Fabric and Bukkit code.
 */
public class EmotecraftConstants {
    public static boolean isLoaded = false; //to detect if the mod loads twice...

    public static final String MOD_ID = "emotecraft";
    public static final String MOD_NAME = "Emotecraft";

    public static final int networkingVersion = 3;

    public static final String playEmoteID = "playemote";
    public static final String stopEmoteID = "stopemote";
    public static final String discoverEmoteID = "discovery";

    public static String getIDAsString(String channel){
        return MOD_ID + ":" + channel;
    }

}
