package io.github.kosmx.emotes.arch.gui.widgets;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.search.ISearchEngine;
import io.github.kosmx.emotes.arch.gui.widgets.search.VanillaSearch;
import io.github.kosmx.emotes.arch.screen.utils.BageUtils;
import io.github.kosmx.emotes.arch.screen.utils.PageButton;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.mc.McUtils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class EmoteListWidget extends ObjectSelectionList<EmoteListWidget.ListEntry> {
    private static final List<Component> LAST_OPENED_PATH = new CopyOnWriteArrayList<>();

    private final FolderEntry mainFolder = new FolderEntry(Component.translatable("emotecraft.folder.main"));
    private FolderEntry lastClickedFolder;
    private boolean compactMode;

    private final PageButton backButton = new PageButton(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT, RecipeBookPage.PAGE_BACKWARD_SPRITES, true, button -> {
        setLastFolder(null);
        if (this.lastClickedFolder == null) {
            setScrollAmount(0);
        } else {
            centerScrollOn(this.lastClickedFolder);
        }
    });

    public EmoteListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight, minecraft.font.lineHeight);
        this.centerListVertically = false;
        this.backButton.active = false;
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
    protected void renderHeader(GuiGraphics guiGraphics, int x, int y) {
        Component path = appendScreenPath(this.mainFolder, Component.empty());
        if (compactMode) {
            renderScrollingString(guiGraphics, minecraft.font, path,
                    x, x, y - 4, getRowRight() - 3, (y + headerHeight) - 4, -1
            );
        } else {
            renderScrollingString(guiGraphics, minecraft.font, path,
                    getX() + 3, y - 4, getRight() - 3, (y + headerHeight) - 4, -1
            );
        }
    }

    @Override
    protected void renderSelection(@NotNull GuiGraphics guiGraphics, int top, int width, int height, int outerColor, int innerColor) {
        if (this.compactMode && scrollbarVisible()) {
            int o = getRowLeft() - 2;
            int p = getRight() - 6 - 1;
            int q = top - 2;
            int r = top + height + 2;
            guiGraphics.fill(o, q, p, r, outerColor);
            guiGraphics.fill(o + 1, q + 1, p - 1, r - 1, innerColor);
        } else {
            super.renderSelection(guiGraphics, top, width, height, outerColor, innerColor);
        }
    }

    @Override
    protected void renderItem(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int index, int left, int top, int width, int height) {
        try { // Concurrency issues
            super.renderItem(guiGraphics, mouseX, mouseY, partialTick, index, left, top, width, height);
        } catch (Throwable ignored) {}
    }

    public void setEmotes(Iterable<EmoteHolder> list, boolean showInvalid) {
        this.mainFolder.entries.clear();
        for (EmoteHolder emoteHolder : list) {
            if (emoteHolder.folder.isEmpty()) {
                this.mainFolder.entries.put(emoteHolder.name, new EmoteEntry(emoteHolder));
            } else {
                createFoldersTree(emoteHolder.folder).entries.put(emoteHolder.name, new EmoteEntry(emoteHolder));
            }
        }
        if (showInvalid) {
            for (EmoteHolder emoteHolder : getEmptyEmotes()) {
                this.mainFolder.entries.put(emoteHolder.name, new EmoteEntry(emoteHolder));
            }
        }
        filter(VanillaSearch.INSTANCE, false, "");

        for (Component folderName : LAST_OPENED_PATH) {
            ListEntry child = Objects.requireNonNullElse(this.lastClickedFolder, this.mainFolder).entries.get(folderName);
            if (child instanceof FolderEntry folder) {
                this.lastClickedFolder = folder;
                setLastFolder(folder);
            } else break;
        }
    }

    public void filter(ISearchEngine engine, boolean isSearchActive, String search) {
        clearEntries();
        engine.filter(getEmotes(isSearchActive).stream(), search).forEach(this::addEntry);
        refreshScrollAmount();
    }

    public FolderEntry createFoldersTree(List<Component> folders) {
        FolderEntry last = this.mainFolder;
        for (Component folder : folders) {
            last = last.getOrCreateFolder(folder);
        }
        return last;
    }

    public Iterable<EmoteHolder> getEmptyEmotes() {
        Collection<EmoteHolder> empties = new LinkedList<>();
        for(Pair<UUID, InputConstants.Key> pair : PlatformTools.getConfig().emoteKeyMap) {
            if (!EmoteHolder.list.containsKey(pair.left())) {
                empties.add(new EmoteHolder.Empty(pair.left()));
            }
        }
        return empties;
    }

    public List<ListEntry> getEmotes(boolean isSearchActive) {
        List<ListEntry> emotes = new ArrayList<>();
        this.mainFolder.collectEmotes(isSearchActive, emotes);

        emotes.sort(ListEntry::compareTo);
        return Collections.unmodifiableList(emotes);
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
        if (selected instanceof FolderEntry folder) {
            this.lastClickedFolder = folder;
            setLastFolder(folder);
        }
    }

    public boolean setLastFolder(FolderEntry folder) {
        if (this.mainFolder.setLastFolder(folder)) {
            this.backButton.active = this.mainFolder.next != null;
            EmoteListWidget.LAST_OPENED_PATH.clear();
            updateLastOpenedPath(this.mainFolder.next);
            return true;
        }
        return false;
    }

    public abstract class ListEntry extends ObjectSelectionList.Entry<ListEntry> implements Comparable<ListEntry> {
        public final Component name;
        public final Component description;
        public final List<Component> bages;

        public ListEntry(@NotNull Component name, Component description, List<Component> bages) {
            this.name = name;
            this.description = description;
            this.bages = bages;
        }

        @Override
        public void render(GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int maxX = x + entryWidth - 3 - (compactMode && scrollbarVisible() ? 7 : 0);
            matrices.enableScissor(x - 1, y - 1, maxX, y + entryHeight + 1);
            if (hovered) {
                matrices.fill(x - 1, y - 1, maxX, y + entryHeight + 1, ARGB.color(128, 66, 66, 66));
            }
            int maxBadgesWidth = Math.max(maxX - minecraft.font.width(this.name), maxX / 3) - (x + 34);
            int badgeWidth = BageUtils.drawBadges(matrices, minecraft.font, this.bages, maxX, y + 1, maxBadgesWidth, true);
            renderScrollingString(matrices, minecraft.font, this.name, x + 34, x + 34, y + 1, maxX - badgeWidth, y + 1 + minecraft.font.lineHeight, -1);
            matrices.drawString(minecraft.font, this.description, x + 34, y + 12, -8355712);
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

        protected abstract void collectEmotes(boolean isSearchActive, List<ListEntry> collection);

        @Override
        public abstract boolean equals(Object obj);

        @Override
        public abstract int hashCode();

        @Override
        public int compareTo(@NotNull EmoteListWidget.ListEntry o) {
            return this.name.getString().compareTo(o.name.getString());
        }
    }

    public class EmoteEntry extends ListEntry {
        public final EmoteHolder emote;

        public EmoteEntry(EmoteHolder emote) {
            super(emote.name, emote.description, emote.bages);
            this.emote = emote;
        }

        @Override
        public void renderAdditional(GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if (!this.emote.author.getString().isEmpty()) {
                Component text = Component.translatable("emotecraft.emote.author")
                        .withStyle(ChatFormatting.GOLD)
                        .append(this.emote.author);

                matrices.drawString(minecraft.font, text, x + 34, y + 23, -8355712);
            }

            ResourceLocation texture = this.emote.getIconIdentifier();
            if (texture != null) {
                GlStateManager._enableBlend();
                matrices.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, 32, 32, 256, 256, 256, 256);
                GlStateManager._disableBlend();
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
                    (emote.fileName != null && emote.fileName.getString().toLowerCase().contains(string.toLowerCase())) ||
                    emote.author.getString().equalsIgnoreCase(string);
        }

        @Override
        protected void collectEmotes(boolean excludeFolders, List<ListEntry> collection) {
            collection.add(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof EmoteEntry entry && this.emote.equals(entry.emote);
        }

        @Override
        public int hashCode() {
            return this.emote.hashCode();
        }

        @Override
        public int compareTo(@NotNull ListEntry o) {
            if (o instanceof EmoteEntry) {
                return super.compareTo(o);
            } else {
                return 1;
            }
        }
    }

    public class FolderEntry extends ListEntry {
        public static final ResourceLocation FOLDER = McUtils.newIdentifier("textures/folder.png");
        public static final ResourceLocation FOLDER_OPEN = McUtils.newIdentifier("textures/folder_open.png");
        public static final Component FOLDER_DESC = Component.translatable("emotecraft.folder");

        private final Map<Component, ListEntry> entries = new HashMap<>();
        private FolderEntry next;

        public FolderEntry(@NotNull Component name) {
            super(name, FOLDER_DESC, Collections.emptyList());
        }

        @Override
        public void renderAdditional(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, hovering ? FOLDER_OPEN : FOLDER, left, top, 0.0F, 0.0F, 32, 32, 32, 32);
        }

        @Override
        protected void collectEmotes(boolean isSearchActive, List<ListEntry> collection) {
            if (this.next == null || !this.entries.containsValue(this.next)) {
                for (var entry : this.entries.values()) {
                    if (entry instanceof FolderEntry folder) {
                        boolean isInvalid = StringUtils.isBlank(this.name.getString());
                        if (!isInvalid) collection.add(folder);

                        if (isSearchActive || isInvalid) {
                            for (var folderEntry : folder.entries.values()) {
                                folderEntry.collectEmotes(isSearchActive, collection);
                            }
                        }
                    } else {
                        entry.collectEmotes(isSearchActive, collection);
                    }
                }
            } else {
                this.next.collectEmotes(isSearchActive, collection);
            }
        }

        public boolean setLastFolder(FolderEntry folder) {
            if (this.next != null) {
                if (folder == null && this.next.next == null) {
                    setSelectedFolder(null);
                    return true;
                }
                return this.next.setLastFolder(folder);
            }
            return setSelectedFolder(folder);
        }

        public boolean setSelectedFolder(FolderEntry folder) {
            if (folder == null || this.entries.containsValue(folder)) {
                this.next = folder;
                return true;
            }
            return false;
        }

        public FolderEntry getOrCreateFolder(Component name) {
            return (FolderEntry) this.entries.computeIfAbsent(name, FolderEntry::new);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FolderEntry entry && this.name.equals(entry.name);
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }

        @Override
        public int compareTo(@NotNull ListEntry o) {
            if (o instanceof FolderEntry) {
                return super.compareTo(o);
            } else {
                return -1;
            }
        }
    }

    public void setCompactMode(boolean compactMode) {
        this.compactMode = compactMode;
    }

    public static MutableComponent appendScreenPath(FolderEntry folder, MutableComponent component) {
        component = component.append(McUtils.SLASH).append(CommonComponents.SPACE);

        if (folder.next != null) {
            return appendScreenPath(folder.next, component.append(folder.name).append(CommonComponents.SPACE));
        } else {
            return component.append(folder.name.copy().withStyle(style -> style.withBold(true)));
        }
    }

    private static void updateLastOpenedPath(@Nullable FolderEntry folder) {
        if (folder == null) return;
        EmoteListWidget.LAST_OPENED_PATH.add(folder.name);
        if (folder.next != null) updateLastOpenedPath(folder.next);
    }

    public PageButton createBackButton() {
        return this.backButton;
    }
}
