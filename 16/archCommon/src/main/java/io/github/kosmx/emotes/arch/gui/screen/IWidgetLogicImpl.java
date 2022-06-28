package io.github.kosmx.emotes.arch.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kosmx.emotes.main.screen.widget.IWidgetLogic;
import net.minecraft.client.gui.components.events.GuiEventListener;


public interface IWidgetLogicImpl extends IWidgetLogic<PoseStack, GuiEventListener>, GuiEventListener {
    @Override
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.emotes_mouseClicked(mouseX, mouseY, button);
    }

    @Override
    default boolean mouseScrolled(double d, double e, double f) {
        return this.emotes_mouseScrolled(d, e, f);
    }
}
