package com.kosmx.emotes.fabric;

import com.kosmx.emotes.common.CommonData;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.fabric.executor.EmotesMain;
import net.fabricmc.api.ModInitializer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Initializer implements ModInitializer {

    public static final Logger logger = Logger.getLogger(CommonData.MOD_NAME);

    @Override
    public void onInitialize() {
        EmoteInstance.instance = new EmotesMain();

    }

    public static void log(Level level, String msg){
        logger.log(level, msg);
    }
}
