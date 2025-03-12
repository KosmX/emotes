package io.github.kosmx.emotes.arch.gui.widgets;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.kosmx.playerAnim.core.util.MathHelper;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.search.ISearchEngine;
import io.github.kosmx.emotes.arch.gui.widgets.search.VanillaSearch;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EmoteListWidget extends ObjectSelectionList<EmoteListWidget.ListEntry> {
    private final FolderEntry mainFolder = new FolderEntry(Component.translatable("emotecraft.folder.main"));
    private boolean compactMode;

    public EmoteListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
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
    protected int scrollBarX() {
        if (!this.compactMode) {
            return super.scrollBarX();
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
        this.mainFolder.entries.clear();
        for (EmoteHolder emoteHolder : list) {
            if (StringUtils.isBlank(emoteHolder.folder)) {
                this.mainFolder.entries.add(new EmoteEntry(emoteHolder));
            } else {
                createFoldersTree(emoteHolder.folder.split("/")).entries.add(new EmoteEntry(emoteHolder));
            }
        }
        if (showInvalid) {
            for (EmoteHolder emoteHolder : getEmptyEmotes()) {
                this.mainFolder.entries.add(new EmoteEntry(emoteHolder));
            }
        }
        filter(VanillaSearch.INSTANCE, "");
    }

    public void filter(ISearchEngine engine, String search) {
        clearEntries();
        engine.filter(getEmotes().stream(), search).forEach(this::addEntry);
        this.setScrollAmount(0);
    }

    public FolderEntry createFoldersTree(String[] folders) {
        FolderEntry last = this.mainFolder;
        for (String folder : folders) {
            last = last.getOrCreateFolder(Component.literal(folder));
        }
        return last;
    }

    public Iterable<EmoteHolder> getEmptyEmotes() {
        Collection<EmoteHolder> empties = new LinkedList<>();
        for(Pair<UUID, InputConstants.Key> pair : PlatformTools.getConfig().emoteKeyMap){
            if(!EmoteHolder.list.containsKey(pair.getLeft())){
                empties.add(new EmoteHolder.Empty(pair.getLeft()));
            }
        }
        return empties;
    }

    public List<ListEntry> getEmotes() {
        return this.mainFolder.getEmotes();
    }

    @Override
    public void updateSize(int width, HeaderAndFooterLayout layout) {
        super.updateSize(width, layout);
        if (compactMode) layout.arrangeElements();
    }

    @Override
    public void updateSizeAndPosition(int width, int height, int y) {
        super.updateSizeAndPosition(compactMode ? width  / 3 : width, height, y);
    }

    @Nullable
    public EmoteHolder getFocusedEmote() {
        if (getFocused() instanceof EmoteEntry emote) {
            return emote.getEmote();
        }
        return null;
    }

    @Override
    public @Nullable ListEntry getHovered() {
        return super.getHovered();
    }

    @Override
    public void setSelected(@Nullable EmoteListWidget.ListEntry selected) {
        super.setSelected(selected);
        if (selected instanceof FolderEntry folder && this.mainFolder.setLastFolder(folder)) {
            System.out.println(appendScreenPath(this.mainFolder, Component.empty()).getString());
        }
    }

    public abstract class ListEntry extends ObjectSelectionList.Entry<ListEntry> {
        public final Component name;
        public final Component description;

        public ListEntry(@NotNull Component name, Component description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public void render(GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int maxX = x + entryWidth - 3 - (compactMode && scrollbarVisible() ? 7 : 0);
            matrices.enableScissor(x - 1, y - 1, maxX, y + entryHeight + 1);
            if (hovered) {
                matrices.fill(x - 1, y - 1, maxX, y + entryHeight + 1, MathHelper.colorHelper(66, 66, 66, 128));
            }
            renderScrollingString(matrices, minecraft.font, this.name, x + 34, x + 34, y + 1, maxX, y + 1 + minecraft.font.lineHeight, 16777215);
            matrices.drawString(minecraft.font, this.description, x + 34, y + 12, 8421504);
            renderAdditional(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
            matrices.disableScissor();
        }

        public abstract void renderAdditional(GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta);

        @Override
        public @NotNull Component getNarration() {
            return this.name;
        }

        public boolean matches(String string) {
            return name.getString().toLowerCase().contains(string.toLowerCase());
        }

        public abstract List<ListEntry> getEmotes();

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ListEntry entry && this.name.equals(entry.name);
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }
    }

    public class EmoteEntry extends ListEntry {
        public final EmoteHolder emote;

        public EmoteEntry(EmoteHolder emote) {
            super(emote.name, emote.description);
            this.emote = emote;
        }

        @Override
        public void renderAdditional(GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
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
        }

        public EmoteHolder getEmote() {
            return this.emote;
        }

        @Override
        public @NotNull Component getNarration() {
            return this.emote.name;
        }

        @Override
        public boolean matches(String string) {
            return super.matches(string) ||
                    description.getString().toLowerCase().contains(string.toLowerCase()) ||
                    emote.author.getString().equalsIgnoreCase(string);
        }

        @Override
        public List<ListEntry> getEmotes() {
            return Collections.singletonList(this);
        }
    }

    public class FolderEntry extends ListEntry {
        public static final Component FOLDER_DESC = Component.translatable("emotecraft.folder");

        private final List<ListEntry> entries = new ArrayList<>();
        private FolderEntry next;

        public FolderEntry(@NotNull Component name) {
            super(name, FOLDER_DESC);
        }

        @Override
        public void renderAdditional(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            // no-op (Icon maybe)
        }

        public boolean isInvalid() {
            return StringUtils.isBlank(this.name.getString());
        }

        @Override
        public List<ListEntry> getEmotes() {
            List<ListEntry> emotes = new ArrayList<>();
            if (this.next == null || !this.entries.contains(this.next)) {
                for (var entry : this.entries) {
                    if (entry instanceof FolderEntry folder && folder.isInvalid()) {
                        emotes.addAll(entry.getEmotes());
                    } else {
                        emotes.add(entry);
                    }
                }
            } else {
                emotes.addAll(this.next.getEmotes());
            }
            emotes.sort(Comparator.comparing(o -> o.name.getString().toLowerCase()));
            return Collections.unmodifiableList(emotes);
        }

        public boolean setLastFolder(FolderEntry folder) {
            if (this.next != null) {
                return this.next.setLastFolder(folder);
            } else {
                return setSelectedFolder(folder);
            }
        }

        public boolean setSelectedFolder(FolderEntry folder) {
            if (this.entries.contains(folder)) {
                this.next = folder;
                return true;
            }
            return false;
        }

        public FolderEntry getOrCreateFolder(Component name) {
            for (ListEntry entry : this.entries) {
                if (entry instanceof FolderEntry folder) {
                    if (folder.name.equals(name)) {
                        return folder;
                    }
                }
            }
            FolderEntry folder = new FolderEntry(name);
            this.entries.add(folder);
            return folder;
        }
    }

    public void setCompactMode(boolean compactMode) {
        this.compactMode = compactMode;
    }

    public static MutableComponent appendScreenPath(FolderEntry folder, MutableComponent component) {
        component = component.append(McUtils.SLASH).append(CommonComponents.SPACE).append(folder.name);

        if (folder.next != null) {
            return appendScreenPath(folder.next, component.append(CommonComponents.SPACE));
        } else {
            return component;
        }
    }
}
