package com.kosmx.emotecraft;

import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.render.entity.PlayerEntityRenderer;

public class Main implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();


    public static final String MOD_ID = "emotecraft";
    public static final String MOD_NAME = "Emotecraft";

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        //TODO: Initializer
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}