package io.github.kosmx.emotes.arch.library;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryClient;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryException;
import org.redlance.emotecraftlibrary.sdk.GameEmoteInfo;
import org.redlance.emotecraftlibrary.sdk.LibraryListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class LibraryFolderEntry extends EmoteListWidget.FolderEntry implements LibraryListener, BiConsumer<AutoCloseable, Throwable> {
    public static final Identifier EMOTECRAFT_LIBRARY_ICON = McUtils.newIdentifier("textures/redlance_emotes_icon.png");

    private final EmoteListWidget widget;
    private final MutableComponent status;

    private final Map<UUID, LibraryEmoteEntry> searchResults = new HashMap<>();
    private CompletableFuture<AutoCloseable> connection;
    private boolean removed;

    public LibraryFolderEntry(EmoteListWidget widget) {
        widget.super(AcceptPrivacyScreen.TITLE, Component.empty());
        this.status = (MutableComponent) this.description;
        this.widget = widget;
    }

    @Override
    protected void onOpen() {
        if (this.connection != null) {
            return; // The live connection is already opening or open.
        }

        this.connection = EmoteLibrary.executeAuthorized(client -> client.openLiveConnection(this));
        getOrPutLoadingEntry().addForWait(this.connection);
        this.connection.whenComplete(this);
    }

    @Override
    protected void onRemoved() {
        super.onRemoved();
        this.removed = true;

        this.searchResults.values().forEach(LibraryEmoteEntry::onRemoved);
        this.searchResults.clear();

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
        getOrPutLoadingEntry().addForWait(EmoteLibrary.executeAuthorized(EmoteLibraryClient::listLiked)
                .whenCompleteAsync((resp, th) -> {
                    if (th != null) return;
                    clearChildren();
                    putAll(resp.getData());
                    this.widget.refreshFilter();
                }, Minecraft.getInstance())
        );
    }

    @Override
    public void onAdd(List<GameEmoteInfo> list) {
        Minecraft.getInstance().execute(() -> {
            putAll(list);
            this.widget.refreshFilter();
        });
    }

    @Override
    public void onRemove(List<UUID> list) {
        Minecraft.getInstance().execute(() -> {
            for (UUID removal : list) removeChild(removal);
            this.widget.refreshFilter();
        });
    }

    private void putAll(List<GameEmoteInfo> list) {
        for (GameEmoteInfo info : list) {
            this.entries.put(info.getId(), getOrCreate(info));
        }
    }

    @Override
    public void onError(EmoteLibraryException e, boolean b) {
        Minecraft.getInstance().execute(() ->
                getOrPutLoadingEntry().addForWait(CompletableFuture.failedFuture(e))
        );
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LibraryFolderEntry;
    }

    @Override
    public int compareTo(@NotNull EmoteListWidget.ListEntry o) {
        if (o instanceof LibraryFolderEntry) {
            return super.compareTo(o);
        } else {
            return -1;
        }
    }

    @Override
    public void accept(AutoCloseable closeable, Throwable throwable) {
        this.status.getSiblings().clear();
        if (throwable != null) {
            this.status.append(throwable.toString());
        }
    }

    @Override
    public void searchFor(String search, Predicate<EmoteListWidget.ListEntry> matcher, Consumer<EmoteListWidget.ListEntry> results) {
        LoadingEntry entry = getOrPutLoadingEntry();
        results.accept(entry);

        entry.addForWait(EmoteLibrary.executeAuthorized(client -> client.search(search, 0, 10 /* TODO proper limit */))
                .whenCompleteAsync((resp, th) -> {
                    if (this.removed) { return; } // The folder was removed while the search was in flight.

                    if (th != null) {
                        CommonData.LOGGER.warn("Failed to search!", th);
                        return;
                    }

                    for (GameEmoteInfo info : resp.getData()) {
                        results.accept(getOrCreate(info));
                    }
                }, Minecraft.getInstance())
        );
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
        private final EmoteListWidget widget;
        private final GameEmoteInfo info;
        private final Identifier icon;
        private final Path iconPath;
        private CompletableFuture<ClientAsset.Texture> texture;

        private CompletableFuture<Animation> emoteFuture;

        public LibraryEmoteEntry(EmoteListWidget widget, GameEmoteInfo info) {
            widget.super(
                    McUtils.fromJson(info.getName()),
                    McUtils.fromJson(info.getDescription()),
                    McUtils.fromJson(info.getAuthor()),
                    EmoteHolder.computeBages(info.getTags())
            );
            this.widget = widget;

            this.info = info;
            this.icon = McUtils.newIdentifier("libraryicon/" + info.getIconHash());
            this.iconPath = InstanceService.INSTANCE.getCacheDirectory().resolve(this.icon.getPath());
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

            this.widget.active = !loading;
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
            if (o instanceof LibraryEmoteEntry) {
                return super.compareTo(o);
            } else {
                return 1;
            }
        }

        @Override
        public void searchFor(String search, Predicate<EmoteListWidget.ListEntry> matcher, Consumer<EmoteListWidget.ListEntry> results) {
            // no-op
        }
    }
}
