package io.github.kosmx.emotes.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.server.services.InstanceService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Serialize Emotecraft related jsons but not animations
 */
public class Serializer<T extends SerializableConfig> {
    public static Serializer<?> INSTANCE;

    protected final ConfigSerializer<T> configSerializer;
    protected final Class<T> configClass;
    private final Consumer<GsonBuilder> consumer;

    protected final Gson serializer;
    private T config;

    public Serializer(ConfigSerializer<T> configSuppler, Class<T> configClass) {
        this(configSuppler, configClass, GsonBuilder::setPrettyPrinting);
    }

    protected Serializer(ConfigSerializer<T> configSuppler, Class<T> configClass, Consumer<GsonBuilder> consumer) {
        this.configSerializer = configSuppler;
        this.configClass = configClass;
        this.consumer = consumer;

        this.serializer = initializeSerializer(new GsonBuilder());
    }

    protected Gson initializeSerializer(GsonBuilder builder) {
        builder.registerTypeAdapter(this.configClass, this.configSerializer);
        if (this.consumer != null) this.consumer.accept(builder);
        return builder.disableHtmlEscaping().create();
    }

    public boolean saveConfig() {
        return saveConfig(this.config);
    }

    public boolean saveConfig(T config) {
        try (BufferedWriter writer = Files.newBufferedWriter(InstanceService.INSTANCE.getConfigPath())) {
            this.serializer.toJson(config, this.configClass, writer);
            return true;
        } catch(IOException e) {
            CommonData.LOGGER.error("Failed to save config!", e);
            return false;
        }
    }

    public T readConfig() {
        if (this.config == null) {
            CommonData.LOGGER.debug("Loading config...");
            this.config = readConfig(InstanceService.INSTANCE.getConfigPath());
        }
        return this.config;
    }

    protected T readConfig(Path path) {
        if (Files.isRegularFile(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                return readConfig(reader);
            } catch(IOException | JsonParseException e) {
                CommonData.LOGGER.warn("Failed to read config!", e);
                CommonData.LOGGER.warn("If you want to regenerate the config, delete the old files!");
            }
        } else {
            T config = readConfig((BufferedReader) null);
            saveConfig(config);
            return config;
        }
        return readConfig((BufferedReader) null);
    }

    protected T readConfig(BufferedReader reader) throws JsonSyntaxException, JsonIOException {
        if (reader != null) {
            T config = this.serializer.fromJson(reader, this.configClass);

            if (config == null) {
                throw new JsonParseException("Json is empty");
            } else {
                return config;
            }
        } else {
            return this.configSerializer.configSuppler.get();
        }
    }

    // Static helpers

    public static SerializableConfig getConfig() {
        return Serializer.INSTANCE.readConfig();
    }
}
