package io.github.kosmx.emotes.main.config;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.serializer.gson.AnimationTypeAdapter;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.server.config.ConfigSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientConfigSerializer extends ConfigSerializer<ClientConfig> {
    public ClientConfigSerializer() {
        super(ClientConfig::new, ClientConfig.staticConfigVersion);
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
        if (node.has("fastmenu")) fastMenuDeserializer(node.get("fastmenu").getAsJsonObject(), config);
        if (node.has("keys")) keyBindsDeserializer(node.get("keys"), config);
    }

    private void fastMenuDeserializer(JsonObject node, ClientConfig config) {
        boolean legacy = config.configVersion < 5; // pre-animation format stored emote UUIDs, resolved after emotes load
        if (legacy) config.legacyFastMenu = new UUID[config.fastMenuEmotes.length][8];

        for (int j = 0; j < config.fastMenuEmotes.length; j++) {
            if (!node.has(Integer.toString(j))) continue;
            JsonElement subNode = node.get(Integer.toString(j));

            if (subNode.isJsonObject()) { // paged format
                JsonObject page = subNode.getAsJsonObject();
                for (int i = 0; i < 8; i++) {
                    if (!page.has(Integer.toString(i))) continue;
                    JsonElement slot = page.get(Integer.toString(i));
                    if (legacy) config.legacyFastMenu[j][i] = getEmoteID(slot);
                    else config.fastMenuEmotes[j][i] = readEmote(slot);
                }
            } else if (legacy) { // very old single-page format
                config.legacyFastMenu[0][j] = getEmoteID(subNode);
            }
        }
    }

    private void keyBindsDeserializer(JsonElement node, ClientConfig config) {
        if (config.configVersion < 5) { // legacy UUID bindings — resolved to holders after emotes load
            config.legacyKeyBinds = new HashMap<>();
            if (config.configVersion < 4) {
                for (JsonElement element : node.getAsJsonArray()) {
                    JsonObject n = element.getAsJsonObject();
                    config.legacyKeyBinds.put(InputConstants.getKey(n.get("key").getAsString()), getEmoteID(n.get("id")));
                }
            } else {
                for (Map.Entry<String, JsonElement> element : node.getAsJsonObject().entrySet()) {
                    config.legacyKeyBinds.put(InputConstants.getKey(element.getValue().getAsString()), UUID.fromString(element.getKey()));
                }
            }
            return;
        }

        for (Map.Entry<String, JsonElement> element : node.getAsJsonObject().entrySet()) {
            EmoteHolder holder = readEmote(element.getValue());
            if (holder != null) config.keyBinds.put(InputConstants.getKey(element.getKey()), holder);
        }
    }

    private void clientSerialize(ClientConfig config, JsonObject node) {
        node.add("fastmenu", fastMenuSerializer(config));
        node.add("keys", keyBindsSerializer(config));
    }

    private JsonObject fastMenuSerializer(ClientConfig config) {
        JsonObject node = new JsonObject();
        for (int j = 0; j < config.fastMenuEmotes.length; j++) {
            if (config.fastMenuEmotes[j] == null) continue;

            JsonObject page = new JsonObject();
            for (int i = 0; i < 8; i++) {
                EmoteHolder holder = config.fastMenuEmotes[j][i];
                if (holder != null) page.addProperty(Integer.toString(i), AnimationTypeAdapter.toBase64(holder.emote));
            }
            if (!page.entrySet().isEmpty()) node.add(Integer.toString(j), page);
        }
        return node;
    }

    private JsonObject keyBindsSerializer(ClientConfig config) {
        JsonObject node = new JsonObject();
        for (Map.Entry<InputConstants.Key, EmoteHolder> bind : config.keyBinds.entrySet()) {
            node.addProperty(bind.getKey().getName(), AnimationTypeAdapter.toBase64(bind.getValue().emote));
        }
        return node;
    }

    private static EmoteHolder readEmote(JsonElement element) {
        try {
            return new EmoteHolder(AnimationTypeAdapter.fromBase64(element.getAsString()));
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Failed to read a bound emote!", th);
            return null;
        }
    }

    public static UUID getEmoteID(JsonElement element) {
        try {
            return UUID.fromString(element.getAsString());
        } catch (Exception e) {
            return new UUID(0, 0);
        }
    }
}
