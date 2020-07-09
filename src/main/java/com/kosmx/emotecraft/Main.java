package com.kosmx.emotecraft;

import com.kosmx.emotecraft.config.Config;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Main implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    //init and config variables

    public static final String MOD_ID = "emotecraft";
    public static final String MOD_NAME = "Emotecraft";

    public static Config config;

    @Override
    public void onInitialize() {

        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(Config.class).getConfig();

        log(Level.INFO, "Initializing");
    }

    public static void log(Level level, String message){
        log(level, message, false);
    }

    public static void log(Level level, String message, boolean force){
        if (force || config == null || config.showDebug) LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}