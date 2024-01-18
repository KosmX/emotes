package io.github.kosmx.emotes.arch.screen.widget;

import com.mojang.blaze3d.platform.InputConstants;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.arch.screen.IRenderHelper;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * That should be a helper stuff for an EntryWidget list...
 */
public interface IEmoteListWidgetHelper extends IWidgetLogic {
    void filter(Supplier<String> supplier);
    void emotesSetLeftPos(int p);
    IEmoteEntry getSelectedEntry();
    void renderThis(GuiGraphics matrices, int mouseX, int mouseY, float tickDelta);
    void setEmotes(Iterable<EmoteHolder> emoteHolders, boolean showInvalid);

    default Iterable<EmoteHolder> getEmptyEmotes(){
        Collection<EmoteHolder> empties = new LinkedList<>();
        for(Pair<UUID, InputConstants.Key> pair : ((ClientConfig)EmoteInstance.config).emoteKeyMap){
            if(!EmoteHolder.list.containsKey(pair.getLeft())){
                empties.add(new EmoteHolder.Empty(pair.getLeft()));
            }
        }
        return empties;
    }

    interface IEmoteEntry extends IRenderHelper {
        EmoteHolder getEmote();
        default void renderThis(GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeitht, int mouseX, int mouseY, boolean hovered, float tickDelta){
            if(hovered){
                renderSystemBlendColor(1, 1, 1, 1);
                drawableHelperFill(matrices, x - 1, y - 1, x + entryWidth - 9, y + entryHeitht + 1, MathHelper.colorHelper(66, 66, 66, 128));
            }
            textDrawWithShadow(matrices, this.getEmote().name, x + 38, y + 1, 16777215);
            textDrawWithShadow(matrices, this.getEmote().description, x + 38, y + 12, 8421504);
            if(! this.getEmote().author.getString().equals(""))
                textDrawWithShadow(matrices, Component.translatable("emotecraft.emote.author").withStyle(ChatFormatting.GOLD).append(this.getEmote().author), x + 38, y + 23, 8421504);
            if(this.getEmote().getIconIdentifier() != null){
                renderSystemBlendColor(1, 1, 1, 1); //color4f => blendColor
                renderEnableBend();
                drawableDrawTexture(matrices, this.getEmote().getIconIdentifier(), x, y, 32, 32, 0, 0, 256, 256, 256, 256);
                renderDisableBend();
            }
        }
    }
}
