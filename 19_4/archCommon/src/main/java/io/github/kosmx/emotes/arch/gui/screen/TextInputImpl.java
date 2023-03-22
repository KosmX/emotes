package io.github.kosmx.emotes.arch.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kosmx.emotes.arch.executor.types.TextImpl;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TextInputImpl extends EditBox implements ITextInputWidget<PoseStack, TextInputImpl> {
    public TextInputImpl(Font textRenderer, int x, int y, int width, int height, Component text) {
        super(textRenderer, x, y, width, height, text);
    }

    public TextInputImpl(int x, int y, int width, int height, TextImpl text){
        this(Minecraft.getInstance().font, x, y, width, height, text.get());
    }

    public TextInputImpl(Font textRenderer, int x, int y, int width, int height, @Nullable EditBox copyFrom, Component text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
    }

    @Override
    public void setInputListener(Consumer<String> onTextChange) {
        this.setResponder(onTextChange);
    }

    @Override
    public TextInputImpl get() {
        return this;
    }
}
