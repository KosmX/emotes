package io.github.kosmx.emotes.arch.screen.widget;

import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.jetbrains.annotations.Nullable;

public interface IChooseElement extends GuiEventListener, Renderable {
    boolean hasEmote();
    @Nullable
    EmoteHolder getEmote();
    void clearEmote();
    void setEmote(EmoteHolder emote);

    void removed();
}
