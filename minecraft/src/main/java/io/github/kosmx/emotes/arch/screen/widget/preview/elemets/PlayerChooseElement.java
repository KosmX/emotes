package io.github.kosmx.emotes.arch.screen.widget.preview.elemets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.easing.EasingType;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.arch.gui.widgets.PlayerPreview;
import io.github.kosmx.emotes.arch.screen.utils.EmotecraftTexture;
import io.github.kosmx.emotes.arch.screen.utils.WidgetOutliner;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.IChooseElement;
import io.github.kosmx.emotes.arch.screen.widget.preview.PreviewFastChooseWidget;
import io.github.kosmx.emotes.main.EmoteHolder;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Vector2f;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

public abstract class PlayerChooseElement extends PlayerPreview implements IChooseElement {
    private static final Float2FloatFunction EASING_TRANSFORMER = EasingType.EASE_IN_CIRC.buildTransformer(null);

    protected final AbstractFastChooseWidget parent;
    protected final int id;

    private boolean isAnimFinishing;

    public PlayerChooseElement(AbstractFastChooseWidget parent, GameProfile profile, int id) {
        super(profile, 0, 0, 0, 0, false);
        this.mannequin.emotecraft$getEmote().muteNbs = true;

        this.parent = parent;
        this.id = id;

        tick();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, this.isAnimFinishing ? mouseX : 0, this.isAnimFinishing ? mouseY : 0, a);
    }

    protected abstract void updateRectangle(float progress);
    protected abstract void getDirectionVector(Vector2f out);

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        boolean doHoverPart = this.parent.controller.doHoverPart(this);

        float progress = this.parent instanceof PreviewFastChooseWidget widget ? widget.getAnimTime() : 0.0F;
        float easedProgress = 1.0F - EASING_TRANSFORMER.get(progress);
        this.isAnimFinishing = easedProgress > 0.9F;

        updateRectangle(easedProgress);
        extractBackground(graphics);
        if (isHoveredOrFocused() && doHoverPart) extractHover(graphics);

        EmoteHolder emoteHolder = getEmote();
        if (this.parent.controller.supportsKeyboardNavigation() && emoteHolder != null) extractTileId(graphics, easedProgress);

        if (emoteHolder != null && PlatformTools.getConfig().showIconsIfPossible.get() && emoteHolder.getIconIdentifier() != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, emoteHolder.getIconIdentifier(), getX(), getY(), 0.0F, 0.0F, getWidth(), getHeight(), 256, 256, 256, 256);
        } else if (emoteHolder != null || !PlatformTools.getConfig().showIconsIfPossible.get()) {
            super.extractWidgetRenderState(graphics, getX() + (getWidth() / 2), getY() + (getHeight() / 2), a);
        }

        if (isHoveredOrFocused() && emoteHolder != null) {
            setTooltip(Tooltip.create(emoteHolder.name));
            setTooltipDelay(Duration.ZERO);
        } else setTooltip(null);

        if (isHovered()) {
            graphics.requestCursor(isActive() && doHoverPart ? emoteHolder != null ? CursorTypes.POINTING_HAND : CursorTypes.RESIZE_ALL : CursorTypes.NOT_ALLOWED);
        }
    }

    protected void extractBackground(GuiGraphicsExtractor graphics) {
        Identifier texture = EmotecraftTexture.MENU_LIST_BACKGROUND.identifier(Minecraft.getInstance().level != null);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX() + 1, getY() + 1, getRight(), getBottom(), getWidth() - 2, getHeight() - 2, 32, 32);
        WidgetOutliner.extractOutline(graphics, this, -1);
    }

    protected void extractHover(GuiGraphicsExtractor graphics) {
        graphics.fill(getX(), getY(), getRight(), getBottom(), ARGB.color(128, 66, 66, 66));
    }

    protected void extractTileId(GuiGraphicsExtractor graphics, float easedProgress) {
        Vector2f dir = new Vector2f();
        getDirectionVector(dir);
        if (dir.x == 0f && dir.y == 0f) return;

        float inv = Mth.invSqrt(dir.x * dir.x + dir.y * dir.y);
        float nx = dir.x * inv, ny = dir.y * inv;

        float cx0 = getX() + getWidth() * 0.5f;
        float cy0 = getY() + getHeight() * 0.5f;

        float cx1 = parent.getX() + parent.getWidth() * 0.5f;
        float cy1 = parent.getY() + parent.getHeight() * 0.5f;

        float t = 1f - easedProgress;
        float cx = Mth.lerp(t, cx0, cx1);
        float cy = Mth.lerp(t, cy0, cy1);

        float halfW = getWidth() * 0.5f;
        float halfH = getHeight() * 0.5f;
        float edge = Math.min(halfW / Math.abs(nx), halfH / Math.abs(ny));
        float offset = edge + 14f + Math.min(getWidth(), getHeight()) * 0.25f * t;

        float px = cx + nx * offset;
        float py = cy + ny * offset;

        Font font = Minecraft.getInstance().font;
        String s = Integer.toString(this.id + 1);
        graphics.text(font, s, Math.round(px - font.width(s) / 2f), Math.round(py - font.lineHeight / 2f), -1);
    }

    @Override
    public void removed() {
        this.mannequin.stopEmote();
    }

    @Override
    public boolean hasEmote() {
        return PlatformTools.getConfig().fastMenuEmotes[parent.getCurrentPage()][id] != null;
    }

    @Override
    public EmoteHolder getEmote() {
        return PlatformTools.getConfig().fastMenuEmotes[parent.getCurrentPage()][id];
    }

    @Override
    public void clearEmote() {
        setEmote(null);
    }

    @Override
    public void setEmote(EmoteListWidget.EmoteLikeEntry emote) {
        int page = parent.getCurrentPage();
        if (emote == null) {
            PlatformTools.getConfig().fastMenuEmotes[page][id] = null;
            return;
        }
        // Snapshot the emote itself (a local one resolves instantly, a library one is fetched once) so the slot works offline.
        emote.getEmote().whenCompleteAsync((animation, th) -> {
            if (th == null) PlatformTools.getConfig().fastMenuEmotes[page][id] = new EmoteHolder(animation);
        }, Minecraft.getInstance());
    }

    @Override
    public void tick() {
        EmoteHolder holder = getEmote();

        float previewTick = holder == null ? 1.0F : calculatePreviewTick(holder.getEmote());
        boolean shouldPlayAgain = holder == null || holder.getEmote().loopType().shouldPlayAgain(null, holder.getEmote());
        boolean updated = playAnimation(holder == null ? null : holder.getEmote(), shouldPlayAgain ? Animation.LoopType.DEFAULT : Animation.LoopType.LOOP, true, previewTick);

        super.pause(!updated && !isHoveredOrFocused());
        if (updated || isHoveredOrFocused()) super.tick();
    }

    protected static float calculatePreviewTick(Animation animation) {
        ExtraAnimationData data = animation.data();

        if (data.has("previewTick")) {
            return (float) data.getRaw("previewTick");
        }

        if (animation.loopType().shouldPlayAgain(null, animation)) {
            float returnToTick = animation.loopType().restartFromTick(null, animation);
            if (returnToTick > 0.0F) {
                return (animation.length() - returnToTick) / 2F;
            }
        }

        return animation.length() / 2F;
    }

    @Override
    protected boolean isValidClickButton(@NonNull MouseButtonInfo button) {
        return this.parent.controller.isValidClickButton(button);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean bl) {
        if (super.mouseClicked(event, bl)) {
            return this.parent.controller.onClick(this, event, bl);
        }
        return false;
    }

    @Override
    public void playDownSound(@NonNull SoundManager handler) {
        if (!this.parent.controller.doHoverPart(this)) return;
        playButtonClickSound(handler);
    }
}
