package io.github.kosmx.emotes.arch.screen.ingame;

import io.github.kosmx.emotes.arch.gui.screen.TextInputImpl;
import io.github.kosmx.emotes.arch.screen.widget.IEmoteListWidgetHelper;
import io.github.kosmx.emotes.inline.TmpGetters;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.arch.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.arch.screen.IScreenSlave;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Stuff to override/implement
 * init
 * isPauseScreen
 * render
 */
@SuppressWarnings("unchecked")
public abstract class FullMenuScreenHelper extends AbstractScreenLogic {

    private TextInputImpl searchBox;
    private IEmoteListWidgetHelper emoteList;

    protected FullMenuScreenHelper(IScreenSlave screen) {
        super(screen);
    }

    abstract public IScreenSlave newEmoteMenu();

    @Override
    public void emotes_initScreen(){
        int x = (int) Math.min(screen.getWidth() * 0.8, screen.getHeight() - 60);
        this.searchBox = newTextInputWidget((screen.getWidth() - x) / 2, 12, x, 20, Component.translatable("emotecraft.search"));
        this.searchBox.setInputListener((string)->emoteList.filter(string::toLowerCase));
        this.emoteList = newEmoteList(x, screen.getHeight(), screen.getWidth());
        this.emoteList.emotesSetLeftPos((screen.getWidth() - x) / 2);
        emoteList.setEmotes(EmoteHolder.list, false);
        screen.addToChildren(searchBox);
        screen.addToChildren(emoteList);
        screen.setInitialFocus(this.searchBox);
        screen.addToButtons(newButton(screen.getWidth() - 120, screen.getHeight() - 30, 96, 20, TmpGetters.getDefaults().defaultTextCancel(), (button->screen.openScreen(null))));
        screen.addToButtons(newButton(screen.getWidth() - 120, screen.getHeight() - 60, 96, 20, TmpGetters.getDefaults().newTranslationText("emotecraft.config"), (button->screen.openScreen(newEmoteMenu()))));
        screen.addButtonsToChildren();
    }

    protected IEmoteListWidgetHelper newEmoteList(int boxSize, int height, int width){
        return newEmoteList(boxSize, height, (height-boxSize)/2+10, width > (width + boxSize)/2 + 120 ? (height + boxSize)/2 + 10 : height - 80, 36);
    }

    protected abstract IEmoteListWidgetHelper newEmoteList(int boxSize, int height, int k, int l, int m);

    @Override
    public boolean emotes_isThisPauseScreen(){
        return false;
    }


    @Override
    public void emotes_renderScreen(GuiGraphics matrices, int mouseX, int mouseY, float delta){
        screen.emotesRenderBackgroundTexture(matrices);
        this.emoteList.renderThis(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
    }
}
