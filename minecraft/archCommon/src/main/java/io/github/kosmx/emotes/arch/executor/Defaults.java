package io.github.kosmx.emotes.arch.executor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.common.CommonData;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class Defaults {
    @Deprecated
    public InputConstants.Key getUnknownKey() {
        return InputConstants.UNKNOWN;
    }


    @Deprecated
    public InputConstants.Key getKeyFromString(String str) {
        return InputConstants.getKey(str);
    }

    @Deprecated
    public InputConstants.Key getKeyFromCode(int keyCode, int scanCode) {
        return InputConstants.getKey(keyCode, scanCode);
    }

    @Deprecated
    public InputConstants.Key getMouseKeyFromCode(int keyCode) {
        return InputConstants.Type.MOUSE.getOrCreate(keyCode);
    }

    @Deprecated
    public MutableComponent textFromString(String str) {
        return Component.literal(str);
    }

    @Deprecated
    public MutableComponent fromJson(JsonElement node) {
        return Component.Serializer.fromJson(node);
    }

    @Deprecated
    public MutableComponent newTranslationText(String key) {
        return Component.translatable(key);
    }

    @Deprecated
    public Component defaultTextsDone() {
        return CommonComponents.GUI_DONE;
    }

    @Deprecated
    public Component defaultTextCancel() {
        return CommonComponents.GUI_CANCEL;
    }

    @Deprecated
    public static ResourceLocation newIdentifier(String namespace, String id) {
        return new ResourceLocation(namespace, id);
    }

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
        return newIdentifier(CommonData.MOD_ID, id);
    }
}
