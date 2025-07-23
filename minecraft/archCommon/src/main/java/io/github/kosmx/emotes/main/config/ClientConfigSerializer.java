package io.github.kosmx.emotes.main.config;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.server.config.ConfigSerializer;
import it.unimi.dsi.fastutil.Pair;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class ClientConfigSerializer extends ConfigSerializer<ClientConfig> {
    public ClientConfigSerializer() {
        super(ClientConfig::new);
    }

    @Override
    public ClientConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ClientConfig config = super.deserialize(json, typeOfT, context);
        clientDeserialize(json.getAsJsonObject(), config);
        return config;
    }

    @Override
    public JsonElement serialize(ClientConfig config, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject node = super.serialize(config, typeOfSrc, context).getAsJsonObject();
        clientSerialize(config, node);
        return node;
    }

    private void clientDeserialize(JsonObject node, ClientConfig config) {
        if(node.has("fastmenu")) fastMenuDeserializer(node.get("fastmenu").getAsJsonObject(), config);
        if(node.has("keys")) keyBindsDeserializer(node.get("keys"), config);
    }

    private void fastMenuDeserializer(JsonObject node, ClientConfig config) {
        for(int j = 0; j < config.fastMenuEmotes.length; j++){
            if (node.has(Integer.toString(j))) {
                JsonElement subNode = node.get(Integer.toString(j));
                // fastmenu config version check
                if (subNode.isJsonObject()) {
                    // new version (with pages)
                    for (int i = 0; i != 8; i++) {
                        if (node.get(Integer.toString(j)).getAsJsonObject().has(Integer.toString(i))) {
                            config.fastMenuEmotes[j][i] = getEmoteID(node.get(Integer.toString(j)).getAsJsonObject().get(Integer.toString(i)));
                        }
                    }
                } else {
                    // old version (without pages) to new version
                    config.fastMenuEmotes[0][j] = getEmoteID(node.get(Integer.toString(j)));
                }
            }
        }
    }

    private void keyBindsDeserializer(JsonElement node, ClientConfig config) {
        if (config.configVersion < 4) {
            oldKeyBindsSerializer(node.getAsJsonArray(), config);
        } else {
            for (Map.Entry<String, JsonElement> element : node.getAsJsonObject().entrySet()) {
                String str = element.getValue().getAsString();
                config.emoteKeyMap.put(UUID.fromString(element.getKey()), InputConstants.getKey(str));
            }
        }
    }

    private void oldKeyBindsSerializer(JsonArray node, ClientConfig config) {
        for(JsonElement jsonElement : node){
            JsonObject n = jsonElement.getAsJsonObject();
            String str = n.get("key").getAsString();
            config.emoteKeyMap.add(Pair.of(getEmoteID(n.get("id")), InputConstants.getKey(str)));
        }
    }

    private void clientSerialize(ClientConfig config, JsonObject node){
        node.add("fastmenu", fastMenuSerializer(config));
        node.add("keys", keyBindsSerializer(config));
    }

    private JsonObject fastMenuSerializer(ClientConfig config){
        JsonObject node = new JsonObject();
        for(int j = 0; j < config.fastMenuEmotes.length; j++){
            if (config.fastMenuEmotes[j] != null) {
                JsonObject subNode = new JsonObject();
                for (int i = 0; i != 8; i++) {
                    if (config.fastMenuEmotes[j][i] != null) {
                        subNode.addProperty(Integer.toString(i), config.fastMenuEmotes[j][i].toString());
                        node.add(Integer.toString(j), subNode);
                    }
                }
            }
        }
        return node;
    }

    private JsonObject keyBindsSerializer(ClientConfig config){
        JsonObject array = new JsonObject();
        for(Pair<UUID, InputConstants.Key> emote : config.emoteKeyMap){
            array.addProperty(emote.left().toString(), emote.right().getName());
        }
        return array;
    }

    public static UUID getEmoteID(JsonElement element) {
        try {
            return UUID.fromString(element.getAsString());
        } catch(Exception e) {
            return new UUID(0, 0);
        }
    }
}
