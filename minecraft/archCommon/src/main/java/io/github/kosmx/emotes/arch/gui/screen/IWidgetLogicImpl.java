package io.github.kosmx.emotes.arch.gui.screen;

import io.github.kosmx.emotes.arch.screen.widget.IWidgetLogic;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;


public interface IWidgetLogicImpl extends IWidgetLogic, GuiEventListener, NarratableEntry {
    @Override
    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.emotes_mouseClicked(mouseX, mouseY, button);
    }

    @Override
    default boolean mouseScrolled(double d, double e, double f) {
        return this.emotes_mouseScrolled(d, e, f);
    }

    @Override
    default NarrationPriority narrationPriority(){
        return NarrationPriority.NONE; //TODO narration
    }

    @Override
    default void updateNarration(NarrationElementOutput narrationElementOutput){
        //TODO this too
    }
}
