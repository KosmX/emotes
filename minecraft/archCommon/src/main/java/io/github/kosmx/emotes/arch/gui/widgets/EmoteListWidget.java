package io.github.kosmx.emotes.arch.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.search.ISearchEngine;
import io.github.kosmx.emotes.arch.gui.widgets.search.VanillaSearch;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EmoteListWidget extends ObjectSelectionList<EmoteListWidget.EmoteEntry> {
    protected List<EmoteEntry> emotes = new ArrayList<>();
    private boolean compactMode;

    public EmoteListWidget(Minecraft minecraftClient, int i, int j, int k, int l) {
        super(minecraftClient, i, j, k, l);
        this.centerListVertically = false;
    }

    @Override
    public int getRowWidth() {
        if (this.compactMode) {
            return this.width;
        }

        return this.width / 2;
    }

    @Override
    protected int getScrollbarPosition() {
        if (!this.compactMode) {
            return super.getScrollbarPosition();
        }
        return getX() + getRowWidth() - SCROLLBAR_WIDTH;
    }


    @Override
    protected void renderSelection(@NotNull GuiGraphics guiGraphics, int i, int j, int k, int l, int m) {
        if (this.compactMode && scrollbarVisible()) {
            int o = getRowLeft() - 2;
            int p = getRight() - 6 - 1;
            int q = i - 2;
            int r = i + k + 2;
            guiGraphics.fill(o, q, p, r, l);
            guiGraphics.fill(o + 1, q + 1, p - 1, r - 1, m);
        } else {
            super.renderSelection(guiGraphics, i, j, k, l, m);
        }
    }

    public void setEmotes(Iterable<EmoteHolder> list, boolean showInvalid) {
        this.emotes.clear();
        for (EmoteHolder emoteHolder : list) {
            this.emotes.add(new EmoteEntry(emoteHolder));
        }
        if (showInvalid) {
            for (EmoteHolder emoteHolder : getEmptyEmotes()) {
                this.emotes.add(new EmoteEntry(emoteHolder));
            }
        }
        this.emotes.sort(Comparator.comparing(o -> o.emote.name.getString().toLowerCase()));
        filter(VanillaSearch.INSTANCE, "");
    }

    public void filter(ISearchEngine engine, String search) {
        clearEntries();
        engine.filter(this.emotes.stream(), search).forEach(this::addEntry);
        this.setScrollAmount(0);
    }

    public Iterable<EmoteHolder> getEmptyEmotes(){
        Collection<EmoteHolder> empties = new LinkedList<>();
        for(Pair<UUID, InputConstants.Key> pair : PlatformTools.getConfig().emoteKeyMap){
            if(!EmoteHolder.list.containsKey(pair.getLeft())){
                empties.add(new EmoteHolder.Empty(pair.getLeft()));
            }
        }
        return empties;
    }

    public List<EmoteEntry> getEmotes() {
        return Collections.unmodifiableList(this.emotes);
    }

    public class EmoteEntry extends ObjectSelectionList.Entry<EmoteEntry> {
        public final EmoteHolder emote;

        public EmoteEntry(EmoteHolder emote) {
            this.emote = emote;
        }

        @Override
        public void render(@NotNull GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int maxX = x + entryWidth - 3 - (compactMode && scrollbarVisible() ? 7 : 0);
            matrices.enableScissor(x - 1, y - 1, maxX, y + entryHeight + 1);
            if (hovered) {
                matrices.fill(x - 1, y - 1, maxX, y + entryHeight + 1, MathHelper.colorHelper(66, 66, 66, 128));
            }
            renderScrollingString(matrices, minecraft.font, this.emote.name, x + 34, x + 34, y + 1, maxX, y + 1 + minecraft.font.lineHeight, 16777215);
            matrices.drawString(minecraft.font, this.emote.description, x + 34, y + 12, 8421504);
            if(!this.emote.author.getString().isEmpty()) {
                Component text = Component.translatable("emotecraft.emote.author")
                    .withStyle(ChatFormatting.GOLD)
                    .append(this.emote.author);

                matrices.drawString(minecraft.font, text, x + 34, y + 23, 8421504);
            }

            ResourceLocation texture = this.emote.getIconIdentifier();
            if (texture != null){
                RenderSystem.enableBlend();
                matrices.blit(RenderType::guiTextured, texture, x, y, 0.0F, 0.0F, 32, 32, 256, 256, 256, 256);
                RenderSystem.disableBlend();
            }
            matrices.disableScissor();
        }

        public EmoteHolder getEmote() {
            return this.emote;
        }

        @Override
        public @NotNull Component getNarration() {
            return this.emote.name;
        }
    }

    public void setCompactMode(boolean compactMode) {
        this.compactMode = compactMode;
    }
}
