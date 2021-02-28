package com.kosmx.emotes.fabric.gui.screen;

import com.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfirmScreenImpl extends ConfirmScreen implements IConfirmScreen<Screen> {
    public ConfirmScreenImpl(BooleanConsumer callback, Text title, Text message) {
        super(callback, title, message);
    }

    public ConfirmScreenImpl(BooleanConsumer callback, Text title, Text message, Text text, Text text2) {
        super(callback, title, message, text, text2);
    }

    @Override
    public void setTimeout(int timeout) {
        this.setTimeout(timeout);
    }

    @Override
    public Screen getScreen() {
        return this;
    }
}
