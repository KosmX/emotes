package com.kosmx.emotes.main.screen;

import com.kosmx.emotes.executor.dataTypes.Text;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import com.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.ITextWidget;

import java.util.function.Consumer;

public abstract class AbstractScreenLogic {
    protected abstract void init();

    abstract protected IButton newButton();
    abstract protected ITextWidget newTextWidget();
    abstract protected ITextInputWidget newTextInputWidget(int x, int y, int width, int height, Text title);

    abstract protected IConfirmScreen createConfigScreen(Consumer<Boolean> consumer, Text title, Text text);

    abstract protected void openThisScreen();

    abstract int getWidth();
    abstract int getHeight();
}
