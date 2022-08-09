package io.github.kosmx.emotes.main.screen.widget;

import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.InputKey;
import io.github.kosmx.emotes.executor.dataTypes.other.EmotesTextFormatting;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.screen.IRenderHelper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * That should be a helper stuff for an EntryWidget list...
 */
public interface IEmoteListWidgetHelper<MATRIX, WIDGET> extends IWidgetLogic<MATRIX, WIDGET> {
    void filter(Supplier<String> supplier);
    void emotesSetLeftPos(int p);
    IEmoteEntry getSelectedEntry();
    void renderThis(MATRIX matrices, int mouseX, int mouseY, float tickDelta);
    void setEmotes(Iterable<EmoteHolder> emoteHolders, boolean showInvalid);

    default Iterable<EmoteHolder> getEmptyEmotes(){
        Collection<EmoteHolder> empties = new LinkedList<>();
        for(Pair<UUID, InputKey> pair : ((ClientConfig)EmoteInstance.config).emoteKeyMap){
            if(!EmoteHolder.list.containsKey(pair.getLeft())){
                empties.add(new EmoteHolder.Empty(pair.getLeft()));
            }
        }
        return empties;
    }

    interface IEmoteEntry<MATRIX> extends IRenderHelper<MATRIX> {
        EmoteHolder getEmote();
        default void renderThis(MATRIX matrices, int index, int y, int x, int entryWidth, int entryHeitht, int mouseX, int mouseY, boolean hovered, float tickDelta){
            if(hovered){
                renderSystemBlendColor(1, 1, 1, 1);
                drawableHelperFill(matrices, x - 1, y - 1, x + entryWidth - 9, y + entryHeitht + 1, MathHelper.colorHelper(66, 66, 66, 128));
            }
            textDrawWithShadow(matrices, this.getEmote().name, x + 38, y + 1, 16777215);
            textDrawWithShadow(matrices, this.getEmote().description, x + 38, y + 12, 8421504);
            if(! this.getEmote().author.getString().equals(""))
                textDrawWithShadow(matrices, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.emote.author").formatted(EmotesTextFormatting.GOLD).append(this.getEmote().author), x + 38, y + 23, 8421504);
            if(this.getEmote().getIconIdentifier() != null){
                renderSystemBlendColor(1, 1, 1, 1); //color4f => blendColor
                renderBindTexture(this.getEmote().getIconIdentifier());
                renderEnableBend();
                drawableDrawTexture(matrices, x, y, 32, 32, 0, 0, 256, 256, 256, 256);
                renderDisableBend();
            }
        }
    }
}
