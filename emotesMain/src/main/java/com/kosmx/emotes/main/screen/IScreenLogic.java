package com.kosmx.emotes.main.screen;

import com.kosmx.emotes.executor.dataTypes.Text;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import com.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IWidget;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public interface IScreenLogic<MATRIX> extends IRenderHelper<MATRIX> {

    IButton newButton(int x, int y, int width, int heitht, Text msg, Consumer<IButton> pressAction);

    ITextInputWidget newTextInputWidget(int x, int y, int width, int height, Text title);

    IConfirmScreen createConfigScreen(Consumer<Boolean> consumer, Text title, Text text);

    void openThisScreen();

    int getWidth();
    int getHeight();

    void setInitialFocus(IWidget searchBox);

    void setFocused(IWidget focused);

    void addToChildren(IWidget widget);
    void addToButtons(IButton button);
    void openParent();
    void addButtonsToChildren();
    void renderBackgroundTexture(int i);
    void renderBackground(MATRIX matrices);
    void openScreen(@Nullable IScreenLogic<MATRIX> screen);
}
