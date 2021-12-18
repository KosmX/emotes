package io.github.kosmx.emotes.arch.gui.screen;

import io.github.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfirmScreenImpl extends ConfirmScreen implements IConfirmScreen<Screen> {
    public ConfirmScreenImpl(BooleanConsumer callback, Component title, Component message) {
        super(callback, title, message);
    }

    public ConfirmScreenImpl(BooleanConsumer callback, Component title, Component message, Component text, Component text2) {
        super(callback, title, message, text, text2);
    }

    @Override
    public void setTimeout(int timeout) {
        this.setDelay(timeout);
    }

    @Override
    public Screen getScreen() {
        return this;
    }
}
