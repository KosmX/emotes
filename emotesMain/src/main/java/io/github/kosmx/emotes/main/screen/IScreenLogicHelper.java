package io.github.kosmx.emotes.main.screen;

import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;

import java.util.function.Consumer;

public interface IScreenLogicHelper<MATRIX> extends IRenderHelper<MATRIX> {

    IButton newButton(int x, int y, int width, int heitht, Text msg, Consumer<IButton> pressAction);

    ITextInputWidget newTextInputWidget(int x, int y, int width, int height, Text title);

    IConfirmScreen createConfigScreen(Consumer<Boolean> consumer, Text title, Text text);

    void openExternalEmotesDir();
}
