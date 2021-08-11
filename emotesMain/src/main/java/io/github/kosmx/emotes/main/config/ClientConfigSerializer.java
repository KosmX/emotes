package io.github.kosmx.emotes.main.config;

import com.google.gson.*;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.server.config.ConfigSerializer;

import java.lang.reflect.Type;


public class ClientConfigSerializer extends ConfigSerializer {

    @Override
    public SerializableConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ClientConfig config = (ClientConfig) super.deserialize(json, typeOfT, context);

        clientDeserialize(json.getAsJsonObject(), config);

        return config;
    }

    @Override
    public JsonElement serialize(SerializableConfig config, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject node = super.serialize(config, typeOfSrc, context).getAsJsonObject();

        if(config instanceof ClientConfig) clientSerialize((ClientConfig) config, node);

        return node;
    }

    @Override
    protected SerializableConfig newConfig() {
        return new ClientConfig();
    }




    private void clientDeserialize(JsonObject node, SerializableConfig sconfig) {
        ClientConfig config = (ClientConfig) sconfig;
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

    private void clientSerialize(ClientConfig config, JsonObject node){
        node.add("fastmenu", fastMenuSerializer(config));
        node.add("keys", keyBindsSerializer(config));
    }

    private JsonObject fastMenuSerializer(ClientConfig config){
        JsonObject node = new JsonObject();
        for(int i = 0; i != 8; i++){
            if(config.fastMenuEmotes[i] != null){
                node.addProperty(Integer.toString(i), config.fastMenuEmotes[i].hashCode());
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
        node.addProperty("id", emote.hashCode());
        node.addProperty("key", emote.keyBinding.getTranslationKey());
        return node;
    }
}
