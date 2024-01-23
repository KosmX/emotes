package io.github.kosmx.emotes.arch.executor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.kosmx.emotes.common.CommonData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class Defaults {

    public static Component fromJson(String json){
        if(json == null){
            return Component.literal("");
        }
        try {
            return Component.Serializer.fromJson(JsonParser.parseString(json));
        }catch (JsonParseException e){
            return Component.literal(json);
        }
    }

    public static Component fromJson(Object obj) {
        if (obj == null || obj instanceof String) {
            return fromJson((String) obj);
        } else if (obj instanceof JsonElement) {
            return Component.Serializer.fromJson((JsonElement) obj);
        } else throw new IllegalArgumentException("Can not create Text from " + obj.getClass().getName());
    }

    public static ResourceLocation newIdentifier(String id){
        return new ResourceLocation(CommonData.MOD_ID, id);
    }
}
