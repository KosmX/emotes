package io.github.kosmx.emotes.arch.screen;

import io.github.kosmx.emotes.arch.gui.screen.IButtonImpl;
import io.github.kosmx.emotes.arch.gui.screen.TextInputImpl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

/**
 * For custom screens
 */
public interface IScreenSlave {

    void openThisScreen();

    int getWidth();
    int getHeight();

    void setInitialFocus(GuiEventListener searchBox);

    void setFocused(GuiEventListener focused);

    void addToChildren(GuiEventListener widget);
    void addToButtons(IButtonImpl button);
    void openParent();
    void addButtonsToChildren();
    void emotesRenderBackgroundTexture(GuiGraphics poseStack);
    void renderBackground(GuiGraphics matrices);
    void openScreen(@Nullable IScreenSlave screen);

    Screen getScreen();
}
