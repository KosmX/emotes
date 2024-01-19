package io.github.kosmx.emotes.arch.gui.screen;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class IButtonImpl extends Button {
    public IButtonImpl(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public IButtonImpl(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
    }

    public void setMessage(Component text) {
        this.setMessage(text);
    }

    public void setActive(boolean b) {
        this.active = b;
    }

    public boolean getActive() {
        return this.active;
    }

    public IButtonImpl get() {
        return this; //Get is for getting the Widget object
    }
}
