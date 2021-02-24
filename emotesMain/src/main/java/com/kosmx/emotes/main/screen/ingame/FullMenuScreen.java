package com.kosmx.emotes.main.screen.ingame;

import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.main.screen.EmoteMenu;
import com.kosmx.emotes.main.screen.IScreenLogic;
import com.kosmx.emotes.main.screen.widget.AbstractEmoteListWidget;

import java.util.List;

/**
 * Stuff to override/implement
 * init
 * isPauseScreen
 * render
 * @param <MATRIX> MatrixStack
 */
public abstract class FullMenuScreen<MATRIX> implements IScreenLogic<MATRIX> {

    private ITextInputWidget<MATRIX, ITextInputWidget> searchBox;
    private EmoteList emoteList;

    abstract public EmoteMenu<MATRIX> newEmoteMenu();

    @Override
    public void initScreen(){
        int x = (int) Math.min(this.getWidth() * 0.8, this.getHeight() - 60);
        this.searchBox = newTextInputWidget((this.getWidth() - x) / 2, 12, x, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.search"));
        this.searchBox.setInputListener((string)->this.emoteList.filter(string::toLowerCase));
        this.emoteList = newEmoteList(x, getHeight(), getWidth());
        this.emoteList.setLeftPos((this.getWidth() - x) / 2);
        emoteList.setEmotes(EmoteHolder.list);
        addToChildren(searchBox);
        addToChildren(emoteList);
        this.setInitialFocus(this.searchBox);
        addToButtons(newButton(this.getWidth() - 120, this.getHeight() - 30, 96, 20, EmoteInstance.instance.getDefaults().defaultTextCancel(), (button->openScreen(null))));
        addToButtons(newButton(this.getWidth() - 120, this.getHeight() - 60, 96, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.config"), (button->openScreen(newEmoteMenu()))));
        addButtonsToChildren();
    }

    protected abstract EmoteList newEmoteList(int boxSize, int height, int width);

    @Override
    public boolean isThisPauseScreen(){
        return false;
    }


    @Override
    public void renderScreen(MATRIX matrices, int mouseX, int mouseY, float delta){
        this.renderBackgroundTexture(0);
        this.emoteList.render(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
    }

    private abstract class EmoteList extends AbstractEmoteListWidget<EmoteList.EmoteEntry, MATRIX> {

        /*
        public EmoteList(int boxSize, int height, int width){
            super(boxSize, height, (height - boxSize) / 2 + 10, width > (width + boxSize)/2 + 120 ? (height + boxSize) / 2 + 10 : height - 80,36);
        }
         */

        @Override
        public void setEmotes(List<EmoteHolder> list){
            for(EmoteHolder emote : list){
                this.emotes.add(new EmoteEntry(emote));
            }
            filter(()->"");
        }

        private class EmoteEntry extends AbstractEmoteListWidget<EmoteEntry, MATRIX>.AbstractEmoteEntry {

            public EmoteEntry(EmoteHolder emote){
                super(emote);
            }

            @Override
            protected void onPressed(){
                if(EmoteInstance.instance.getClientMethods().isAbstractClientEntity(EmoteInstance.instance.getClientMethods().getMainPlayer())){
                    this.emote.playEmote(EmoteInstance.instance.getClientMethods().getMainPlayer());
                    openScreen(null);
                }
            }
        }
    }
}
