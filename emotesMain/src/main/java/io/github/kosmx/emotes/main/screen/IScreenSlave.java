package io.github.kosmx.emotes.main.screen;

import io.github.kosmx.emotes.executor.dataTypes.screen.IScreen;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.IWidget;

import javax.annotation.Nullable;

/**
 * For custom screens
 */
public interface IScreenSlave<MATRIX, SCREEN> extends IScreen<SCREEN> {

    void openThisScreen();

    int getWidth();
    int getHeight();

    void setInitialFocus(IWidget searchBox);

    void setFocused(IWidget focused);

    void addToChildren(IWidget widget);
    void addToButtons(IButton button);
    void openParent();
    void addButtonsToChildren();
    void emotesRenderBackgroundTexture(int i);
    void renderBackground(MATRIX matrices);
    void openScreen(@Nullable IScreen<SCREEN> screen);
}
