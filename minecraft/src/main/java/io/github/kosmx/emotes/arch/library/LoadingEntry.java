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
import org.jetbrains.annotations.Nullable;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryException;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LoadingEntry extends EmoteListWidget.ListEntry {
    protected static final Component LOADING = Component.translatable("gui.friends.button.loading");

    private final @Nullable Component message;
    private final @Nullable CompletableFuture<?> future;

    public LoadingEntry(EmoteListWidget widget, Component message) {
        this(widget, message, null);
    }

    private LoadingEntry(EmoteListWidget widget, @Nullable Component message, @Nullable CompletableFuture<?> future) {
        widget.super(message != null ? message : LOADING, CommonComponents.EMPTY, Collections.emptyList());
        this.message = message;
        this.future = future;
    }

    /** A standalone entry that renders {@code future}: loading while it runs, its error once it failed. */
    public static LoadingEntry waitingFor(EmoteListWidget widget, CompletableFuture<?> future) {
        return new LoadingEntry(widget, null, future);
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
        int centerX = getContentX() + getContentWidth() / 2;
        int centerY = getContentYMiddle();

        if (this.future != null && (!this.future.isDone() || this.future.isCompletedExceptionally())) {
            extractFutureErrors(graphics, this.future, centerX, centerY, getContentWidth());
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
