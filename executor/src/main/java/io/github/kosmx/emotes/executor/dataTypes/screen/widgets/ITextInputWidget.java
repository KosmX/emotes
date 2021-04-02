package io.github.kosmx.emotes.executor.dataTypes.screen.widgets;

import java.util.function.Consumer;

public interface ITextInputWidget<MATRIX, T extends ITextInputWidget> extends IWidget<T> {
    void setInputListener(Consumer<String> onTextChange);

    void render(MATRIX matrices, int mouseX, int mouseY, float delta);
}
