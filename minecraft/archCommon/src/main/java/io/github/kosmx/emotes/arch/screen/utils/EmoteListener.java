package io.github.kosmx.emotes.arch.screen.utils;

import com.google.common.base.Stopwatch;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.main.MainClientInit;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class EmoteListener implements Closeable {
    private WatchService watcher;
    private CompletableFuture<?> loader;

    public EmoteListener(Path path) {
        try {
            this.watcher = path.getFileSystem().newWatchService();

            path.register(this.watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY
            );
        } catch (Throwable th) {
            LoggerService.INSTANCE.log(Level.WARNING, "Failed to start file watcher!", th);
        }
    }

    public void load(Runnable onComplete) {
        if (this.loader != null) {
            this.loader.cancel(true);
        }

        PlatformTools.addToast(Component.translatable("emotecraft.reloading"));

        this.loader = CompletableFuture.runAsync(() -> {
            Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                MainClientInit.loadEmotes();

                PlatformTools.addToast(Component.translatable("emotecraft.reloading.done",
                        stopwatch.stop().elapsed(TimeUnit.SECONDS)
                ));
            } catch (Throwable th) {
                LoggerService.INSTANCE.log(Level.WARNING, "Failed to reload emotes!", th);
            }
        }, Util.ioPool()).thenRun(onComplete);
    }

    public boolean isLoading() {
        return this.loader != null && !this.loader.isDone();
    }

    public boolean isFilesChanged() {
        if (isLoading()) {
            return false;
        }

        boolean bl = false;
        WatchKey key;
        if(watcher != null && (key = watcher.poll()) != null){
            bl = !key.pollEvents().isEmpty();//there is something...
            key.reset();
        }
        return bl;
    }

    @Override
    public void close() throws IOException {
        if (this.loader != null) {
            this.loader.cancel(true);
            this.loader = null;
        }

        if (this.watcher != null) {
            this.watcher.close();
            this.watcher = null;
        }
    }

    public boolean isWatcherClosed() {
        return this.watcher == null;
    }

    public void blockWhileLoading() {
        if (this.loader != null && !this.loader.isDone() && !this.loader.isCompletedExceptionally()) {
            this.loader.join();
        }
    }
}
