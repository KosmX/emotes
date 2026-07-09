package io.github.kosmx.emotes.arch.library;

import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryException;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LoadingEntry extends EmoteListWidget.ListEntry {
    protected static final Component LOADING = Component.translatable("gui.friends.button.loading");
    private final Set<CompletableFuture<?>> futures = ConcurrentHashMap.newKeySet();

    protected final EmoteListWidget widget;
    private final Component message;

    // Last failed operation. Kept as a single field (not a pile of failed futures) so repeated errors don't accumulate.
    private volatile Throwable error;

    public LoadingEntry(EmoteListWidget widget) {
        this(widget, null);
    }

    public LoadingEntry(EmoteListWidget widget, Component message) {
        widget.super(message != null ? message : LOADING, CommonComponents.EMPTY, Collections.emptyList());
        this.widget = widget;
        this.message = message;
    }

    /** A standalone entry that renders {@code error} (e.g. a footer for a failed pagination request). */
    public static LoadingEntry error(EmoteListWidget widget, Throwable error) {
        LoadingEntry entry = new LoadingEntry(widget);
        entry.addForWait(CompletableFuture.failedFuture(error));
        return entry;
    }

    protected <T> CompletableFuture<T> addForWait(CompletableFuture<T> future) {
        this.error = null; // a fresh operation supersedes any previous error
        this.futures.add(future);
        return future.whenComplete((_, th) -> {
            this.futures.remove(future);
            if (th != null) this.error = th;
        });
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
        int centerX = getContentX() + getContentWidth() / 2;
        int centerY = getContentYMiddle();

        if (!this.futures.isEmpty()) { // still loading
            this.widget.active = false;
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font, LOADING, centerX, centerY - font.lineHeight, -1);
            graphics.centeredText(font, LoadingDotsText.get(Util.getMillis()), centerX, centerY, -1);
            return;
        }

        this.widget.active = true;
        Throwable error = this.error;
        if (error != null) {
            renderError(graphics, error, centerX, centerY, getContentWidth());
        } else if (this.message != null) {
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font, this.message, centerX, centerY - font.lineHeight / 2, -1);
        } else {
            super.extractContent(graphics, mouseX, mouseY, hovered, a);
        }
    }

    /** Renders {@code future}'s state (loading dots while pending, wrapped error text once failed) centered. */
    protected static void extractFutureErrors(GuiGraphicsExtractor graphics, CompletableFuture<?> future, int centerX, int centerY, int maxWidth) {
        if (!future.isDone()) {
            Font font = Minecraft.getInstance().font;
            graphics.centeredText(font, LOADING, centerX, centerY - font.lineHeight, -1);
            graphics.centeredText(font, LoadingDotsText.get(Util.getMillis()), centerX, centerY, -1);
        } else if (future.isCompletedExceptionally()) {
            renderError(graphics, future.exceptionNow(), centerX, centerY, maxWidth);
        }
    }

    private static void renderError(GuiGraphicsExtractor graphics, Throwable throwable, int centerX, int centerY, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> lines = font.split(Component.literal(EmoteLibraryException.reason(throwable)), maxWidth);
        int startY = centerY - lines.size() * font.lineHeight / 2;
        for (int i = 0; i < lines.size(); i++) {
            graphics.centeredText(font, lines.get(i), centerX, startY + i * font.lineHeight, -1);
        }
    }

    @Override
    protected void onRemoved() {
        this.widget.active = true;
        this.futures.clear();
        this.error = null;
    }

    @Override
    protected void extractAdditionalContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        // no-op
    }

    @Override
    protected void collectEmotes(Consumer<EmoteListWidget.ListEntry> collection) {
        collection.accept(this);
    }

    @Override
    protected int sortPriority() {
        return 0; // Loading/error rows sort ahead of every other entry type.
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LoadingEntry;
    }

    @Override
    public int hashCode() {
        return 34543534;
    }

    @Override
    public void searchFor(String search, Predicate<EmoteListWidget.ListEntry> matcher, Consumer<EmoteListWidget.ListEntry> results) {
        results.accept(this);
    }
}
