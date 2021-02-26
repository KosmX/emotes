package com.kosmx.emotes.main.screen;

import com.kosmx.emotes.executor.dataTypes.Text;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import com.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IWidget;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IScreenLogicHelper<MATRIX> extends IRenderHelper<MATRIX> {

    IButton newButton(int x, int y, int width, int heitht, Text msg, Consumer<IButton> pressAction);

    ITextInputWidget newTextInputWidget(int x, int y, int width, int height, Text title);

    IConfirmScreen createConfigScreen(Consumer<Boolean> consumer, Text title, Text text);

}
