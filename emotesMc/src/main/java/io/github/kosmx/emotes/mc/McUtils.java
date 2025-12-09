package io.github.kosmx.emotes.mc;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.kosmx.emotes.common.CommonData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

public class McUtils {
    public static final Component MOD_NAME = Component.literal(CommonData.MOD_NAME);
    public static final Component SLASH = Component.literal("/");

    public static Component fromJson(String json, HolderLookup.Provider registries) {
        if (json == null || json.isBlank()) return CommonComponents.EMPTY;

        try {
            return ComponentSerialization.CODEC.parse(
                    registries.createSerializationContext(JsonOps.INSTANCE),
                    JsonParser.parseString(json)
            ).getOrThrow();
        } catch (Throwable e) {
            return Component.nullToEmpty(json);
        }
    }

    public static Component fromJson(Object obj) {
        return McUtils.fromJson(obj, RegistryAccess.EMPTY);
    }

    public static Component fromJson(Object obj, HolderLookup.Provider registries) {
        return switch (obj) {
            case null -> CommonComponents.EMPTY;

            case String string -> McUtils.fromJson(string, registries);

            case JsonElement element -> ComponentSerialization.CODEC.parse(
                    registries.createSerializationContext(JsonOps.INSTANCE), element
            ).getOrThrow();

            default -> throw new IllegalArgumentException("Can not create Text from " + obj.getClass().getName());
        };
    }

    public static Identifier newIdentifier(String id) {
        return Identifier.fromNamespaceAndPath(CommonData.MOD_ID, id);
    }
}
