package io.github.kosmx.emotes.arch.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class TextInputImpl extends EditBox {
    public TextInputImpl(Font textRenderer, int x, int y, int width, int height, Component text) {
        super(textRenderer, x, y, width, height, text);
    }

    public TextInputImpl(int x, int y, int width, int height, Component text){
        this(Minecraft.getInstance().font, x, y, width, height, text);
    }

    public TextInputImpl(Font textRenderer, int x, int y, int width, int height, @Nullable EditBox copyFrom, Component text) {
        super(textRenderer, x, y, width, height, copyFrom, text);
    }

    public void setInputListener(Consumer<String> onTextChange) {
        this.setResponder(onTextChange);
    }

    public TextInputImpl get() {
        return this;
    }
}
