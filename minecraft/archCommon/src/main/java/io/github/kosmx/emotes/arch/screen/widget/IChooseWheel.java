package io.github.kosmx.emotes.arch.screen.widget;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphics;

public interface IChooseWheel {


    void render(GuiGraphics matrices, int mouseX, int mouseY, float delta);

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean mouseScrolled(double mouseX, double mouseY, double amount);

    boolean isMouseOver(double mouseX, double mouseY);

    interface IChooseElement {

        boolean hasEmote();

        EmoteHolder getEmote();

        void clearEmote();

        void setEmote(EmoteHolder emote);
    }

    static IChooseWheel getWheel(AbstractFastChooseWidget widget) {
        if (((ClientConfig) EmoteInstance.config).oldChooseWheel.get()) {
            return new LegacyChooseWidget(widget);
        } else {
            return new ModernChooseWheel(widget);
        }
    }
}
