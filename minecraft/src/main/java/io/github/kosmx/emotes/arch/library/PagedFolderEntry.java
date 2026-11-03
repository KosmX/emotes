package io.github.kosmx.emotes.arch.library;

import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryClient;
import org.redlance.emotecraftlibrary.sdk.GameLibraryItem;
import org.redlance.emotecraftlibrary.shared.models.PagedResponse;

import java.util.List;
import java.util.UUID;

public abstract class PagedFolderEntry<T extends GameLibraryItem> extends EmoteListWidget.FolderEntry {
    protected final EmoteListWidget widget;
    final LibraryPages<T> pages;

    protected PagedFolderEntry(EmoteListWidget widget, Component name, Component description, Component author) {
        widget.super(name, description, author);
        this.widget = widget;
        this.pages = new LibraryPages<>(widget, this::fetch, this::putLast, this::onLoadError);
    }

    protected abstract PagedResponse<T, Void> fetch(EmoteLibraryClient client, int offset, int limit);

    /** @return {@code shown} if it shows {@code item} as it is, else a new row for it */
    protected abstract EmoteListWidget.ListEntry entry(T item, @Nullable EmoteListWidget.ListEntry shown);

    protected abstract void onLoadError(Throwable throwable);

    @Override
    protected void onRemoved() {
        super.onRemoved();
        this.pages.drop();
    }

    @Override
    public void paginate(EmoteListWidget widget) {
        this.pages.paginate();
    }

    void reload() {
        EmoteListWidget.ListEntry open = this.widget.openFolder();
        for (Object key : List.copyOf(this.entries.keySet())) {
            if (this.entries.get(key) != open) removeChild(key); // The open bundle stays open
        }
        this.pages.restart();
        this.widget.refreshFilter();
    }

    /** Shows {@code item} in place of its row here, or last if it has none. @return the row */
    EmoteListWidget.ListEntry put(T item) {
        EmoteListWidget.ListEntry shown = this.entries.get(item.getId());
        EmoteListWidget.ListEntry row = entry(item, shown);
        EmoteListWidget.ListEntry open = this.widget.openFolder();
        putChild(item.getId(), row);
        if (shown == open && row != shown && row instanceof EmoteListWidget.FolderEntry folder) {
            setSelectedFolder(folder); // A changed bundle stays open, as its new row
        }
        return row;
    }

    void putLast(T item) {
        this.entries.putLast(item.getId(), put(item)); // A row shown already moves there
    }

    void remove(UUID id) {
        EmoteListWidget.ListEntry shown = this.entries.get(id);
        if (shown == null) return;

        if (shown == this.widget.openFolder()) {
            this.widget.setLastFolder(null);
        }
        this.pages.shiftBack();
        removeChild(id);
    }

    boolean shows(UUID id) {
        return this.entries.containsKey(id);
    }
}
