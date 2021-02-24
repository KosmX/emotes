package com.kosmx.emotes.fabric.executor.types.screen;

import com.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import com.kosmx.emotes.fabric.executor.types.TextImpl;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class IButtonImpl extends ButtonWidget implements IButton<IButtonImpl> {
    public IButtonImpl(int x, int y, int width, int height, Text message, PressAction onPress) {
        super(x, y, width, height, message, onPress);
    }

    public IButtonImpl(int x, int y, int width, int height, Text message, PressAction onPress, TooltipSupplier tooltipSupplier) {
        super(x, y, width, height, message, onPress, tooltipSupplier);
    }

    @Override
    public void setMessage(com.kosmx.emotes.executor.dataTypes.Text text) {
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
        return this;
    }
}
