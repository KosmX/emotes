package io.github.kosmx.emotes.arch.screen.widget;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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

    interface IEmoteEntry {
        EmoteHolder getEmote();
        default void renderThis(GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeitht, int mouseX, int mouseY, boolean hovered, float tickDelta){
            if(hovered){
                RenderSystem.setShaderColor((float) 1, (float) 1, (float) 1, (float) 1);
                matrices.fill(x - 1, y - 1, x + entryWidth - 9, y + entryHeitht + 1, MathHelper.colorHelper(66, 66, 66, 128));
            }
            matrices.drawString(Minecraft.getInstance().font, this.getEmote().name, (int) ((float) (x + 38)), (int) ((float) (y + 1)), 16777215);
            matrices.drawString(Minecraft.getInstance().font, this.getEmote().description, (int) ((float) (x + 38)), (int) ((float) (y + 12)), 8421504);
            if(! this.getEmote().author.getString().equals("")) {
                Component text = Component.translatable("emotecraft.emote.author").withStyle(ChatFormatting.GOLD).append(this.getEmote().author);
                matrices.drawString(Minecraft.getInstance().font, text, (int) ((float) (x + 38)), (int) ((float) (y + 23)), 8421504);
            }
            if(this.getEmote().getIconIdentifier() != null){
                //color4f => blendColor
                RenderSystem.setShaderColor((float) 1, (float) 1, (float) 1, (float) 1);
                RenderSystem.enableBlend();
                ResourceLocation texture = this.getEmote().getIconIdentifier();
                matrices.blit(texture, x, y, 32, 32, (float) 0, (float) 0, 256, 256, 256, 256);
                RenderSystem.disableBlend();
            }
        }
    }
}
