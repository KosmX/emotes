package io.github.kosmx.emotes.server.serializer;

import com.google.gson.*;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.common.tools.BiMap;

import java.lang.reflect.Type;
import java.util.UUID;

public class BiMapSerializer implements JsonSerializer<BiMap<UUID, UUID>>, JsonDeserializer<BiMap<UUID, UUID>> {
    @Override
    public JsonElement serialize(BiMap<UUID, UUID> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("bedrock-emote", "java-emote"); //just for a little help.
        for(Pair<UUID, UUID> pair:src){
            jsonObject.addProperty(pair.getLeft().toString(), pair.getRight().toString());
        }
        return jsonObject;
    }

    @Override
    public BiMap<UUID, UUID> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final BiMap<UUID, UUID> map = new BiMap<>();
        json.getAsJsonObject().entrySet().forEach(entry -> {
            if(entry.getKey().equals("bedrock-emote")) return;
            try {
                map.put(UUID.fromString(entry.getKey()), UUID.fromString(entry.getValue().getAsString()));
            }catch (IllegalArgumentException ignore){}
        });
        return map;
    }
}
