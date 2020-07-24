package com.kosmx.emotecraft.config;

import com.google.gson.*;
import com.kosmx.emotecraft.Main;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.InputUtil;
import org.apache.logging.log4j.Level;

import java.lang.reflect.Type;

public class ConfigSerializer implements JsonDeserializer<SerializableConfig>, JsonSerializer<SerializableConfig> {

    @Override
    public SerializableConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject node = json.getAsJsonObject();
        SerializableConfig config = new SerializableConfig();
        if(node.has("showDebug"))config.showDebug = node.get("showDebug").getAsBoolean();
        if(node.has("validate"))config.showDebug = node.get("validate").getAsBoolean();
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)clientDeserialize(node, config);
        return config;
    }

    @Environment(EnvType.CLIENT)
    private void clientDeserialize(JsonObject node, SerializableConfig config){
        if(node.has("dark"))config.dark = node.get("dark").getAsBoolean();
        if(node.has("fastmenu"))fastMenuDeserializer(node.get("fastmenu").getAsJsonObject(), config);
        if(node.has("keys"))keyBindsDeserializer(node.get("keys").getAsJsonArray(), config);
    }
    @Environment(EnvType.CLIENT)
    private void fastMenuDeserializer(JsonObject node, SerializableConfig config){
        for(int i = 0;i != 8; i++){
            if(node.has(Integer.toString(i))){
                EmoteHolder emote = EmoteHolder.getEmoteFromHash(node.get(Integer.toString(i)).getAsInt());
                config.fastMenuEmotes[i] = emote;
                if(emote == null){
                    Main.log(Level.ERROR, "Can't find emote from hash: " + node.get(Integer.toString(i)).getAsInt());
                }
            }
        }
    }
    @Environment(EnvType.CLIENT)
    private void keyBindsDeserializer(JsonArray node, SerializableConfig config){
        for(JsonElement object:node){
            keyBindDeserializer(object.getAsJsonObject());
        }
        EmoteHolder.bindKeys(config);
    }
    @Environment(EnvType.CLIENT)
    private void keyBindDeserializer(JsonObject node){
        EmoteHolder emote = EmoteHolder.getEmoteFromHash(node.get("id").getAsInt());
        if(emote != null){
            emote.keyBinding = InputUtil.fromTranslationKey(node.get("key").getAsString());
        }
        else {
            Main.log(Level.ERROR, "Can't find emote from hash: " + node.get("id").getAsInt());
        }
    }

    @Override
    public JsonElement serialize(SerializableConfig config, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject node = new JsonObject();
        node.addProperty("showDebug", config.showDebug);
        node.addProperty("validate", config.showDebug);
        if(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) clientSerialize(config, node);
        return node;
    }

    @Environment(EnvType.CLIENT)
    private void clientSerialize(SerializableConfig config, JsonObject node){
        node.addProperty("dark", config.dark);
        node.add("fastmenu", fastMenuSerializer(config));
        node.add("keys", keyBindsSerializer(config));
    }
    @Environment(EnvType.CLIENT)
    private JsonObject fastMenuSerializer(SerializableConfig config){
        JsonObject node = new JsonObject();
        for (int i = 0; i != 8; i++){
            if(config.fastMenuEmotes[i] != null){
                node.addProperty(Integer.toString(i), config.fastMenuEmotes[i].hash);
            }
        }
        return node;
    }
    @Environment(EnvType.CLIENT)
    private JsonArray keyBindsSerializer(SerializableConfig config){
        JsonArray array = new JsonArray();
        for(EmoteHolder emote:config.emotesWithKey){
            array.add(keyBindSerializer(emote));
        }
        return array;
    }
    @Environment(EnvType.CLIENT)
    private JsonObject keyBindSerializer(EmoteHolder emote){
        JsonObject node = new JsonObject();
        node.addProperty("id", emote.hash);
        node.addProperty("key", emote.keyBinding.getTranslationKey());
        return node;
    }

}
