package com.kosmx.emotes.fabric.gui.screen;

import com.kosmx.emotes.main.screen.widget.IWidgetLogic;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;


public interface IWidgetLogicImpl extends IWidgetLogic<MatrixStack, Element>, Element {
    @Override
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.emotes_mouseClicked(mouseX, mouseY, button);
    }
}
