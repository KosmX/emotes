package io.github.kosmx.emotes.inline.dataTypes.screen.widgets;

import io.github.kosmx.emotes.inline.dataTypes.Text;

public interface IButton<T> extends IWidget<T> {
    void setMessage(Text text);

    void setActive(boolean b);

    boolean getActive();
}
