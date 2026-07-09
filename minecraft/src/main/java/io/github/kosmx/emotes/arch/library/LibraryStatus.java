package io.github.kosmx.emotes.arch.library;

public enum LibraryStatus {
    DISABLED(false),
    SHOW_FOLDER(true),
    ENABLED(true);

    public final boolean showEntry;


    LibraryStatus(boolean showEntry) {
        this.showEntry = showEntry;
    }
}
