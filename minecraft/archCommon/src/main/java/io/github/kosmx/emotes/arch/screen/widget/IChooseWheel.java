package io.github.kosmx.emotes.arch.screen.widget;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

public interface IChooseWheel extends Renderable, GuiEventListener {
    @Override
    default void setFocused(boolean bl) {
    }

    @Override
    default boolean isFocused() {
        return false;
    }

    interface IChooseElement {
        boolean hasEmote();
        EmoteHolder getEmote();
        void clearEmote();
        void setEmote(EmoteHolder emote);
    }

    static IChooseWheel getWheel(AbstractFastChooseWidget widget) {
        if (PlatformTools.getConfig().oldChooseWheel.get()) {
            return new LegacyChooseWidget(widget);
        } else {
            return new ModernChooseWheel(widget);
        }
    }
}
