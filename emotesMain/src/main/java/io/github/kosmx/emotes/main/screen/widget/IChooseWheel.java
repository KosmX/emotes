package io.github.kosmx.emotes.main.screen.widget;

import io.github.kosmx.emotes.main.EmoteHolder;

public interface IChooseWheel<MATRIX> {


    void render(MATRIX matrices, int mouseX, int mouseY, float delta);

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean isMouseOver(double mouseX, double mouseY);

    public interface IChooseElement {

        boolean hasEmote();

        EmoteHolder getEmote();

        void clearEmote();

        void setEmote(EmoteHolder emote);
    }
}
