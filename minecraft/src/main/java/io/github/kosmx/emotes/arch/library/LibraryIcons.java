package io.github.kosmx.emotes.arch.library;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import io.github.kosmx.emotes.arch.library.modals.ShareScreen;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.mc.McUtils;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.redlance.emotecraftlibrary.sdk.GameLibraryItem;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class LibraryIcons {
    private static final int MAX_TEXTURES = 64; // A few screens of rows; one drawn again after eviction loads from the disk cache
    private static final int SIZE = 32;
    private static final int TEXTURE_SIZE = 256;

    private final Cache<String, CompletableFuture<ClientAsset.Texture>> textures = CacheBuilder.newBuilder()
            .maximumSize(MAX_TEXTURES)
            .concurrencyLevel(1) // One segment, so the whole size is shared: everything runs on the render thread
            .removalListener(LibraryIcons::releaseTexture)
            .build();

    /** @return whether it was drawn */
    boolean extract(GuiGraphicsExtractor graphics, GameLibraryItem item, int x, int y) {
        Identifier texture = texture(item);
        if (texture == null) {
            return false;
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, SIZE, SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
        return true;
    }

    ShareScreen.Icon icon(GameLibraryItem item, int x, int y) {
        return new ShareScreen.Icon(new ScreenRectangle(x, y, SIZE, SIZE), texture(item), TEXTURE_SIZE);
    }

    private @Nullable Identifier texture(GameLibraryItem item) {
        String hash = item.getIconHash();
        String url = item.getIconUrl();
        if (hash == null || url == null) {
            return null; // The cache goes by hash
        }

        CompletableFuture<ClientAsset.Texture> texture = this.textures.asMap().computeIfAbsent(hash, _ -> download(hash, url));
        return texture.isDone() && !texture.isCompletedExceptionally() ? texture.join().texturePath() : null;
    }

    void release() {
        this.textures.invalidateAll();
    }

    private static void releaseTexture(RemovalNotification<String, CompletableFuture<ClientAsset.Texture>> removal) {
        // A texture still loading is released once registered (both run on the render thread)
        removal.getValue().thenAccept(registered -> Minecraft.getInstance().getTextureManager().release(registered.texturePath()));
    }

    private static CompletableFuture<ClientAsset.Texture> download(String hash, String url) {
        // A fresh id per download, so a release still pending for an earlier one can't take this one down
        Identifier id = McUtils.newIdentifier("libraryicon/" + UUID.randomUUID());
        return Minecraft.getInstance().getSkinManager().skinTextureDownloader.downloadAndRegisterSkin(
                id, InstanceService.INSTANCE.getCacheDirectory().resolve("libraryicon/" + hash), url, false
        ).whenComplete((_, throwable) -> {
            if (throwable != null) CommonData.LOGGER.warn("Failed to load library icon {}!", url, throwable); // Kept failed: not retried every frame
        });
    }
}
