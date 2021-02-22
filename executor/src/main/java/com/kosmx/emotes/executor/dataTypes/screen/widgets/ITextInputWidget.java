package com.kosmx.emotes.executor.dataTypes.screen.widgets;

import java.util.function.Consumer;

public interface ITextInputWidget extends IWidget {
    void setInputListener(Consumer<String> onTextChange);
}
