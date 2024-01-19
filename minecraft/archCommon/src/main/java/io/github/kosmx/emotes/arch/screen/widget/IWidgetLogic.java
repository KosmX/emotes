package io.github.kosmx.emotes.arch.screen.widget;

import net.minecraft.client.gui.components.events.GuiEventListener;

public interface IWidgetLogic {
    default boolean emotes_mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean emotes_mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    GuiEventListener get();
}
