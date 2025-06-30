package io.github.kosmx.emotes.arch.screen.utils;

import com.google.common.base.Stopwatch;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.arch.EmotecraftClientMod;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class EmoteListener extends PackSelectionScreen.Watcher {
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
            LoggerService.INSTANCE.log(Level.WARNING, "Failed to initialize emote dir monitoring", ex);
            return null;
        }
    }

    public void load(Runnable onComplete) {
        if (this.loader != null) {
            this.loader.cancel(true);
        }

        PlatformTools.addToast(Component.translatable("emotecraft.reloading"));

        Stopwatch stopwatch = Stopwatch.createStarted();
        this.loader = EmotecraftClientMod.loadEmotes()
                .thenRun(() -> PlatformTools.addToast(Component.translatable("emotecraft.reloading.done",
                        FORMAT.format((double) stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) / 1000D)
                )))
                .thenRun(onComplete);
    }

    public boolean isLoading() {
        return this.loader != null && !this.loader.isDone();
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
                LoggerService.INSTANCE.log(Level.WARNING, "Failed to wait for emote loading!", th);
                this.loader.cancel(true);
            }
        }
    }
}
