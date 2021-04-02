package io.github.kosmx.emotes.fabric.executor.types;

import com.google.gson.JsonElement;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.executor.dataTypes.other.TextFormatting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

public class TextImpl implements Text {

    final MutableComponent MCText;

    public TextImpl(MutableComponent mcText) {
        MCText = mcText;
    }

    @Override
    public String getString() {
        return this.MCText.getString();
    }

    @Override
    public JsonElement toJsonTree() {
        return net.minecraft.network.chat.Component.Serializer.toJsonTree(this.MCText);
    }

    @Override
    public Text formatted(TextFormatting form) {
        return new TextImpl(this.MCText.withStyle(iFormatToFormat(form)));
    }

    @Override
    public Text append(Text text) {
        return new TextImpl(this.MCText.append(((TextImpl) text).MCText));
    }

    protected ChatFormatting iFormatToFormat(TextFormatting textFormatting){
        return ChatFormatting.getByCode(textFormatting.getCode());
    }

    public MutableComponent get(){
        return this.MCText;
    }
}
