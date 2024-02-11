package io.github.kosmx.emotes.arch.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.arch.screen.widget.IWidgetLogic;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractEmoteListWidget<E extends AbstractEmoteListWidget.AbstractEmoteEntry<E>> extends ObjectSelectionList<E> implements IWidgetLogic {

    public AbstractEmoteEntry<E> getSelectedEntry() {
        return this.getSelected();
    }

    protected List<E> emotes = new ArrayList<>();
    private final Screen screen;

    public AbstractEmoteListWidget(Minecraft minecraftClient, int i, int j, int k, int l, Screen screen){
        super(minecraftClient, i, j, k, l);
        this.centerListVertically = false;
        this.screen = screen;
    }


    @Override
    public int getRowWidth(){
        return this.width - 5;
    }

    protected abstract E newEmoteEntry(Minecraft client, EmoteHolder emoteHolder);

    public void emotesSetLeftPos(int left) {
        this.setPosition(left, getY());
    }

    public void setEmotes(Iterable<EmoteHolder> list, boolean showInvalid){
        this.emotes = new ArrayList<>();
        for(EmoteHolder emoteHolder:list){
            this.emotes.add(newEmoteEntry(Minecraft.getInstance(), emoteHolder));
        }
        if(showInvalid) {
            for (EmoteHolder emoteHolder : getEmptyEmotes()) {
                this.emotes.add(newEmoteEntry(Minecraft.getInstance(), emoteHolder));
            }
        }
        this.emotes.sort(Comparator.comparing(o -> o.emote.name.getString().toLowerCase()));
        filter(() -> "");
    }

    public void filter(Supplier<String> string){
        this.clearEntries();
        for(E emote : this.emotes){
            if(emote.emote.name.getString().toLowerCase().contains(string.get()) || emote.emote.description.getString().toLowerCase().contains(string.get()) || emote.emote.author.getString().toLowerCase().equals(string.get())){
                this.addEntry(emote);
            }
        }
        this.setScrollAmount(0);
    }

    public void renderThis(GuiGraphics matrices, int mouseX, int mouseY, float tickDelta) {
        this.render(matrices, mouseX, mouseY, tickDelta);
    }

    @Override
    protected int getScrollbarPosition(){
        return this.getX() + width - 6;
        //return super.getScrollbarPosition();
    }

    @Override
    public boolean isFocused(){
        return screen.getFocused() == this;
    }

    public Iterable<EmoteHolder> getEmptyEmotes(){
        Collection<EmoteHolder> empties = new LinkedList<>();
        for(Pair<UUID, InputConstants.Key> pair : ((ClientConfig) EmoteInstance.config).emoteKeyMap){
            if(!EmoteHolder.list.containsKey(pair.getLeft())){
                empties.add(new EmoteHolder.Empty(pair.getLeft()));
            }
        }
        return empties;
    }

    public static abstract class AbstractEmoteEntry<T extends AbstractEmoteEntry<T>> extends ObjectSelectionList.Entry<T> {
        protected final Minecraft client;
        public final EmoteHolder emote;

        public AbstractEmoteEntry(Minecraft client, EmoteHolder emote){
            this.client = client;
            this.emote = emote;
        }


        @Override
        public void render(@NotNull GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta){
            if(hovered){
                RenderSystem.setShaderColor((float) 1, (float) 1, (float) 1, (float) 1);
                matrices.fill(x - 1, y - 1, x + entryWidth - 9, y + entryHeight + 1, MathHelper.colorHelper(66, 66, 66, 128));
            }
            matrices.drawString(Minecraft.getInstance().font, this.getEmote().name, (int) ((float) (x + 38)), (int) ((float) (y + 1)), 16777215);
            matrices.drawString(Minecraft.getInstance().font, this.getEmote().description, (int) ((float) (x + 38)), (int) ((float) (y + 12)), 8421504);
            if(!this.getEmote().author.getString().isEmpty()) {
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

        public EmoteHolder getEmote() {
            return this.emote;
        }


        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button){
            if(button == 0){
                this.onPressed();
                return true;
            }else{
                return false;
            }
        }

        @Override
        public @NotNull Component getNarration() {
            return this.emote.name;
        }

        protected abstract void onPressed();
    }
}
