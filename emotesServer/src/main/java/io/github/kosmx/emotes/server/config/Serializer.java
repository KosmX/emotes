package io.github.kosmx.emotes.server.config;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

public class Serializer {
    public static Gson serializer;

    public static Serializer INSTANCE;

    public Serializer(){
        initializeSerializer();
    }

    public void initializeSerializer(){
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        this.registerTypeAdapters(builder);
        serializer = builder.create();
    }

    protected void registerTypeAdapters(GsonBuilder builder){
        JsonDeserializer<SerializableConfig> configSerializer = new ConfigSerializer();
        JsonSerializer<SerializableConfig> configDeserializer = new ConfigSerializer();
        builder.registerTypeAdapter(SerializableConfig.class, configDeserializer);
        builder.registerTypeAdapter(SerializableConfig.class, configSerializer);
    }

    public static void saveConfig(){
        saveConfig(EmoteInstance.config);
    }

    public static void saveConfig(SerializableConfig config){
        try{
            BufferedWriter writer = Files.newBufferedWriter(EmoteInstance.instance.getConfigPath());
            serializer.toJson(config, writer);
            writer.close();
            //FileUtils.write(Main.CONFIGPATH, Serializer.serializer.toJson(Main.config), "UTF-8", false);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Reads the config from ~/config/emotecraft.json or yaml
     * @return config
     */
    public static SerializableConfig getConfig(){
        return INSTANCE.readConfig(EmoteInstance.instance.getConfigPath());
    }

    /**
     * Reads the config and creates a new file, if needed
     * @param path config path
     * @return config
     */
    protected SerializableConfig readConfig(Path path){
        if(path.toFile().isFile()){
            BufferedReader reader = null;
            try{
                reader = Files.newBufferedReader(path);
                SerializableConfig config = readConfig(reader);
                reader.close();
                return config;
            }catch (IOException | JsonParseException e){
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Failed to read config: " + e.getMessage(), true);
                e.printStackTrace();
            }
        }
        else{
            try{
                BufferedWriter writer = Files.newBufferedWriter(path);
                SerializableConfig config = readConfig((BufferedReader) null);
                saveConfig(config);
                return config;
            }
            catch (IOException e){
                EmoteInstance.instance.getLogger().log(Level.WARNING, "Failed to create config file: " + e.getMessage(), true);
                e.printStackTrace();
            }
        }
        return readConfig((BufferedReader) null);
    }

    protected SerializableConfig readConfig(BufferedReader reader) throws JsonSyntaxException, JsonIOException{
        if(reader != null) return serializer.fromJson(reader, SerializableConfig.class);
        return new SerializableConfig();
    }
}
