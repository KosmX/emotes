package io.github.kosmx.emotes.server.config;

import com.google.gson.*;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.SerializableConfig;

import java.lang.reflect.Type;
import java.util.function.Supplier;

public class ConfigSerializer<T extends SerializableConfig> implements JsonDeserializer<T>, JsonSerializer<T> {
    protected final Supplier<T> configSuppler;
    protected final int currentConfigVersion;

    public ConfigSerializer(Supplier<T> configSuppler, int version) {
        this.configSuppler = configSuppler;
        this.currentConfigVersion = version;
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
        JsonObject node = json.getAsJsonObject();
        T config = this.configSuppler.get();
        config.configVersion = this.currentConfigVersion;
        if (node.has("config_version"))
            config.configVersion = node.get("config_version").getAsInt();

        if (config.configVersion < this.currentConfigVersion) {
            CommonData.LOGGER.debug("Serializing config with older version...");

        } else if (config.configVersion > this.currentConfigVersion) {
            CommonData.LOGGER.warn("You are trying to load version {} config. The mod can only load correctly up to {}. If you won't modify any config, I won't overwrite your config file.", config.configVersion, this.currentConfigVersion);
        }

        config.iterate(entry -> deserializeEntry(entry, node, context));

        return config;
    }

    protected <E> void deserializeEntry(SerializableConfig.ConfigEntry<E> entry, JsonObject node, JsonDeserializationContext context) {
        String id = null;
        if (node.has(entry.getName())) {
            id = entry.getName();
        } else {
            String old = entry.getOldConfigName();
            if (old != null && node.has(old)) {
                id = old;
            }
        }

        if (id == null) return;
        entry.set(context.deserialize(node.get(id), entry.get().getClass()));
    }

    @Override
    public JsonElement serialize(SerializableConfig config, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject node = new JsonObject();
        node.addProperty("config_version", this.currentConfigVersion); //I always save config with the latest format.
        config.iterate(entry -> serializeEntry(entry, node, context));
        return node;
    }

    protected <E> void serializeEntry(SerializableConfig.ConfigEntry<E> entry, JsonObject node, JsonSerializationContext context) {
        node.add(entry.getName(), context.serialize(entry.get()));
    }
}
