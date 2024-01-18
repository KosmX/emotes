package io.github.kosmx.emotes.inline.dataTypes.screen.widgets;

import net.minecraft.network.chat.Component;

public interface IButton extends IWidget {
    void setMessage(Component text);

    void setActive(boolean b);

    boolean getActive();
}
