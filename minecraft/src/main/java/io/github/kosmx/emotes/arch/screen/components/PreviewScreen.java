package io.github.kosmx.emotes.arch.screen.components;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.arch.gui.widgets.PlayerPreview;
import io.github.kosmx.emotes.common.CommonData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Header and footer screen with a {@link PlayerPreview} placed left of its contents.
 */
public abstract class PreviewScreen extends Screen {
    protected static final int MAX_PREVIEW_SIZE = 256; // In gui units, so the preview follows the gui scale instead of the window

    protected final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    protected Screen lastScreen;

    @Nullable
    protected PlayerPreview preview;
    @Nullable
    private Object previewed; // source currently loaded into the preview — avoid re-fetching it every tick

    protected PreviewScreen(Component title, Screen lastScreen) {
        super(title);
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        this.layout.removeChildren(); // The frames accumulate otherwise; widgets below are reused across resizes.
        this.addTitle();
        this.addPlayerPreview();
        this.addContents();
        this.addFooter();
        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected abstract void addTitle();

    protected void addPlayerPreview() {
        if (this.preview == null) {
            this.preview = new PlayerPreview(this.minecraft.getGameProfile(), 0, 0, 0, 0, true);
        }
        this.addRenderableWidget(this.preview); // Not in the layout, it is placed next to the contents once they are arranged
    }

    protected abstract void addContents();

    protected abstract void addFooter();

    /** Centers the preview, both ways, in the content area left of {@code contentsLeft}. Call once the layout is arranged. */
    protected void repositionPreview(int contentsLeft) {
        if (this.preview == null) return;

        int previewHeight = Math.min(this.height / 2, MAX_PREVIEW_SIZE);
        int space = contentsLeft - Button.DEFAULT_SPACING * 2;
        int previewWidth = Math.clamp(space, 0, previewHeight);

        this.preview.visible = space >= previewHeight / 3; // For small screens
        this.preview.setSize(previewWidth, previewHeight);
        this.preview.setPosition(
                Button.DEFAULT_SPACING + (space - previewWidth) / 2,
                this.layout.getHeaderHeight() + (this.layout.getContentHeight() - previewHeight) / 2
        );
    }

    /** Plays the emote of {@code source} in the preview, loading it only when the source changes. */
    protected void previewEmote(Object source, Supplier<CompletableFuture<Animation>> emote) {
        PlayerPreview preview = this.preview;
        if (preview == null || source == this.previewed) return;

        this.previewed = source;
        emote.get().whenCompleteAsync((animation, throwable) -> {
            if (this.previewed != source) return; // moved to another source before it finished loading
            if (throwable != null) {
                // Passive preview — the source shows the error itself; no modal here.
                CommonData.LOGGER.error("Failed to load emote!", throwable);
                return;
            }
            // The finished emote stays loaded in the player, so only skip it while it is still playing
            preview.playAnimation(animation, Animation.LoopType.DEFAULT, preview.getMannequin().isPlayingEmote());
        }, this.minecraft);
    }

    /** Forgets the previewed source, so it is loaded again next time. */
    protected void clearPreviewed(boolean stopEmote) {
        this.previewed = null;
        if (stopEmote && this.preview != null) this.preview.getMannequin().stopEmote();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.preview != null) this.preview.tick();
    }

    @Override
    public void removed() {
        super.removed();
        clearPreviewed(true); // Coming back does not init again, so the hovered emote has to be loaded again
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.lastScreen);
    }
}
