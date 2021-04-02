package io.github.kosmx.emotes.executor.dataTypes.screen.widgets;

import io.github.kosmx.emotes.executor.dataTypes.Text;

public interface IButton<T> extends IWidget<T> {
    void setMessage(Text text);

    void setActive(boolean b);

    boolean getActive();
}
