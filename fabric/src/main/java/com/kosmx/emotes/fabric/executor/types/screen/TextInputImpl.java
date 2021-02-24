package com.kosmx.emotes.fabric.executor.types.screen;

import com.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import com.kosmx.emotes.fabric.executor.types.TextImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TextInputImpl extends TextFieldWidget implements ITextInputWidget<MatrixStack, TextInputImpl> {
    public TextInputImpl(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    public TextInputImpl(int x, int y, int width, int height, TextImpl text){
        this(MinecraftClient.getInstance().textRenderer, x, y, width, height, text.get());
    }

    public TextInputImpl(TextRenderer textRenderer, int x, int y, int width, int height, @Nullable TextFieldWidget copyFrom, Text text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
    }

    @Override
    public void setInputListener(Consumer<String> onTextChange) {
        this.setChangedListener(onTextChange);
    }

    @Override
    public TextInputImpl get() {
        return this;
    }
}
