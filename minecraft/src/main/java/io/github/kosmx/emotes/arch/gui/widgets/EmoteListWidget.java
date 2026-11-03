package io.github.kosmx.emotes.arch.gui.widgets;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.cursor.CursorType;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.search.ISearchEngine;
import io.github.kosmx.emotes.arch.library.LibraryFolderEntry;
import io.github.kosmx.emotes.arch.library.PagedFolderEntry;
import io.github.kosmx.emotes.arch.screen.utils.BageUtils;
import io.github.kosmx.emotes.arch.screen.utils.PageButton;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EmoteListWidget extends ObjectSelectionList<EmoteListWidget.ListEntry> {
    private static final List<Component> LAST_OPENED_PATH = new CopyOnWriteArrayList<>();
    private static final long SCROLL_STOP_DELAY = 400L; // ms the list has to stay still to count as stopped

    private final LibraryFolderEntry libraryEntry = new LibraryFolderEntry(this);
    private final FolderEntry mainFolder = new FolderEntry(Component.translatable("emotecraft.folder.main"));
    private final List<Runnable> loadMore = new ArrayList<>();
    private FolderEntry lastClickedFolder;
    private ListEntry footer;
    private boolean compactMode;
    private long lastScrollTime;

    private final PageButton backButton = new PageButton(Button.DEFAULT_HEIGHT, Button.DEFAULT_HEIGHT, RecipeBookPage.PAGE_BACKWARD_SPRITES, true, button -> {
        setLastFolder(null);
        if (this.lastClickedFolder == null) {
            setScrollAmount(0);
        } else {
            centerScrollOn(this.lastClickedFolder);
        }
    });

    public EmoteListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
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
    protected void extractSelection(@NonNull GuiGraphicsExtractor graphics, @NonNull ListEntry entry, int outlineColor) {
        if (this.compactMode && scrollable()) {
            int j = entry.getX();
            int k = entry.getY();
            int l = j + entry.getWidth() - 8;
            int m = k + entry.getHeight();
            graphics.fill(j, k, l, m, outlineColor);
            graphics.fill(j + 1, k + 1, l - 1, m - 1, -16777216);
        } else {
            super.extractSelection(graphics, entry, outlineColor);
        }
    }

    @Override
    protected void extractItem(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, @NonNull ListEntry entry) {
        try { // Concurrency issues
            super.extractItem(graphics, mouseX, mouseY, a, entry);
        } catch (Throwable ignored) {}
    }

    @Override
    public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        // Load the next chunk once scrolled near the end (or immediately while the content doesn't fill the viewport).
        if (!this.loadMore.isEmpty() && scrollAmount() >= maxScrollAmount() * 0.8) {
            List<Runnable> pending = new ArrayList<>(this.loadMore);
            this.loadMore.clear();
            pending.forEach(Runnable::run);
        }
    }

    @Override
    public void setScrollAmount(double scrollAmount) {
        double previous = scrollAmount();
        super.setScrollAmount(scrollAmount);
        if (scrollAmount() != previous) this.lastScrollTime = Util.getMillis();
    }

    /** @return whether the list scrolled in the last {@link #SCROLL_STOP_DELAY} ms, so its rows may still be sliding under the cursor. */
    public boolean isScrolling() {
        return Util.getMillis() - this.lastScrollTime < SCROLL_STOP_DELAY;
    }

    public void setEmotes(Iterable<EmoteHolder> list, boolean showInvalid) {
        boolean showLibrary = PlatformTools.getConfig().cloudLibraryStatus.get().showEntry;
        for (ListEntry entry : this.mainFolder.entries.values()) {
            if (entry != this.libraryEntry || !showLibrary) entry.onRemoved(); // The library entry put back keeps its stream and rows
        }
        this.mainFolder.entries.clear();
        if (showLibrary) {
            this.mainFolder.entries.put("emotecraftlibrary", this.libraryEntry);
        }
        for (EmoteHolder emoteHolder : list) {
            if (emoteHolder.folder.isEmpty()) {
                this.mainFolder.entries.put(emoteHolder.get(), new EmoteHolder.EmoteHolderEntry(this, emoteHolder));
            } else {
                createFoldersTree(emoteHolder.folder).entries.put(emoteHolder.get(), new EmoteHolder.EmoteHolderEntry(this, emoteHolder));
            }
        }
        if (showInvalid) {
            for (EmoteHolder emoteHolder : getEmptyEmotes()) {
                this.mainFolder.entries.put(emoteHolder.get(), new EmoteHolder.EmoteHolderEntry(this, emoteHolder));
            }
        }
        filter(null, "");

        for (Component folderName : LAST_OPENED_PATH) {
            ListEntry child = Objects.requireNonNullElse(this.lastClickedFolder, this.mainFolder).entries.get(folderName);
            if (child instanceof FolderEntry folder) {
                this.lastClickedFolder = folder;
                setLastFolder(folder);
            } else break;
        }
    }

    public void filter(@Nullable ISearchEngine engine, String search) {
        this.loadMore.clear();
        this.footer = null;
        clearEntries();
        addEntry(new HeaderEntry(), (int)(9.0F * 1.5F));
        setSelected(null);
        if (engine != null) {
            engine.filter(this.mainFolder, search, this::addEntry);
        } else {
            getEmotes().forEach(this::addEntry);
            openFolder().paginate(this);
        }
        if (this.footer != null) {
            addEntry(this.footer);
        }
        refreshScrollAmount();
    }

    /** Registers a callback invoked once the list is scrolled near its end. Cleared on every {@link #filter}. */
    public void requestLoadMore(Runnable action) {
        this.loadMore.add(action);
    }

    /** Sets an entry appended after the current contents (e.g. an end-of-list marker). Cleared on every {@link #filter}. */
    public void setFooter(@Nullable ListEntry footer) {
        this.footer = footer;
    }

    public FolderEntry openFolder() {
        FolderEntry folder = this.mainFolder;
        for (FolderEntry child = folder.openChild(); child != null; child = folder.openChild()) {
            folder = child;
        }
        return folder;
    }

    public void refreshFilter() {}

    /**
     * Notifies every entry (recursively) that it is being removed from the list, e.g. when the screen
     * is closed. Library folders use this to close their live connection.
     */
    public void releaseEntries() {
        this.mainFolder.entries.values().forEach(ListEntry::onRemoved);
    }

    /** Counterpart of {@link #releaseEntries}: only the main folder's open child, which brings back what is open inside it. */
    public void reopenEntries() {
        FolderEntry open = this.mainFolder.openChild();
        if (open != null) open.onOpen();
    }

    public FolderEntry createFoldersTree(List<Component> folders) {
        FolderEntry last = this.mainFolder;
        for (Component folder : folders) {
            last = last.getOrCreateFolder(folder);
        }
        return last;
    }

    /**
     * @return key-bound emotes not shown anywhere else in the current view — neither in {@link EmoteHolder#list}
     * (local/server emotes) nor already loaded by the library folder — so they stay manageable without duplicating.
     */
    public Iterable<EmoteHolder> getEmptyEmotes() {
        Collection<EmoteHolder> extras = new LinkedList<>();
        for (EmoteHolder holder : PlatformTools.getConfig().keyBinds.values()) {
            UUID id = holder.getUuid();
            if (!EmoteHolder.list.containsKey(id) && !this.libraryEntry.hasLoaded(id)) extras.add(holder);
        }
        return extras;
    }

    public List<ListEntry> getEmotes() {
        List<ListEntry> emotes = new ArrayList<>();
        this.mainFolder.collectEmotes(emotes::add);

        emotes.sort(ListEntry::compareTo);
        return Collections.unmodifiableList(emotes);
    }

    @Override
    public void updateSize(int width, @NonNull HeaderAndFooterLayout layout) {
        setSize(this.compactMode ? width / 3 : width, layout.getContentHeight()); // Only the size, the layout places us afterwards
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
            folder.select();
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
        protected static final int START_OFFSET = 33;
        protected static final int DESCRIPTION_Y = 12;
        protected static final int SUBTEXT_Y = 23;

        public final Component name;
        public final Component description;
        public final Component author;
        public final List<Component> bages;
        private final boolean descriptionBlank;
        private final boolean hasSubtext;

        public ListEntry(@NotNull Component name, Component description, List<Component> bages) {
            this(name, description, CommonComponents.EMPTY, bages);
        }

        public ListEntry(@NotNull Component name, Component description, Component author, List<Component> bages) {
            this.name = name;
            this.description = description;
            this.author = author;
            this.bages = bages;
            this.descriptionBlank = StringUtils.isBlank(description.getString()); // description is final; avoid rebuilding the string every frame
            this.hasSubtext = !author.getString().isEmpty(); // author is final; avoid re-rendering the string every frame
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            int maxX = getContentRight() - (compactMode && scrollable() ? 7 : 0);
            graphics.enableScissor(getX() - 1, getY() - 1, maxX, getY() + getHeight() + 1);
            if (hovered) {
                graphics.requestCursor(isFocused() ? CursorType.DEFAULT : CursorTypes.POINTING_HAND);
                graphics.fill(getContentX() - 1, getContentY() - 1, maxX, getContentBottom() + 1, ARGB.color(128, 66, 66, 66));
            }
            int lineHeight = minecraft.font.lineHeight;
            int top = getContentY() + contentShift(); // Vertically center the whole text block within the row.

            int maxBadgesWidth = Math.max(maxX - minecraft.font.width(this.name), maxX / 3) - (getContentX() + START_OFFSET);
            int badgeWidth = BageUtils.extractBadges(graphics, minecraft.font, badges(), maxX, top, maxBadgesWidth, true);
            graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE).acceptScrolling(this.name, getContentX() + START_OFFSET, getContentX() + START_OFFSET, maxX - badgeWidth, top, top + lineHeight);
            graphics.text(minecraft.font, this.description, getContentX() + START_OFFSET, top + DESCRIPTION_Y, -8355712);
            extractAdditionalContent(graphics, mouseX, mouseY, hovered, a);
            graphics.disableScissor();
        }

        protected abstract void extractAdditionalContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta);

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            return super.isMouseOver(mouseX, mouseY) && !(isOverScrollbar(mouseX, mouseY) && scrollable()); // The compact list draws its scrollbar over the rows
        }

        @Override
        public @NotNull Component getNarration() {
            return this.name;
        }

        /** Called when this entry, a folder, is opened, or reopened by {@link EmoteListWidget#reopenEntries}. */
        protected void onOpen() {}

        /** Called when this entry is removed from the list (its parent folder is cleared or rebuilt). */
        protected void onRemoved() {}

        /** @return {@code true} when {@link #extractAdditionalContent} draws a line below the name (e.g. the author). */
        protected boolean hasSubtext() {
            return this.hasSubtext;
        }

        /** @return the badges drawn right of the name */
        protected List<Component> badges() {
            return this.bages;
        }

        /** Vertical offset that centers the name/description/sub-text block within the row. */
        protected int contentShift() {
            int lineHeight = minecraft.font.lineHeight;
            int blockBottom;
            if (hasSubtext()) {
                blockBottom = subtextY() + lineHeight;
            } else if (!this.descriptionBlank) {
                blockBottom = DESCRIPTION_Y + lineHeight;
            } else {
                blockBottom = lineHeight;
            }
            return Math.max(0, (getContentBottom() - getContentY() - blockBottom) / 2);
        }

        /** Y-offset within the text block at which {@link #extractAdditionalContent} draws its sub-line. */
        protected int subtextY() {
            return this.descriptionBlank ? DESCRIPTION_Y : SUBTEXT_Y;
        }

        protected void extractAuthor(GuiGraphicsExtractor graphics) {
            if (!hasSubtext()) return;

            Component text = Component.translatable("emotecraft.emote.author")
                    .withStyle(ChatFormatting.GOLD)
                    .append(this.author);

            graphics.text(minecraft.font, text, getContentX() + START_OFFSET, getContentY() + contentShift() + subtextY(), -8355712);
        }

        public boolean matches(String string) {
            return name.getString().toLowerCase().contains(string.toLowerCase());
        }

        protected abstract void collectEmotes(Consumer<ListEntry> collection);

        @Override
        public abstract boolean equals(Object obj);

        @Override
        public abstract int hashCode();

        /**
         * Lower sorts first. Gives every entry type a single, total sort order (loading &lt; library folder &lt;
         * folder &lt; emote), so mixing types in one {@link #compareTo} never violates the comparator contract.
         * Library rows compare equal among themselves to keep their folder's order, so they're only sorted with each other and loading rows.
         */
        protected int sortPriority() {
            return 100;
        }

        @Override
        public int compareTo(@NotNull EmoteListWidget.ListEntry o) {
            int cmp = Integer.compare(sortPriority(), o.sortPriority());
            return cmp != 0 ? cmp : this.name.getString().compareTo(o.name.getString());
        }

        public abstract void searchFor(String search, Predicate<ListEntry> matcher, Consumer<ListEntry> results);
    }

    public final class HeaderEntry extends ListEntry {
        public HeaderEntry() {
            super(CommonComponents.EMPTY, CommonComponents.EMPTY, Collections.emptyList());
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            Component path = appendScreenPath(mainFolder, Component.empty());
            if (compactMode) {
                graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE).acceptScrolling(path,
                        getContentX(), getContentX(), getContentRight(), getContentY(), getContentY() + minecraft.font.lineHeight
                );
            } else {
                graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE).acceptScrollingWithDefaultCenter(path,
                        getContentX(), getContentRight(), getContentY(), getContentY() + minecraft.font.lineHeight
                );
            }
        }

        @Override
        protected void extractAdditionalContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            // no-op
        }

        @Override
        protected void collectEmotes(Consumer<ListEntry> collection) {
            // no-op
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof HeaderEntry;
        }

        @Override
        public int hashCode() {
            return 984359084;
        }

        @Override
        public void searchFor(String search, Predicate<ListEntry> matcher, Consumer<ListEntry> results) {
            // no-op
        }

        @Override
        public boolean matches(String string) {
            return false;
        }
    }

    public abstract class EmoteLikeEntry extends ListEntry {
        public EmoteLikeEntry(@NotNull Component name, Component description, Component author, List<Component> bages) {
            super(name, description, author, bages);
        }

        @Override
        protected void extractAdditionalContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            extractAuthor(graphics);
        }

        public abstract CompletableFuture<Animation> getEmote();

        @Override
        public boolean matches(String string) {
            return super.matches(string) ||
                    this.description.getString().toLowerCase().contains(string.toLowerCase()) ||
                    this.author.getString().equalsIgnoreCase(string);
        }

        @Override
        protected void collectEmotes(Consumer<ListEntry> collection) {
            collection.accept(this);
        }

        @Override
        public abstract boolean equals(Object obj);

        @Override
        public abstract int hashCode();

        @Override
        protected int sortPriority() {
            return 3; // Emotes sort after folders.
        }

        public abstract UUID getUuid();
    }

    public class FolderEntry extends ListEntry {
        public static final Identifier FOLDER = McUtils.newIdentifier("textures/folder.png");
        public static final Identifier FOLDER_OPEN = McUtils.newIdentifier("textures/folder_open.png");
        public static final Component FOLDER_DESC = Component.translatable("emotecraft.folder");

        protected final SequencedMap<Object, ListEntry> entries = new LinkedHashMap<>();
        private final Supplier<List<Component>> badges = Suppliers.memoize(() -> List.of(Component.translatable("emotecraft.folder.emotes", countEmotes()).withStyle(ChatFormatting.GRAY)));
        private FolderEntry next;

        private FolderEntry(Object obj) {
            this((Component) obj, FOLDER_DESC);
        }

        public FolderEntry(@NotNull Component name, @NotNull Component description) {
            this(name, description, CommonComponents.EMPTY);
        }

        public FolderEntry(@NotNull Component name, @NotNull Component description, Component author) {
            super(name, description, author, Collections.emptyList());
        }

        @Override
        protected void extractAdditionalContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float tickDelta) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, hovering ? FOLDER_OPEN : FOLDER, getX(), getContentY(), 0.0F, 0.0F, 32, 32, 32, 32);
        }

        @Override
        protected List<Component> badges() {
            return this.badges.get();
        }

        /** @return the emotes in this folder and its subfolders */
        protected int countEmotes() {
            int count = 0;
            for (ListEntry entry : this.entries.values()) {
                count += entry instanceof FolderEntry folder ? folder.countEmotes() : 1;
            }
            return count;
        }

        @Override
        protected void collectEmotes(Consumer<ListEntry> collection) {
            FolderEntry open = openChild();
            if (open == null) {
                for (var entry : this.entries.values()) {
                    if (entry instanceof FolderEntry folder) {
                        boolean isInvalid = StringUtils.isBlank(this.name.getString());
                        if (!isInvalid) collection.accept(folder);

                        if (isInvalid) {
                            for (var folderEntry : folder.entries.values()) {
                                folderEntry.collectEmotes(collection);
                            }
                        }
                    } else {
                        entry.collectEmotes(collection);
                    }
                }
            } else {
                open.collectEmotes(collection);
            }
        }

        /** @return the child folder open in this one, if any */
        private @Nullable FolderEntry openChild() {
            return this.next != null && this.entries.containsValue(this.next) ? this.next : null;
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

        /** Opens this folder once it's selected; a search result may live elsewhere than the open folder. */
        protected void select() {
            EmoteListWidget.this.setLastFolder(this);
        }

        public boolean setSelectedFolder(FolderEntry folder) {
            if (folder == null || this.entries.containsValue(folder)) {
                this.next = folder;
                if (folder != null) {
                    folder.onOpen();
                }
                return true;
            }
            return false;
        }

        @Override
        protected void onRemoved() {
            for (ListEntry entry : this.entries.values()) {
                entry.onRemoved();
            }
        }

        /** Puts {@code entry} under {@code key}, in place of the child there, which is notified; a new key goes last. */
        protected void putChild(Object key, ListEntry entry) {
            ListEntry replaced = this.entries.put(key, entry);
            if (replaced != null && replaced != entry) {
                replaced.onRemoved();
            }
        }

        /** Removes and notifies the child mapped to {@code key}, if present. */
        protected void removeChild(Object key) {
            ListEntry removed = this.entries.remove(key);
            if (removed != null) {
                removed.onRemoved();
            }
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
        protected int sortPriority() {
            return 2; // Folders sort before emotes.
        }

        /**
         * Called on the currently open folder each time the (unfiltered) list is rebuilt, so a folder can
         * append its own footer or register lazy pagination. No-op by default; {@link PagedFolderEntry} overrides it.
         */
        public void paginate(EmoteListWidget widget) {}

        @Override
        public void searchFor(String search, Predicate<ListEntry> matcher, Consumer<ListEntry> results) {
            FolderEntry open = openChild();
            if (open != null) {
                open.searchFor(search, matcher, results);
            } else {
                for (var entry : this.entries.values()) {
                    if (entry instanceof FolderEntry folder && StringUtils.isBlank(this.name.getString())) {
                        for (var folderEntry : folder.entries.values()) folderEntry.searchFor(search, matcher, results);
                    } else {
                        entry.searchFor(search, matcher, results);
                    }
                }
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
