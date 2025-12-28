package org.redlance.dima_dencep.mods.emotecraft.geyser.utils.resourcepack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Collection;

public final class CollectionAdapter implements JsonSerializer<Collection<?>> {
    @Override
    public JsonElement serialize(Collection<?> src, Type typeOfSrc, JsonSerializationContext ctx) {
        if (src == null || src.isEmpty()) return null;
        JsonArray array = new JsonArray();
        for (Object child : src) {
            array.add(ctx.serialize(child));
        }
        return array;
    }
}

