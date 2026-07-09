package io.github.kosmx.emotes.arch.library;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryException;
import org.redlance.emotecraftlibrary.sdk.GameEmoteInfo;
import org.redlance.emotecraftlibrary.sdk.LibraryListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class LibraryFolderEntry extends EmoteListWidget.FolderEntry implements LibraryListener {
    public static final Identifier EMOTECRAFT_LIBRARY_ICON = McUtils.newIdentifier("textures/redlance_emotes_icon.png");
    private static final int PAGE_SIZE = 10;
    private static final Component END = Component.translatable("emotecraft.library.end");

    private final EmoteListWidget widget;

    private final Map<UUID, LibraryEmoteEntry> searchResults = new HashMap<>();
    private CompletableFuture<AutoCloseable> connection;
    // Bumped on every open/close. Async callbacks capture it and bail if it changed, so a stale connection's
    // listener callbacks and in-flight fetches can't mutate state after the folder was reopened or discarded.
    private int connectionGeneration;
    private int emoteOrder;
    private int likedOffset;
    private boolean likedLastPage;
    private Throwable likedError;

    private String searchQuery;
    private List<UUID> searchResultIds;
    private int searchOffset;
    private boolean searchLastPage;
    private LoadingEntry searchLoading;

    public LibraryFolderEntry(EmoteListWidget widget) {
        widget.super(AcceptPrivacyScreen.TITLE, CommonComponents.EMPTY);
        this.widget = widget;
    }

    @Override
    protected void onOpen() {
        if (this.connection != null) {
            return; // The live connection is already opening or open.
        }

        int generation = ++this.connectionGeneration;
        this.connection = EmoteLibrary.executeAuthorized(client -> client.openLiveConnection(gated(generation), Minecraft.getInstance()));
        getOrPutLoadingEntry().addForWait(this.connection);
    }

    /** @return whether {@code generation} still names the live connection — false once the folder was reopened or discarded. */
    private boolean isCurrent(int generation) {
        return generation == this.connectionGeneration;
    }

    /**
     * Wraps this folder as a listener bound to one connection: it drops every callback once that connection is
     * no longer the live one, so a stale connection that hasn't finished closing can't mutate the reopened folder.
     */
    private LibraryListener gated(int generation) {
        return new LibraryListener() {
            @Override
            public void onReset() {
                if (isCurrent(generation)) LibraryFolderEntry.this.onReset();
            }

            @Override
            public void onAdd(List<GameEmoteInfo> list) {
                if (isCurrent(generation)) LibraryFolderEntry.this.onAdd(list);
            }

            @Override
            public void onRemove(List<UUID> list) {
                if (isCurrent(generation)) LibraryFolderEntry.this.onRemove(list);
            }

            @Override
            public void onError(EmoteLibraryException error, boolean fatal) {
                if (isCurrent(generation)) LibraryFolderEntry.this.onError(error, fatal);
            }
        };
    }

    @Override
    protected void onRemoved() {
        super.onRemoved();

        this.connectionGeneration++; // mute the current connection's callbacks and any in-flight liked fetches

        this.searchResults.values().forEach(LibraryEmoteEntry::onRemoved);
        this.searchResults.clear();
        this.searchQuery = null; // superseded — also mutes any in-flight search callback (it checks searchQuery)
        this.searchResultIds = null;
        this.searchLoading = null;

        if (this.connection != null) {
            this.connection.whenComplete((closeable, _) -> close(closeable));
            this.connection = null;
        }
    }

    @Override
    protected void extractAdditionalContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float tickDelta) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, EMOTECRAFT_LIBRARY_ICON, getX(), getContentY(), 0.0F, 0.0F, 32, 32, 32, 32);
    }

    private static void close(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch (Exception e) {
            CommonData.LOGGER.warn("Failed to close EmotecraftLibrary connection!", e);
        }
    }

    public LoadingEntry getOrPutLoadingEntry() {
        if (this.entries.get("loading") instanceof LoadingEntry entry) {
            return entry;
        } else {
            LoadingEntry loading = new LoadingEntry(this.widget);
            this.entries.put("loading", loading);
            this.widget.refreshFilter();
            return loading;
        }
    }

    @Override
    public void onReset() {
        int generation = this.connectionGeneration;
        getOrPutLoadingEntry().addForWait(EmoteLibrary.executeAuthorized(client -> client.listLiked(0, PAGE_SIZE))
                .whenCompleteAsync((resp, th) -> {
                    if (!isCurrent(generation) || th != null) return;
                    clearChildren();
                    this.emoteOrder = 0;
                    this.likedError = null;
                    putAll(resp.getData());
                    this.likedOffset = resp.getMeta().getNextOffset();
                    this.likedLastPage = resp.getMeta().isLastPage();
                    this.widget.refreshFilter();
                }, Minecraft.getInstance())
        );
    }

    private void loadMoreLiked() {
        int generation = this.connectionGeneration;
        int offset = this.likedOffset;
        EmoteLibrary.executeAuthorized(client -> client.listLiked(offset, PAGE_SIZE))
                .whenCompleteAsync((resp, th) -> {
                    if (!isCurrent(generation)) return;
                    if (th != null) {
                        this.likedError = th; // surface it as a footer instead of silently stalling pagination
                        this.widget.refreshFilter();
                        return;
                    }
                    putAll(resp.getData());
                    this.likedOffset = resp.getMeta().getNextOffset();
                    this.likedLastPage = resp.getMeta().isLastPage();
                    this.widget.refreshFilter();
                }, Minecraft.getInstance());
    }

    /** Sets up liked-emote pagination on the open folder each time the list is rebuilt (see {@link EmoteListWidget.FolderEntry#paginate}). */
    @Override
    public void paginate(EmoteListWidget widget) {
        if (this.likedError != null) {
            widget.setFooter(LoadingEntry.error(widget, this.likedError)); // a page failed — show it, stop paging until onReset
        } else if (this.likedLastPage) {
            widget.setFooter(new LoadingEntry(widget, END));
        } else if (this.likedOffset > 0) { // Only once the first page has loaded.
            widget.requestLoadMore(this::loadMoreLiked);
        }
    }

    @Override
    public void onAdd(List<GameEmoteInfo> list) {
        putAll(list);
        this.widget.refreshFilter();
    }

    @Override
    public void onRemove(List<UUID> list) {
        for (UUID removal : list) removeChild(removal);
        this.widget.refreshFilter();
    }

    private void putAll(List<GameEmoteInfo> list) {
        for (GameEmoteInfo info : list) {
            LibraryEmoteEntry entry = getOrCreate(info);
            entry.order = this.emoteOrder++;
            this.entries.put(info.getId(), entry);
        }
    }

    @Override
    public void onError(EmoteLibraryException e, boolean fatal) {
        if (!fatal) {
            return; // Transient (network/5xx): the SDK reconnects itself with backoff and re-syncs via onReset().
        }

        // Fatal (session expired/revoked): the SDK stopped reconnecting. Surface the error and drop the dead
        // connection so re-opening the library re-authorizes and reopens the stream (onOpen guards on connection != null).
        getOrPutLoadingEntry().addForWait(CompletableFuture.failedFuture(e));
        if (this.connection != null) {
            this.connection.whenComplete((closeable, _) -> close(closeable));
            this.connection = null;
        }
    }

    @Override
    protected int sortPriority() {
        return 1; // The library folder sorts ahead of regular folders. equals/hashCode: inherited name-based (singleton).
    }

    @Override
    public void searchFor(String search, Predicate<EmoteListWidget.ListEntry> matcher, Consumer<EmoteListWidget.ListEntry> results) {
        if (PlatformTools.getConfig().cloudLibraryStatus.get() != LibraryStatus.ENABLED) {
            return; // Privacy not accepted yet — don't hit the server (would fail with "not enabled").
        }

        if (!Objects.equals(search, this.searchQuery)) { // New query: reset and fetch the first page.
            this.searchQuery = search;
            this.searchResultIds = null;
            this.searchOffset = 0;
            this.searchLastPage = false;
            this.searchLoading = new LoadingEntry(this.widget);
            loadSearchPage();
        }

        if (this.searchResultIds == null) {
            results.accept(this.searchLoading);
        } else {
            for (UUID id : this.searchResultIds) {
                // A result may be a liked entry (this.entries) or a search-only one (searchResults).
                if (this.entries.getOrDefault(id, this.searchResults.get(id)) instanceof LibraryEmoteEntry entry) {
                    results.accept(entry);
                }
            }

            if (this.searchLastPage) {
                this.widget.setFooter(new LoadingEntry(this.widget, END));
            } else {
                this.widget.requestLoadMore(this::loadSearchPage);
            }
        }
    }

    private void loadSearchPage() {
        String search = this.searchQuery;
        int offset = this.searchOffset;
        boolean firstPage = this.searchResultIds == null;

        var future = EmoteLibrary.executeAuthorized(client -> client.search(search, offset, PAGE_SIZE))
                .whenCompleteAsync((resp, th) -> {
                    if (!Objects.equals(search, this.searchQuery)) {
                        return; // A newer query superseded this one.
                    }

                    if (th != null) {
                        CommonData.LOGGER.warn("Failed to search!", th);
                        return; // Keep the loading entry, which now renders the error.
                    }

                    List<UUID> ids = this.searchResultIds != null ? new ArrayList<>(this.searchResultIds) : new ArrayList<>();
                    for (GameEmoteInfo info : resp.getData()) {
                        getOrCreate(info);
                        ids.add(info.getId());
                    }
                    this.searchResultIds = ids;
                    this.searchOffset = resp.getMeta().getNextOffset();
                    this.searchLastPage = resp.getMeta().isLastPage();
                    this.widget.refreshFilter(); // Re-render with the results (drops the loading entry).
                }, Minecraft.getInstance());

        if (firstPage) {
            this.searchLoading.addForWait(future);
        }
    }

    private LibraryEmoteEntry getOrCreate(GameEmoteInfo info) {
        EmoteListWidget.ListEntry exiting = this.entries.getOrDefault(info.getId(), this.searchResults.get(info.getId()));
        if (!(exiting instanceof LibraryEmoteEntry)) {
            LibraryEmoteEntry searchEntry = new LibraryEmoteEntry(this.widget, info);
            searchEntry.onOpen();
            this.searchResults.put(info.getId(), searchEntry);
            return searchEntry;
        }
        return (LibraryEmoteEntry) exiting;
    }

    public final class LibraryEmoteEntry extends EmoteListWidget.EmoteLikeEntry {
        private final GameEmoteInfo info;
        private final Identifier icon;
        private final Path iconPath;
        private CompletableFuture<ClientAsset.Texture> texture;

        private CompletableFuture<Animation> emoteFuture;
        private int order;

        public LibraryEmoteEntry(EmoteListWidget widget, GameEmoteInfo info) {
            widget.super(
                    McUtils.fromJson(info.getName()),
                    McUtils.fromJson(info.getDescription()),
                    McUtils.fromJson(info.getAuthor()),
                    EmoteHolder.computeBages(info.getTags())
            );

            this.info = info;
            // Texture id is per-emote so releasing one entry's icon never yanks another's when two emotes share an
            // icon hash; the on-disk cache file stays keyed by hash so the download is still deduplicated.
            this.icon = McUtils.newIdentifier("libraryicon/" + info.getId());
            this.iconPath = InstanceService.INSTANCE.getCacheDirectory().resolve("libraryicon/" + info.getIconHash());
        }

        @Override
        protected void onOpen() {
            if (this.texture != null) {
                return; // The icon is already downloading or registered.
            }

            try {
                Files.createDirectories(this.iconPath.getParent());
            } catch (IOException ignored) {}

            this.texture = Minecraft.getInstance().getSkinManager().skinTextureDownloader.downloadAndRegisterSkin(
                    this.icon, this.iconPath, this.info.getIconUrl(), false
            );
        }

        @Override
        protected void onRemoved() {
            if (this.texture == null) {
                return;
            }

            CompletableFuture<ClientAsset.Texture> texture = this.texture;
            this.texture = null;
            // Release the texture only once it has actually been registered (both run on the render thread).
            texture.whenComplete((_, throwable) -> {
                if (throwable == null) {
                    Minecraft.getInstance().getTextureManager().release(this.icon);
                }
            });
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
            boolean loading = this.emoteFuture != null && !this.emoteFuture.isDone();
            boolean failed = this.emoteFuture != null && this.emoteFuture.isCompletedExceptionally();

            LibraryFolderEntry.this.widget.active = !loading;
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
            if (this.texture != null && this.texture.isDone() && !this.texture.isCompletedExceptionally()) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, this.icon, getContentX(), getContentY(), 0.0F, 0.0F, 32, 32, 256, 256, 256, 256);
            }
        }

        @Override
        public CompletableFuture<Animation> getEmote() {
            try {
                return emoteFuture = EmoteLibrary.ANIMATION_CACHE.get(this.info.getId());
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
            return this.info.hashCode();
        }

        @Override
        public int compareTo(@NotNull EmoteListWidget.ListEntry o) {
            if (o instanceof LibraryEmoteEntry entry) {
                return Integer.compare(this.order, entry.order); // Keep the order the library sent them in.
            } else {
                return super.compareTo(o); // Falls back to the shared priority order (emotes after folders).
            }
        }

        @Override
        public void searchFor(String search, Predicate<EmoteListWidget.ListEntry> matcher, Consumer<EmoteListWidget.ListEntry> results) {
            // no-op
        }
    }
}
