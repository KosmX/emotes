package io.github.kosmx.emotes.main.screen.ingame;

import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.screen.IScreen;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.main.screen.IScreenSlave;
import io.github.kosmx.emotes.main.screen.widget.IEmoteListWidgetHelper;

/**
 * Stuff to override/implement
 * init
 * isPauseScreen
 * render
 * @param <MATRIX> MatrixStack
 */
@SuppressWarnings("unchecked")
public abstract class FullMenuScreenHelper<MATRIX, SCREEN, WIDGET> extends AbstractScreenLogic<MATRIX, SCREEN> {

    private ITextInputWidget<MATRIX, ITextInputWidget> searchBox;
    private IEmoteListWidgetHelper<MATRIX, WIDGET> emoteList;

    protected FullMenuScreenHelper(IScreenSlave screen) {
        super(screen);
    }

    abstract public IScreen<SCREEN> newEmoteMenu();

    @Override
    public void emotes_initScreen(){
        int x = (int) Math.min(screen.getWidth() * 0.8, screen.getHeight() - 60);
        this.searchBox = newTextInputWidget((screen.getWidth() - x) / 2, 12, x, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.search"));
        this.searchBox.setInputListener((string)->emoteList.filter(string::toLowerCase));
        this.emoteList = newEmoteList(x, screen.getHeight(), screen.getWidth());
        this.emoteList.emotesSetLeftPos((screen.getWidth() - x) / 2);
        emoteList.setEmotes(EmoteHolder.list, false);
        screen.addToChildren(searchBox);
        screen.addToChildren(emoteList);
        screen.setInitialFocus(this.searchBox);
        screen.addToButtons(newButton(screen.getWidth() - 120, screen.getHeight() - 30, 96, 20, EmoteInstance.instance.getDefaults().defaultTextCancel(), (button->screen.openScreen(null))));
        screen.addToButtons(newButton(screen.getWidth() - 120, screen.getHeight() - 60, 96, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.config"), (button->screen.openScreen(newEmoteMenu()))));
        screen.addButtonsToChildren();
    }

    protected IEmoteListWidgetHelper<MATRIX, WIDGET> newEmoteList(int boxSize, int height, int width){
        return newEmoteList(boxSize, height, (height-boxSize)/2+10, width > (width + boxSize)/2 + 120 ? (height + boxSize)/2 + 10 : height - 80, 36);
    }

    protected abstract IEmoteListWidgetHelper<MATRIX, WIDGET> newEmoteList(int boxSize, int height, int k, int l, int m);

    @Override
    public boolean emotes_isThisPauseScreen(){
        return false;
    }


    @Override
    public void emotes_renderScreen(MATRIX matrices, int mouseX, int mouseY, float delta){
        screen.emotesRenderBackgroundTexture(0);
        this.emoteList.renderThis(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
    }
}
