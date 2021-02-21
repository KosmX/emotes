package com.kosmx.emotes.main.screen.widget;

import com.kosmx.emotes.common.tools.MathHelper;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.dataTypes.other.TextFormatting;
import com.kosmx.emotes.main.EmoteHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Methods to override in the class:
 * int getRowWidth()
 * int getScrollbarPosition()
 * boolean isFocused()
 *
 * @param <E>
 */
public abstract class AbstractEmoteListWidget<E extends AbstractEmoteListWidget.AbstractEmoteEntry, MATRIX> extends AbstractWidgetLogic<MATRIX> {

    protected List<E> emotes = new ArrayList<>();

    abstract int getWidth();


    public int getRowWidth(){
        return this.getWidth() - 5;
    }

    public abstract void setEmotes(List<EmoteHolder> list);

    public void filter(Supplier<String> string){
        this.clearEntries();
        for(E emote : this.emotes){
            if(emote.emote.name.toString().toLowerCase().contains(string.get()) || emote.emote.description.toString().toLowerCase().contains(string.get()) || emote.emote.author.toString().toLowerCase().equals(string.get())){
                this.addEntry(emote);
            }
        }
    }

    abstract protected void clearEntries();
    abstract protected void addEntry(E entry);

    protected int getScrollbarPositionX(){
        return this.getRight() - 6;
    }
    protected abstract int getRight();


    /**
     * stuff to implement + override
     * void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)
     * boolean mouseClicked(double x, double y, int button)
     */
    public abstract class AbstractEmoteEntry {
        public final EmoteHolder emote;

        public AbstractEmoteEntry(EmoteHolder emote){
            this.emote = emote;
        }


        public void render(MATRIX matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta){

            if(hovered){
                renderSystemBlendColor(1, 1, 1, 1);
                drawableHelperFill(matrices, x - 1, y - 1, x + entryWidth - 9, y + entryHeight + 1, MathHelper.colorHelper(66, 66, 66, 128));
            }
            textDrawWithShadow(matrices, this.emote.name, x + 38, y + 1, 16777215);
            textDrawWithShadow(matrices, this.emote.description, x + 38, y + 12, 8421504);
            if(! this.emote.author.getString().equals(""))
                textDrawWithShadow(matrices, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.emote.author").formatted(TextFormatting.GOLD).append(this.emote.author), x + 38, y + 23, 8421504);
            if(this.emote.getIconIdentifier() != null){
                renderSystemBlendColor(1, 1, 1, 1); //color4f => blendColor
                renderBindTexture(this.emote.getIconIdentifier());
                renderEnableBend();
                drawableDrawTexture(matrices, x, y, 32, 32, 0, 0, 256, 256, 256, 256);
                renderDisableBend();
            }
        }


        public boolean mouseClicked(double mouseX, double mouseY, int button){
            if(button == 0){
                this.onPressed();
                return true;
            }else{
                return false;
            }
        }

        protected abstract void onPressed();
    }
}
