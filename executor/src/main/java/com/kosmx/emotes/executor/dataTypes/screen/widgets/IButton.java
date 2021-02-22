package com.kosmx.emotes.executor.dataTypes.screen.widgets;

import com.kosmx.emotes.executor.dataTypes.Text;

public interface IButton extends IWidget {
    void setMessage(Text text);

    void setActive(boolean b);

    boolean getActive();
}
