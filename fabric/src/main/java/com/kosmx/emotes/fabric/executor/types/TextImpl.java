package com.kosmx.emotes.fabric.executor.types;

import com.google.gson.JsonElement;
import com.kosmx.emotes.executor.dataTypes.other.TextFormatting;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public class TextImpl implements com.kosmx.emotes.executor.dataTypes.Text {

    final MutableText MCText;

    public TextImpl(MutableText mcText) {
        MCText = mcText;
    }

    @Override
    public String getString() {
        return this.MCText.getString();
    }

    @Override
    public JsonElement toJsonTree() {
        return net.minecraft.text.Text.Serializer.toJsonTree(this.MCText);
    }

    @Override
    public com.kosmx.emotes.executor.dataTypes.Text formatted(TextFormatting form) {
        return new TextImpl(this.MCText.formatted(iFormatToFormat(form)));
    }

    @Override
    public com.kosmx.emotes.executor.dataTypes.Text append(com.kosmx.emotes.executor.dataTypes.Text text) {
        return new TextImpl(this.MCText.append(((TextImpl) text).MCText));
    }

    protected Formatting iFormatToFormat(TextFormatting textFormatting){
        return Formatting.byCode(textFormatting.getCode());
    }

    public MutableText get(){
        return this.MCText;
    }
}
