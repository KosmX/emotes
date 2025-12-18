package io.github.kosmx.emotes.arch.screen.widget.preview.elemets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.easing.EasingType;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.PlayerPreview;
import io.github.kosmx.emotes.arch.screen.utils.EmotecraftTexture;
import io.github.kosmx.emotes.arch.screen.utils.WidgetOutliner;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.IChooseElement;
import io.github.kosmx.emotes.arch.screen.widget.preview.PreviewFastChooseWidget;
import io.github.kosmx.emotes.main.EmoteHolder;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.time.Duration;
import java.util.UUID;

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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, this.isAnimFinishing ? mouseX : 0, this.isAnimFinishing ? mouseY : 0, partialTick);
    }

    protected abstract void updateRectangle(float progress);

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean doHoverPart = this.parent.controller.doHoverPart(this);

        float progress = this.parent instanceof PreviewFastChooseWidget widget ? widget.getAnimTime() : 0.0F;
        float easedProgress = 1.0F - EASING_TRANSFORMER.get(progress);
        this.isAnimFinishing = easedProgress > 0.9F;

        updateRectangle(easedProgress);
        renderBackground(guiGraphics);
        if (isHoveredOrFocused() && doHoverPart) renderHover(guiGraphics);

        EmoteHolder emoteHolder = getEmote();

        if (emoteHolder != null && PlatformTools.getConfig().showIconsIfPossible.get() && emoteHolder.getIconIdentifier() != null) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, emoteHolder.getIconIdentifier(), getX(), getY(), 0.0F, 0.0F, getWidth(), getHeight(), 256, 256, 256, 256);
        } else if (emoteHolder != null || !PlatformTools.getConfig().showIconsIfPossible.get()) {
            super.renderWidget(guiGraphics, getX() + (getWidth() / 2), getY() + (getHeight() / 2), partialTick);
        }

        if (isHoveredOrFocused() && emoteHolder != null) {
            setTooltip(Tooltip.create(emoteHolder.name));
            setTooltipDelay(Duration.ZERO);
        } else setTooltip(null);

        if (isHovered()) {
            guiGraphics.requestCursor(isActive() && doHoverPart ? emoteHolder != null ? CursorTypes.POINTING_HAND : CursorTypes.RESIZE_ALL : CursorTypes.NOT_ALLOWED);
        }
    }

    protected void renderBackground(GuiGraphics guiGraphics) {
        Identifier texture = EmotecraftTexture.MENU_LIST_BACKGROUND.identifier(Minecraft.getInstance().level != null);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, getX() + 1, getY() + 1, getRight(), getBottom(), getWidth() - 2, getHeight() - 2, 32, 32);
        WidgetOutliner.renderOutline(guiGraphics, this, -1);
    }

    protected void renderHover(GuiGraphics guiGraphics) {
        guiGraphics.fill(getX(), getY(), getRight(), getBottom(), ARGB.color(128, 66, 66, 66));
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
        UUID uuid = PlatformTools.getConfig().fastMenuEmotes[parent.getCurrentPage()][id];
        if (uuid != null) {
            EmoteHolder emote = EmoteHolder.list.get(uuid);
            if (emote == null && this.parent.controller.doesShowInvalid()) {
                emote = new EmoteHolder.Empty(uuid);
            }
            return emote;
        } else {
            return null;
        }
    }

    @Override
    public void clearEmote() {
        setEmote(null);
    }

    @Override
    public void setEmote(EmoteHolder emote) {
        PlatformTools.getConfig().fastMenuEmotes[parent.getCurrentPage()][id] = emote == null ? null : emote.getUuid();
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
    protected boolean isValidClickButton(MouseButtonInfo button) {
        return this.parent.controller.isValidClickButton(button);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        if (super.mouseClicked(event, bl)) {
            return this.parent.controller.onClick(this, event, bl);
        }
        return false;
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if (!this.parent.controller.doHoverPart(this)) return;
        playButtonClickSound(handler);
    }
}
