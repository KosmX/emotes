package io.github.kosmx.emotes.arch.screen;

import io.github.kosmx.emotes.inline.dataTypes.screen.widgets.IButton;
import io.github.kosmx.emotes.inline.dataTypes.screen.widgets.IWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

/**
 * For custom screens
 */
public interface IScreenSlave {

    void openThisScreen();

    int getWidth();
    int getHeight();

    void setInitialFocus(IWidget searchBox);

    void setFocused(IWidget focused);

    void addToChildren(IWidget widget);
    void addToButtons(IButton button);
    void openParent();
    void addButtonsToChildren();
    void emotesRenderBackgroundTexture(GuiGraphics poseStack);
    void renderBackground(GuiGraphics matrices);
    void openScreen(@Nullable IScreenSlave screen);

    Screen getScreen();
}
