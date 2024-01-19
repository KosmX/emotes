package io.github.kosmx.emotes.arch.gui.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfirmScreenImpl extends ConfirmScreen {
    public ConfirmScreenImpl(BooleanConsumer callback, Component title, Component message) {
        super(callback, title, message);
    }

    public ConfirmScreenImpl(BooleanConsumer callback, Component title, Component message, Component text, Component text2) {
        super(callback, title, message, text, text2);
    }

    public void setDelay(int timeout) {
        this.setDelay(timeout);
    }

    public Screen getScreen() {
        return this;
    }
}
