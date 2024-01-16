package io.github.kosmx.emotes.arch.gui.screen;

import io.github.kosmx.emotes.arch.executor.types.TextImpl;
import io.github.kosmx.emotes.inline.dataTypes.Text;
import io.github.kosmx.emotes.inline.dataTypes.screen.widgets.IButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class IButtonImpl extends Button implements IButton<IButtonImpl> {
    public IButtonImpl(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public IButtonImpl(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
    }

    @Override
    public void setMessage(Text text) {
        this.setMessage(((TextImpl)text).get());
    }

    @Override
    public void setActive(boolean b) {
        this.active = b;
    }

    @Override
    public boolean getActive() {
        return this.active;
    }

    @Override
    public IButtonImpl get() {
        return this; //Get is for getting the Widget object
    }
}
