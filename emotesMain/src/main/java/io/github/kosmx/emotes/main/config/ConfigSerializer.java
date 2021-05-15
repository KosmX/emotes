package io.github.kosmx.emotes.main.config;

import com.google.gson.*;
import io.github.kosmx.emotes.common.tools.Pair;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;

import java.lang.reflect.Type;
import java.util.logging.Level;

public class ConfigSerializer implements JsonDeserializer<SerializableConfig>, JsonSerializer<SerializableConfig> {

    @Override
    public SerializableConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException{
        JsonObject node = json.getAsJsonObject();
        SerializableConfig config = EmoteInstance.instance.isClient() ? new ClientConfig() : new SerializableConfig();
        config.configVersion = SerializableConfig.staticConfigVersion;
        if(node.has("config_version"))config.configVersion = node.get("config_version").getAsInt();
        if(config.configVersion < SerializableConfig.staticConfigVersion){
            EmoteInstance.instance.getLogger().log(Level.INFO, "Serializing config with older version.", true);
        }
        else if(config.configVersion > SerializableConfig.staticConfigVersion){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "You are trying to load version "+ config.configVersion + " config. The mod can only load correctly up to v" + SerializableConfig.staticConfigVersion+". If you won't modify any config, I won't overwrite your config file.", true);
        }

        config.iterate(entry -> deserializeEntry(entry, node));

        if(EmoteInstance.instance.isClient()) clientDeserialize(node, (ClientConfig) config);
        return config;
    }

    private void deserializeEntry(SerializableConfig.ConfigEntry<?> entry, JsonObject node){
        String id = null;
        if(node.has(entry.getName())){
            id = entry.getName();
        }
        else if(node.has(entry.getOldConfigName())){
            id = entry.getOldConfigName();
        }
        if(id != null){
            JsonElement element = node.get(id);
            if(entry instanceof SerializableConfig.BooleanConfigEntry){
                ((SerializableConfig.BooleanConfigEntry)entry).set(element.getAsBoolean());
            }
            else if(entry instanceof SerializableConfig.FloatConfigEntry){
                ((SerializableConfig.FloatConfigEntry)entry).set(element.getAsFloat());
            }
        }
    }

    private void clientDeserialize(JsonObject node, ClientConfig config){
        EmoteFixer emoteFixer = new EmoteFixer(config.configVersion);
        if(node.has("fastmenu")) fastMenuDeserializer(node.get("fastmenu").getAsJsonObject(), config, emoteFixer);
        if(node.has("keys")) keyBindsDeserializer(node.get("keys").getAsJsonArray(), config, emoteFixer);
    }

    private void fastMenuDeserializer(JsonObject node, ClientConfig config, EmoteFixer fixer){
        for(int i = 0; i != 8; i++){
            if(node.has(Integer.toString(i))){
                config.fastMenuHash[i] = fixer.getEmoteID(node.get(Integer.toString(i)));
            }
        }
    }

    private void keyBindsDeserializer(JsonArray node, ClientConfig config, EmoteFixer fixer){
        for(JsonElement object : node){
            JsonObject n = object.getAsJsonObject();
            config.emotesWithHash.add(new Pair<>(fixer.getEmoteID(n.get("id")), n.get("key").getAsString()));
            //keyBindDeserializer(object.getAsJsonObject());
        }
    }

    @Override
    public JsonElement serialize(SerializableConfig config, Type typeOfSrc, JsonSerializationContext context){
        JsonObject node = new JsonObject();
        node.addProperty("config_version", SerializableConfig.staticConfigVersion); //I always save config with the latest format.
        config.iterate(entry -> serializeEntry(entry, node));
        if(config instanceof ClientConfig) clientSerialize((ClientConfig) config, node);
        return node;
    }

    private void serializeEntry(SerializableConfig.ConfigEntry<?> entry, JsonObject node){
        if(entry instanceof SerializableConfig.BooleanConfigEntry){
            node.addProperty(entry.getName(), ((SerializableConfig.BooleanConfigEntry) entry).get());
        }
        else if(entry instanceof SerializableConfig.FloatConfigEntry){
            node.addProperty(entry.getName(), (float)((SerializableConfig.FloatConfigEntry) entry).get());
        }
    }

    private void clientSerialize(ClientConfig config, JsonObject node){
        node.add("fastmenu", fastMenuSerializer(config));
        node.add("keys", keyBindsSerializer(config));
    }

    private JsonObject fastMenuSerializer(ClientConfig config){
        JsonObject node = new JsonObject();
        for(int i = 0; i != 8; i++){
            if(config.fastMenuEmotes[i] != null){
                node.addProperty(Integer.toString(i), config.fastMenuEmotes[i].hash);
            }
        }
        return node;
    }

    private JsonArray keyBindsSerializer(ClientConfig config){
        JsonArray array = new JsonArray();
        for(EmoteHolder emote : config.emotesWithKey){
            array.add(keyBindSerializer(emote));
        }
        return array;
    }

    private JsonObject keyBindSerializer(EmoteHolder emote){
        JsonObject node = new JsonObject();
        node.addProperty("id", emote.hash);
        node.addProperty("key", emote.keyBinding.getTranslationKey());
        return node;
    }

}
