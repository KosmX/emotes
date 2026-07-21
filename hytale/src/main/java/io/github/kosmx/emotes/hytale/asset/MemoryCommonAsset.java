package io.github.kosmx.emotes.hytale.asset;

import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;

/**
 * A {@link CommonAsset} backed purely by bytes we generated at runtime, with no file or classpath resource behind it.
 * <p>
 * {@link CommonAsset} keeps the blob in a {@link WeakReference}, so it may drop the cached future at any
 * time and call {@link #getBlob0()} again — hence the hard reference kept here.
 */
public final class MemoryCommonAsset extends CommonAsset {
    private final byte[] data;

    public MemoryCommonAsset(@NotNull String name, @NotNull byte[] data) {
        super(name, data); // hashes the bytes for us
        this.data = data;
    }

    @Override
    protected CompletableFuture<byte[]> getBlob0() {
        return CompletableFuture.completedFuture(this.data);
    }
}
