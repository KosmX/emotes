package io.github.kosmx.emotes.arch.library;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.arch.library.modals.AcceptPrivacyScreen;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryClient;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryException;
import org.redlance.emotecraftlibrary.sdk.GameBundleInfo;
import org.redlance.emotecraftlibrary.sdk.GameEmoteInfo;
import org.redlance.emotecraftlibrary.sdk.GameLibraryItem;
import org.redlance.emotecraftlibrary.sdk.LibraryListener;
import org.redlance.emotecraftlibrary.shared.models.PagedResponse;

import java.net.HttpURLConnection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class LibraryFolderEntry extends PagedFolderEntry<GameLibraryItem> {
    public static final Identifier EMOTECRAFT_LIBRARY_ICON = McUtils.newIdentifier("textures/redlance_emotes_icon.png");
    private static final Component DESCRIPTION = Component.translatable("emotecraft.library.description");

    private final LibraryIcons icons = new LibraryIcons();
    private @Nullable Stream stream;
    private @Nullable Search search;

    public LibraryFolderEntry(EmoteListWidget widget) {
        super(widget, AcceptPrivacyScreen.TITLE, DESCRIPTION, CommonComponents.EMPTY);
    }

    @Override
    protected void onOpen() {
        if (this.stream != null && !this.stream.connection.isCompletedExceptionally()) return; // Open, or opening
        if (PlatformTools.getConfig().cloudLibraryStatus.get() != LibraryStatus.ENABLED) {
            leave(); // Reopened after the library was turned off on another screen
            return;
        }

        this.stream = new Stream();
        waitFor(this.stream.connection);
    }

    @Override
    protected void onRemoved() {
        super.onRemoved();
        closeStream();
        this.icons.release(); // The next draw loads them again, should the screen come back
    }

    @Override
    protected void extractAdditionalContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float tickDelta) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, EMOTECRAFT_LIBRARY_ICON, getX(), getContentY(), 0.0F, 0.0F, 32, 32, 32, 32);
    }

    /** @return whether the library shows the given emote, at its root or in the open bundle, so a bound copy of it isn't shown elsewhere too. */
    public boolean hasLoaded(UUID id) {
        LibraryBundleEntry bundle = openBundle();
        return shows(id) || bundle != null && bundle.shows(id);
    }

    @Override
    protected void onLoadError(Throwable throwable) {
        if (!LibraryModals.show(throwable)) return; // Not actionable: the end of the list shows it

        closeStream(); // So reopening the library connects again
        leave();
    }

    @Override
    protected PagedResponse<GameLibraryItem, Void> fetch(EmoteLibraryClient client, int offset, int limit) {
        return client.listLiked(offset, limit);
    }

    @Override
    protected EmoteListWidget.ListEntry entry(GameLibraryItem item, @Nullable EmoteListWidget.ListEntry shown) {
        return switch (item) {
            case GameBundleInfo info -> shown instanceof LibraryBundleEntry bundle && bundle.displays(info) ? bundle : new LibraryBundleEntry(this.widget, info);
            case GameEmoteInfo info -> emote(info, shown);
        };
    }

    @Override
    protected int sortPriority() {
        return 1; // The library folder sorts ahead of regular folders. equals/hashCode: inherited name-based (singleton).
    }

    @Override
    public void searchFor(String query, Predicate<EmoteListWidget.ListEntry> matcher, Consumer<EmoteListWidget.ListEntry> results) {
        if (PlatformTools.getConfig().cloudLibraryStatus.get() != LibraryStatus.ENABLED) {
            return; // Privacy not accepted yet — don't hit the server (would fail with "not enabled").
        }

        if (this.search == null || !this.search.query.equals(query)) {
            if (this.search != null) this.search.pages.drop();
            this.search = new Search(query);
        }
        this.search.rows.values().forEach(results);
        this.search.pages.paginate();
    }

    private void closeStream() {
        if (this.stream == null) return;
        this.stream.close();
        this.stream = null;
    }

    /** Shows {@code future} where the root and the open bundle wait for their pages. */
    private void waitFor(CompletableFuture<?> future) {
        this.pages.waitFor(future);
        LibraryBundleEntry bundle = openBundle();
        if (bundle != null) bundle.pages.waitFor(future);
        this.widget.refreshFilter();
    }

    private @Nullable LibraryBundleEntry openBundle() {
        return this.widget.openFolder() instanceof LibraryBundleEntry bundle ? bundle : null;
    }

    private void open(LibraryBundleEntry bundle) {
        if (openBundle() != null) this.widget.setLastFolder(null);
        if (this.widget.openFolder() != this && !this.widget.setLastFolder(this)) return;
        this.widget.setLastFolder((LibraryBundleEntry) put(bundle.info)); // A search result may be on a page not loaded yet
    }

    private void leave() {
        while (this.widget.openFolder() == this || openBundle() != null) {
            this.widget.setLastFolder(null);
        }
    }

    private LibraryEmoteEntry emote(GameEmoteInfo info, @Nullable EmoteListWidget.ListEntry shown) {
        return shown instanceof LibraryEmoteEntry emote && emote.info.equals(info) ? emote : new LibraryEmoteEntry(this.widget, info);
    }

    private static boolean liesAtRoot(GameLibraryItem item) {
        return !(item instanceof GameEmoteInfo info) || info.getBundleIds().isEmpty();
    }

    private void placeInOpenBundle(GameLibraryItem item, BiConsumer<LibraryBundleEntry, GameEmoteInfo> put) {
        LibraryBundleEntry bundle = openBundle();
        if (bundle == null || !(item instanceof GameEmoteInfo info)) return;

        if (info.getBundleIds().contains(bundle.info.getId())) {
            put.accept(bundle, info);
        } else {
            bundle.remove(info.getId());
        }
    }

    /** Library rows compare equal among themselves, so the stable sort keeps their folder's order. */
    private static boolean isRow(EmoteListWidget.ListEntry entry) {
        return entry instanceof LibraryEmoteEntry || entry instanceof LibraryBundleEntry;
    }

    /** One live connection: a replaced or closed one may still have callbacks on the way, which it ignores. */
    private final class Stream implements LibraryListener {
        private final CompletableFuture<AutoCloseable> connection = EmoteLibrary.executeAuthorized(client -> client.openLiveConnection(this, Minecraft.getInstance()));

        private Stream() {
            this.connection.whenCompleteAsync((_, error) -> {
                if (error != null && isCurrent()) onLoadError(error);
            }, Minecraft.getInstance());
        }

        private boolean isCurrent() {
            return LibraryFolderEntry.this.stream == this;
        }

        private void close() {
            this.connection.whenComplete((closeable, _) -> EmoteLibrary.close(closeable));
        }

        @Override
        public void onReset() {
            if (!isCurrent()) return;

            reload();
            LibraryBundleEntry bundle = openBundle();
            if (bundle != null) bundle.reload(); // Its changes around the disconnect are lost too
        }

        @Override
        public void onAdd(List<GameLibraryItem> items) {
            if (!isCurrent()) return;

            for (GameLibraryItem item : items) {
                if (liesAtRoot(item)) putLast(item);
                placeInOpenBundle(item, LibraryBundleEntry::putLast);
            }
            widget.refreshFilter();
        }

        @Override
        public void onUpdate(List<GameLibraryItem> items) {
            if (!isCurrent()) return;

            for (GameLibraryItem item : items) {
                if (liesAtRoot(item)) {
                    put(item);
                } else {
                    remove(item.getId()); // Moved into a bundle
                }
                placeInOpenBundle(item, LibraryBundleEntry::put);
                if (search != null) search.rows.computeIfPresent(item.getId(), (_, shown) -> entry(item, shown));
            }
            widget.refreshFilter();
        }

        @Override
        public void onRemove(List<UUID> ids) {
            if (!isCurrent()) return;

            LibraryBundleEntry bundle = openBundle();
            for (UUID id : ids) {
                remove(id);
                if (bundle != null) bundle.remove(id);
                if (search != null && search.rows.remove(id) != null) search.pages.shiftBack();
            }
            widget.refreshFilter();
        }

        @Override
        public void onError(EmoteLibraryException error, boolean fatal) {
            if (!fatal || !isCurrent()) return; // Transient (network/5xx): the SDK reconnects itself with backoff and re-syncs via onReset().

            // Fatal (session expired/revoked): the SDK stopped reconnecting. Show it, and let reopening the library connect again.
            waitFor(CompletableFuture.failedFuture(error));
            closeStream();
        }
    }

    private final class Search {
        private final String query;
        private final SequencedMap<UUID, EmoteListWidget.ListEntry> rows = new LinkedHashMap<>();
        private final LibraryPages<GameLibraryItem> pages;

        private Search(String query) {
            this.query = query;
            this.pages = new LibraryPages<>(widget, (client, offset, limit) -> client.search(query, offset, limit), this::put, error -> {
                LibraryModals.show(error);
                CommonData.LOGGER.warn("Failed to search!", error);
            });
            this.pages.restart();
        }

        private void put(GameLibraryItem item) {
            this.rows.put(item.getId(), entry(item, this.rows.get(item.getId())));
        }
    }

    public final class LibraryBundleEntry extends PagedFolderEntry<GameEmoteInfo> {
        private final GameBundleInfo info;

        public LibraryBundleEntry(EmoteListWidget widget, GameBundleInfo info) {
            super(widget, McUtils.fromJson(info.getName()), McUtils.fromJson(info.getDescription()), McUtils.fromJson(info.getAuthor()));
            this.info = info;
        }

        /** Not equals: the emote count isn't shown, so its change keeps the row and the emotes loaded in it. */
        private boolean displays(GameBundleInfo info) {
            return Objects.equals(this.info.getName(), info.getName())
                    && Objects.equals(this.info.getDescription(), info.getDescription())
                    && Objects.equals(this.info.getAuthor(), info.getAuthor())
                    && Objects.equals(this.info.getIconHash(), info.getIconHash())
                    && Objects.equals(this.info.getIconUrl(), info.getIconUrl());
        }

        @Override
        protected void onOpen() {
            reload(); // Read each time it opens: a closed bundle isn't kept up to date
        }

        @Override
        protected void select() {
            open(this);
        }

        @Override
        protected PagedResponse<GameEmoteInfo, Void> fetch(EmoteLibraryClient client, int offset, int limit) {
            return client.listLiked(this.info.getId(), offset, limit);
        }

        @Override
        protected EmoteListWidget.ListEntry entry(GameEmoteInfo item, @Nullable EmoteListWidget.ListEntry shown) {
            return emote(item, shown);
        }

        @Override
        protected void onLoadError(Throwable throwable) {
            if (EmoteLibrary.unwrap(throwable) instanceof EmoteLibraryException.Http http && http.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                LibraryFolderEntry.this.remove(this.info.getId()); // No longer liked: the library answers 404 for such a bundle
            } else {
                LibraryFolderEntry.this.onLoadError(throwable);
            }
        }

        @Override
        protected void extractAdditionalContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float tickDelta) {
            if (!icons.extract(graphics, this.info, getContentX(), getContentY())) {
                super.extractAdditionalContent(graphics, mouseX, mouseY, hovering, tickDelta);
            }
            extractAuthor(graphics);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof LibraryBundleEntry entry && this.info.getId().equals(entry.info.getId());
        }

        @Override
        public int hashCode() {
            return this.info.getId().hashCode();
        }

        @Override
        public int compareTo(@NotNull EmoteListWidget.ListEntry o) {
            return isRow(o) ? 0 : super.compareTo(o);
        }

        @Override
        public void searchFor(String search, Predicate<EmoteListWidget.ListEntry> matcher, Consumer<EmoteListWidget.ListEntry> results) {
            // no-op
        }
    }

    public final class LibraryEmoteEntry extends EmoteListWidget.EmoteLikeEntry {
        private final GameEmoteInfo info;
        private CompletableFuture<Animation> emoteFuture;

        public LibraryEmoteEntry(EmoteListWidget widget, GameEmoteInfo info) {
            widget.super(
                    McUtils.fromJson(info.getName()),
                    McUtils.fromJson(info.getDescription()),
                    McUtils.fromJson(info.getAuthor()),
                    EmoteHolder.computeBages(info.getTags())
            );

            this.info = info;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            boolean loading = this.emoteFuture != null && !this.emoteFuture.isDone();
            boolean failed = this.emoteFuture != null && this.emoteFuture.isCompletedExceptionally();

            if (loading || failed) {
                int centerX = getContentX() + getContentWidth() / 2;
                int centerY = getContentYMiddle();
                LoadingEntry.extractFutureErrors(graphics, this.emoteFuture, centerX, centerY, getContentWidth());
            } else {
                super.extractContent(graphics, mouseX, mouseY, hovered, a);
            }
        }

        @Override
        protected void extractAdditionalContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            super.extractAdditionalContent(graphics, mouseX, mouseY, hovered, tickDelta);
            icons.extract(graphics, this.info, getContentX(), getContentY());
        }

        @Override
        public CompletableFuture<Animation> getEmote() {
            try {
                return this.emoteFuture = EmoteLibrary.ANIMATION_CACHE.get(this.info.getId());
            } catch (ExecutionException e) {
                return CompletableFuture.failedFuture(e);
            }
        }

        @Override
        public UUID getUuid() {
            return this.info.getId();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof LibraryEmoteEntry entry && this.info.getId().equals(entry.info.getId());
        }

        @Override
        public int hashCode() {
            return this.info.getId().hashCode();
        }

        @Override
        public int compareTo(@NotNull EmoteListWidget.ListEntry o) {
            return isRow(o) ? 0 : super.compareTo(o);
        }

        @Override
        public void searchFor(String search, Predicate<EmoteListWidget.ListEntry> matcher, Consumer<EmoteListWidget.ListEntry> results) {
            // no-op
        }
    }
}
