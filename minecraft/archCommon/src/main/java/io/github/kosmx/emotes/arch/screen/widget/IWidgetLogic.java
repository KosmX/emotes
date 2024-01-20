package io.github.kosmx.emotes.arch.screen.widget;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public interface IWidgetLogic extends GuiEventListener, NarratableEntry {
    default boolean emotes_mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean emotes_mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    GuiEventListener get();


    @Override
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.emotes_mouseClicked(mouseX, mouseY, button);
    }

    @Override
    default boolean mouseScrolled(double d, double e, double f) {
        return this.emotes_mouseScrolled(d, e, f);
    }

    @Override
    default NarratableEntry.NarrationPriority narrationPriority(){
        return NarratableEntry.NarrationPriority.NONE; //TODO narration
    }

    @Override
    default void updateNarration(NarrationElementOutput narrationElementOutput){
        //TODO this too
    }
}
