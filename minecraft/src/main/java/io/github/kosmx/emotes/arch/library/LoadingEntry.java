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

    public LoadingEntry(EmoteListWidget widget) {
        widget.super(LOADING, CommonComponents.EMPTY, Collections.emptyList());
        this.widget = widget;
    }

    protected <T> CompletableFuture<T> addForWait(CompletableFuture<T> future) {
        this.futures.add(future);
        return future.whenComplete((_, th) -> {
            if (th == null) this.futures.remove(future);
        });
    }

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a) {
        if (this.futures.isEmpty()) {
            this.widget.active = true;
            super.extractContent(graphics, mouseX, mouseY, hovered, a);
            return;
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(this.futures.toArray(CompletableFuture[]::new));

        int centerX = getContentX() + getContentWidth() / 2;
        int centerY = getContentYMiddle();

        if (!allOf.isDone()) {
            this.widget.active = false;
        } else {
            this.widget.active = true;
            if (!allOf.isCompletedExceptionally()) {
                super.extractContent(graphics, mouseX, mouseY, hovered, a);
                return;
            }
        }

        extractFutureErrors(graphics, allOf, centerX, centerY, getContentWidth());
    }

    protected static void extractFutureErrors(GuiGraphicsExtractor graphics, CompletableFuture<?> future, int centerX, int centerY, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        if (!future.isDone()) {
            graphics.centeredText(font, LOADING, centerX, centerY - font.lineHeight, -1);
            graphics.centeredText(font, LoadingDotsText.get(Util.getMillis()), centerX, centerY, -1);
        } else if (future.isCompletedExceptionally()) {
            List<FormattedCharSequence> lines = font.split(Component.literal(EmoteLibraryException.reason(future.exceptionNow())), maxWidth);
            int startY = centerY - lines.size() * font.lineHeight / 2;
            for (int i = 0; i < lines.size(); i++) {
                graphics.centeredText(font, lines.get(i), centerX, startY + i * font.lineHeight, -1);
            }
        }
    }

    @Override
    protected void onRemoved() {
        this.widget.active = true;
        this.futures.clear();
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
