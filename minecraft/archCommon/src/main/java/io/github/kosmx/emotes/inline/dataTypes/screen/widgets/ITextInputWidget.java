package io.github.kosmx.emotes.inline.dataTypes.screen.widgets;

import net.minecraft.client.gui.GuiGraphics;

import java.util.function.Consumer;

public interface ITextInputWidget<T extends ITextInputWidget> extends IWidget {
    void setInputListener(Consumer<String> onTextChange);

    void render(GuiGraphics matrices, int mouseX, int mouseY, float delta);
}
