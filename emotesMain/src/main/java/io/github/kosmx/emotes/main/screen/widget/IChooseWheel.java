package io.github.kosmx.emotes.main.screen.widget;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;

public interface IChooseWheel<MATRIX> {


    void render(MATRIX matrices, int mouseX, int mouseY, float delta);

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean mouseScrolled(double mouseX, double mouseY, double amount);

    boolean isMouseOver(double mouseX, double mouseY);

    interface IChooseElement {

        boolean hasEmote();

        EmoteHolder getEmote();

        void clearEmote();

        void setEmote(EmoteHolder emote);
    }

    static <MATRIX, WIDGET> IChooseWheel<MATRIX> getWheel(AbstractFastChooseWidget<MATRIX, WIDGET> widget) {
        if (((ClientConfig) EmoteInstance.config).oldChooseWheel.get()) {
            return new LegacyChooseWidget<>(widget);
        } else {
            return new ModernChooseWheel<>(widget);
        }
    }
}
