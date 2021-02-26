package com.kosmx.emotes.main.screen.ingame;

import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.dataTypes.screen.IScreen;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.main.screen.AbstractScreenLogic;
import com.kosmx.emotes.main.screen.EmoteMenu;
import com.kosmx.emotes.main.screen.IScreenLogicHelper;
import com.kosmx.emotes.main.screen.IScreenSlave;
import com.kosmx.emotes.main.screen.widget.AbstractEmoteListWidget;

import java.util.List;

/**
 * Stuff to override/implement
 * init
 * isPauseScreen
 * render
 * @param <MATRIX> MatrixStack
 */
public abstract class FullMenuScreenHelper<MATRIX, SCREEN> extends AbstractScreenLogic<MATRIX, SCREEN> {

    private ITextInputWidget<MATRIX, ITextInputWidget> searchBox;
    private EmoteList emoteList;

    protected FullMenuScreenHelper(IScreenSlave screen) {
        super(screen);
    }

    abstract public IScreen<SCREEN> newEmoteMenu();

    @Override
    public void initScreen(){
        int x = (int) Math.min(screen.getWidth() * 0.8, screen.getHeight() - 60);
        this.searchBox = newTextInputWidget((screen.getWidth() - x) / 2, 12, x, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.search"));
        this.searchBox.setInputListener((string)->emoteList.filter(string::toLowerCase));
        this.emoteList = newEmoteList(x, screen.getHeight(), screen.getWidth());
        this.emoteList.setLeftPos((screen.getWidth() - x) / 2);
        emoteList.setEmotes(EmoteHolder.list);
        screen.addToChildren(searchBox);
        screen.addToChildren(emoteList);
        screen.setInitialFocus(this.searchBox);
        screen.addToButtons(newButton(screen.getWidth() - 120, screen.getHeight() - 30, 96, 20, EmoteInstance.instance.getDefaults().defaultTextCancel(), (button->screen.openScreen(null))));
        screen.addToButtons(newButton(screen.getWidth() - 120, screen.getHeight() - 60, 96, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.config"), (button->screen.openScreen(newEmoteMenu()))));
        screen.addButtonsToChildren();
    }

    protected abstract EmoteList newEmoteList(int boxSize, int height, int width);

    @Override
    public boolean isThisPauseScreen(){
        return false;
    }


    @Override
    public void renderScreen(MATRIX matrices, int mouseX, int mouseY, float delta){
        screen.renderBackgroundTexture(0);
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
                    screen.openScreen(null);
                }
            }
        }
    }
}
