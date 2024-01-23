package io.github.kosmx.emotes.arch.screen.widget;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

public interface IWidgetLogic extends GuiEventListener, NarratableEntry {
    @Override
    default NarratableEntry.NarrationPriority narrationPriority(){
        return NarratableEntry.NarrationPriority.NONE; //TODO narration
    }

    @Override
    default void updateNarration(NarrationElementOutput narrationElementOutput){
        //TODO this too
    }
}
