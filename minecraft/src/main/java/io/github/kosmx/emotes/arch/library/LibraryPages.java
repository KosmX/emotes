package io.github.kosmx.emotes.arch.library;

import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryClient;
import org.redlance.emotecraftlibrary.shared.models.PagedResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** A library list read one page at a time, each from where the last one ended. */
final class LibraryPages<T> {
    private static final int PAGE_SIZE = 10;
    private static final Component END = Component.translatable("emotecraft.library.end");

    private final EmoteListWidget widget;
    private final Reader<T> reader;
    private final Consumer<T> show;
    private final Consumer<Throwable> onError;

    private @Nullable CompletableFuture<?> request; // The one on the way, or the one that failed: the end of the list shows it
    private @Nullable Integer next = 0; // Where the next page starts; null after the last one

    LibraryPages(EmoteListWidget widget, Reader<T> reader, Consumer<T> show, Consumer<Throwable> onError) {
        this.widget = widget;
        this.reader = reader;
        this.show = show;
        this.onError = onError;
    }

    void restart() {
        this.next = 0;
        read(0);
    }

    /** Shows {@code future} at the end of the list instead of reading on, e.g. the connection the first page waits for. */
    void waitFor(CompletableFuture<?> future) {
        this.request = future;
    }

    void drop() {
        this.request = null;
    }

    /** A row read before {@link #next} left the list, so the next page starts one earlier. */
    void shiftBack() {
        if (this.next != null) this.next--;
    }

    void paginate() {
        if (this.request != null) {
            this.widget.setFooter(LoadingEntry.waitingFor(this.widget, this.request));
        } else if (this.next == null) {
            this.widget.setFooter(new LoadingEntry(this.widget, END));
        } else {
            this.widget.requestLoadMore(this::more);
        }
    }

    private void more() {
        if (this.request == null && this.next != null) read(this.next); // One queued before a restart or a failure is stale
    }

    private void read(int offset) {
        CompletableFuture<PagedResponse<T, Void>> page = EmoteLibrary.executeAuthorized(client -> this.reader.read(client, offset, PAGE_SIZE));
        this.request = page;
        page.whenCompleteAsync((response, error) -> {
            if (this.request != page) return; // Dropped, or another one took its place
            if (error != null) {
                this.onError.accept(error);
            } else {
                this.request = null;
                // Relative to the offset it was asked at, so a shift made meanwhile stays
                this.next = response.getMeta().isLastPage() ? null : this.next + response.getMeta().getNextOffset() - offset;
                response.getData().forEach(this.show);
            }
            this.widget.refreshFilter();
        }, Minecraft.getInstance());
    }

    @FunctionalInterface
    interface Reader<T> {
        PagedResponse<T, Void> read(EmoteLibraryClient client, int offset, int limit);
    }
}
