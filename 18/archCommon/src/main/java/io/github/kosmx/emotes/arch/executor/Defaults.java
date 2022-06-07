package io.github.kosmx.emotes.arch.executor;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.arch.executor.types.IdentifierImpl;
import io.github.kosmx.emotes.arch.executor.types.Key;
import io.github.kosmx.emotes.arch.executor.types.TextImpl;
import io.github.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import io.github.kosmx.emotes.executor.dataTypes.IIdentifier;
import io.github.kosmx.emotes.executor.dataTypes.InputKey;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class Defaults implements IDefaultTypes {
    @Override
    public InputKey getUnknownKey() {
        return new Key(InputConstants.UNKNOWN);
    }

    @Override
    public InputKey getKeyFromString(String str) {
        return new Key(InputConstants.getKey(str));
    }

    @Override
    public InputKey getKeyFromCode(int keyCode, int scanCode) {
        return new Key(InputConstants.getKey(keyCode, scanCode));
    }

    @Override
    public InputKey getMouseKeyFromCode(int keyCode) {
        return new Key(InputConstants.Type.MOUSE.getOrCreate(keyCode));
    }

    @Override
    public Text emptyTex() {
        return new TextImpl(TextComponent.EMPTY.plainCopy());
    }

    @Override
    public Text textFromString(String str) {
        return new TextImpl(new TextComponent(str));
    }

    @Override
    public Text fromJson(JsonElement node) {
        return new TextImpl(net.minecraft.network.chat.Component.Serializer.fromJson(node));
    }

    @Override
    public Text newTranslationText(String key) {
        return new TextImpl(new TranslatableComponent(key));
    }

    @Override
    public Text defaultTextsDone() {
        return new TextImpl(CommonComponents.GUI_DONE.plainCopy());
    }

    @Override
    public Text defaultTextCancel() {
        return new TextImpl(CommonComponents.GUI_CANCEL.plainCopy());
    }

    @Override
    public IIdentifier newIdentifier(String namespace, String id) {
        return new IdentifierImpl(new net.minecraft.resources.ResourceLocation(namespace, id));
    }
}
