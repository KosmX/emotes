package com.kosmx.emotes.executor.dataTypes.screen.widgets;

import java.util.function.Consumer;

public interface ITextInputWidget<MATRIX> extends IWidget {
    void setInputListener(Consumer<String> onTextChange);

    void render(MATRIX matrices, int mouseX, int mouseY, float delta);
}
