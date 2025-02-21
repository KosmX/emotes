package io.github.kosmx.emotes.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.server.services.InstanceService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * Serialize Emotecraft related jsons but not animations
 */
public class Serializer {
    public static Serializer INSTANCE;

    public final Gson serializer;
    private SerializableConfig config;

    public Serializer() {
        GsonBuilder builder = new GsonBuilder().setPrettyPrinting();
        initializeSerializer(builder);
        this.serializer = builder.create();
    }

    public void initializeSerializer(GsonBuilder builder) {
        builder.registerTypeAdapter(SerializableConfig.class, new ConfigSerializer());
    }

    public void saveConfig() {
        if (saveConfig(this.config)) {
            this.config = null;
        }
    }

    public boolean saveConfig(SerializableConfig config) {
        try (BufferedWriter writer = Files.newBufferedWriter(InstanceService.INSTANCE.getConfigPath())) {
            this.serializer.toJson(config, writer);
            return true;
        } catch(IOException e) {
            LoggerService.INSTANCE.log(Level.WARNING, "Failed to save config!", e);
            return false;
        }
    }

    public SerializableConfig readConfig() {
        if (this.config == null) {
            LoggerService.INSTANCE.log(Level.INFO, "Loading config...");
            this.config = readConfig(InstanceService.INSTANCE.getConfigPath());
        }
        return this.config;
    }

    protected SerializableConfig readConfig(Path path) {
        if (Files.isRegularFile(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                return readConfig(reader);
            } catch(IOException | JsonParseException e) {
                LoggerService.INSTANCE.log(Level.WARNING, "Failed to read config!", e);
                LoggerService.INSTANCE.log(Level.WARNING, "If you want to regenerate the config, delete the old files!");
            }
        } else {
            SerializableConfig config = readConfig((BufferedReader) null);
            saveConfig(config);
            return config;
        }
        return readConfig((BufferedReader) null);
    }

    protected SerializableConfig readConfig(BufferedReader reader) throws JsonSyntaxException, JsonIOException {
        if(reader != null){
            SerializableConfig config = serializer.fromJson(reader, SerializableConfig.class);
            if (config == null) throw new JsonParseException("Json is empty");
            return config;
        }
        return new SerializableConfig();
    }

    // Static helpers

    public static Gson getSerializer() {
        return Serializer.INSTANCE.serializer;
    }

    public static SerializableConfig getConfig() {
        return Serializer.INSTANCE.readConfig();
    }
}
