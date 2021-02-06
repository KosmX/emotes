package com.kosmx.emotecraft;

import com.google.gson.JsonParseException;
import com.kosmx.emotecraft.config.SerializableConfig;
import com.kosmx.emotecraft.config.Serializer;
import com.kosmx.emotecraft.network.MainNetwork;
import com.kosmx.emotecraftCommon.CommonData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class Main implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    //init and config variables

    public static final String MOD_ID = "emotecraft";
    public static final String MOD_NAME = "Emotecraft";
    public static final Path CONFIGPATH = FabricLoader.getInstance().getConfigDir().resolve("emotecraft.json");

    public static SerializableConfig config;

    /**
     * This initializer runs on the server and on the client.
     * Load config, init networking
     * And Main has the static variables of the mod.
     */
    @Override
    public void onInitialize(){

        CommonData.logger = new com.kosmx.emotecraftCommon.Logger() {
            @Override
            public void log(String msg) {
                Main.log(Level.INFO, msg);
            }

            @Override
            public void warn(String msg) {
                Main.log(Level.WARN, msg);
            }

            @Override
            public void error(String msg) {
                Main.log(Level.ERROR, msg);
            }
        };

        Serializer.initializeSerializer();

        loadConfig();

        log(Level.INFO, "Initializing");

        //initServerNetwork(); //Network handler both dedicated server and client internal server

        MainNetwork.init();
    }

    public static void log(Level level, String message){
        log(level, message, false);
    }

    public static void log(Level level, String message, boolean force){
        if(force || (config != null && config.showDebug)) LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    private static void loadConfig(){
        if(CONFIGPATH.toFile().isFile()){
            try{
                BufferedReader reader = Files.newBufferedReader(CONFIGPATH);
                config = Serializer.serializer.fromJson(reader, SerializableConfig.class);
                reader.close();
                //config = Serializer.serializer.fromJson(FileUtils.readFileToString(CONFIGPATH, "UTF-8"), SerializableConfig.class);
            }catch(Throwable e){
                config = new SerializableConfig();
                if(e instanceof IOException){
                    Main.log(Level.ERROR, "Can't access to config file: " + e.getLocalizedMessage(), true);
                }else if(e instanceof JsonParseException){
                    Main.log(Level.ERROR, "Config is invalid Json file: " + e.getLocalizedMessage(), true);
                }else{
                    e.printStackTrace();
                }
            }
        }else{
            config = new SerializableConfig();
        }
        EmotecraftCallbacks.loadConfig.invoker().loadConfig(config);
    }


}