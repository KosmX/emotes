package io.github.kosmx.emotes.mc;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.kosmx.emotes.common.CommonData;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class McUtils {
    public static final Component SLASH = Component.literal("/");

    public static Component fromJson(String json, HolderLookup.Provider registries) {
        if (json == null) {
            return Component.empty();
        }

        try {
            return Component.Serializer.fromJson(JsonParser.parseString(json), registries);
        } catch (Throwable e) {
            return Component.nullToEmpty(json);
        }
    }

    public static Component fromJson(Object obj, HolderLookup.Provider registries) {
        return switch (obj) {
            case null -> Component.empty();

            case String string -> McUtils.fromJson(string, registries);

            case JsonElement element -> Component.Serializer.fromJson(element, registries);

            default -> throw new IllegalArgumentException("Can not create Text from " + obj.getClass().getName());
        };
    }

    public static ResourceLocation newIdentifier(String id){
        return ResourceLocation.fromNamespaceAndPath(CommonData.MOD_ID, id);
    }
}
