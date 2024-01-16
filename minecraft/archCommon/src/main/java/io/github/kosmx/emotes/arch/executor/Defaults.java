package io.github.kosmx.emotes.arch.executor;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.arch.executor.types.IdentifierImpl;
import io.github.kosmx.emotes.arch.executor.types.Key;
import io.github.kosmx.emotes.arch.executor.types.TextImpl;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.inline.dataTypes.IIdentifier;
import io.github.kosmx.emotes.inline.dataTypes.InputKey;
import io.github.kosmx.emotes.inline.dataTypes.Text;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class Defaults {
    public InputKey getUnknownKey() {
        return new Key(InputConstants.UNKNOWN);
    }

    public InputKey getKeyFromString(String str) {
        return new Key(InputConstants.getKey(str));
    }

    public InputKey getKeyFromCode(int keyCode, int scanCode) {
        return new Key(InputConstants.getKey(keyCode, scanCode));
    }

    public InputKey getMouseKeyFromCode(int keyCode) {
        return new Key(InputConstants.Type.MOUSE.getOrCreate(keyCode));
    }

    public Text emptyTex() {
        return new TextImpl(Component.empty().plainCopy());
    }

    public Text textFromString(String str) {
        return new TextImpl(Component.literal(str));
    }

    public Text fromJson(JsonElement node) {
        return new TextImpl(net.minecraft.network.chat.Component.Serializer.fromJson(node));
    }

    public Text newTranslationText(String key) {
        return new TextImpl(Component.translatable(key));
    }

    public Text defaultTextsDone() {
        return new TextImpl(CommonComponents.GUI_DONE.plainCopy());
    }

    public Text defaultTextCancel() {
        return new TextImpl(CommonComponents.GUI_CANCEL.plainCopy());
    }

    public IIdentifier newIdentifier(String namespace, String id) {
        return new IdentifierImpl(new net.minecraft.resources.ResourceLocation(namespace, id));
    }

    public Text fromJson(String json){
        if(json == null){
            return textFromString("");
        }
        try {
            return fromJson(new JsonParser().parse(json));
        }catch (JsonParseException e){
            return textFromString(json);
        }
    }

    public Text fromJson(Object obj) {
        if (obj == null || obj instanceof String) {
            return fromJson((String) obj);
        } else if (obj instanceof JsonElement) {
            return fromJson((JsonElement) obj);
        } else throw new IllegalArgumentException("Can not create Text from " + obj.getClass().getName());
    }

    public IIdentifier newIdentifier(String id){
        return newIdentifier(CommonData.MOD_ID, id);
    }
}
