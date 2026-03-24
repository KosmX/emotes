package io.github.kosmx.emotes.arch.screen.utils;

import com.google.common.base.Stopwatch;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.EmotecraftClientMod;
import io.github.kosmx.emotes.common.CommonData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class EmoteListener extends PackSelectionScreen.Watcher {
    public static final Component RELOADING_WAIT = Component.translatable("emotecraft.reloading.wait");
    public static final Component RELOADING = Component.translatable("emotecraft.reloading");

    private static final DecimalFormat FORMAT = new DecimalFormat("#0.000");

    private CompletableFuture<?> loader;

    protected EmoteListener(Path path) throws IOException {
        super(path);
    }

    @Nullable
    public static EmoteListener create(Path packPath) {
        try {
            return new EmoteListener(packPath);
        } catch (IOException ex) {
            CommonData.LOGGER.warn("Failed to initialize emote dir monitoring", ex);
            return null;
        }
    }

    public void load(Runnable onComplete, @NotNull Executor executor) {
        if (this.loader != null) this.loader.cancel(true);
        PlatformTools.addToast(EmoteListener.RELOADING);

        Stopwatch stopwatch = Stopwatch.createStarted();
        this.loader = EmotecraftClientMod.loadEmotes()
                .thenRun(() -> PlatformTools.addToast(Component.translatable("emotecraft.reloading.done",
                        FORMAT.format((double) stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) / 1000D)
                )))
                .thenRunAsync(onComplete, Objects.requireNonNullElseGet(executor, Minecraft::getInstance));
    }

    public boolean isLoading() {
        return this.loader != null && !this.loader.isDone() && !this.loader.isCompletedExceptionally();
    }

    @Override
    public void close() throws IOException {
        super.close();

        if (this.loader != null) {
            this.loader.cancel(true);
            this.loader = null;
        }
    }

    public void blockWhileLoading() {
        if (this.loader != null && !this.loader.isDone() && !this.loader.isCompletedExceptionally()) {
            try {
                this.loader.get(10, TimeUnit.SECONDS);
            } catch (Throwable th) {
                CommonData.LOGGER.warn("Failed to wait for emote loading!", th);
                this.loader.cancel(true);
            }
        }
    }
}
