package com.kosmx.emotes.common;


public class SerializableConfig {

    /**
     * changelog
     * 2 - PlayerSafetyOption
     */
    public static int staticConfigVersion = 2;


    public boolean validateEmote = false;
    public float validThreshold = 8f;
    public boolean showDebug = false;
    public int configVersion;

    public int[] fastMenuHash = new int[8];

}
