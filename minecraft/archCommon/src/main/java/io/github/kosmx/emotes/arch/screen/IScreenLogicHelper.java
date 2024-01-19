package io.github.kosmx.emotes.arch.screen;

import io.github.kosmx.emotes.arch.gui.screen.ConfirmScreenImpl;
import io.github.kosmx.emotes.inline.dataTypes.screen.widgets.IButton;
import io.github.kosmx.emotes.inline.dataTypes.screen.widgets.ITextInputWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public interface IScreenLogicHelper extends IRenderHelper {

    IButton newButton(int x, int y, int width, int heitht, Component msg, Consumer<IButton> pressAction);

    ITextInputWidget newTextInputWidget(int x, int y, int width, int height, Component title);

    ConfirmScreen createConfigScreen(Consumer<Boolean> consumer, Component title, Component text);

    void openExternalEmotesDir();
}
