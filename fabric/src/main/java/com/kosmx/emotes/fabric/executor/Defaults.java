package com.kosmx.emotes.fabric.executor;

import com.google.gson.JsonElement;
import com.kosmx.emotes.executor.dataTypes.IDefaultTypes;
import com.kosmx.emotes.executor.dataTypes.IIdentifier;
import com.kosmx.emotes.executor.dataTypes.InputKey;
import com.kosmx.emotes.executor.dataTypes.Text;
import com.kosmx.emotes.fabric.executor.types.IdentifierImpl;
import com.kosmx.emotes.fabric.executor.types.Key;
import com.kosmx.emotes.fabric.executor.types.TextImpl;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

public class Defaults implements IDefaultTypes {
    @Override
    public InputKey getUnknownKey() {
        return new Key(InputUtil.UNKNOWN_KEY);
    }

    @Override
    public InputKey getKeyFromString(String str) {
        return new Key(InputUtil.fromTranslationKey(str));
    }

    @Override
    public InputKey getKeyFromCode(int keyCode, int scanCode) {
        return new Key(InputUtil.fromKeyCode(keyCode, scanCode));
    }

    @Override
    public InputKey getMouseKeyFromCode(int keyCode) {
        return new Key(InputUtil.Type.MOUSE.createFromCode(keyCode));
    }

    @Override
    public Text emptyTex() {
        return new TextImpl(LiteralText.EMPTY.copy());
    }

    @Override
    public Text textFromString(String str) {
        return new TextImpl(new LiteralText(str));
    }

    @Override
    public Text fromJson(JsonElement node) {
        return new TextImpl(net.minecraft.text.Text.Serializer.fromJson(node));
    }

    @Override
    public Text newTranslationText(String key) {
        return new TextImpl(new TranslatableText(key));
    }

    @Override
    public Text defaultTextsDone() {
        return new TextImpl(ScreenTexts.DONE.copy());
    }

    @Override
    public Text defaultTextCancel() {
        return new TextImpl(ScreenTexts.CANCEL.copy());
    }

    @Override
    public IIdentifier newIdentifier(String namespace, String id) {
        return new IdentifierImpl(new net.minecraft.util.Identifier(namespace, id));
    }
}
