package io.github.kosmx.emotes.arch.screen.widget.preview.elemets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.authlib.GameProfile;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.widgets.PlayerPreview;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.IChooseElement;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.sounds.SoundManager;

import java.time.Duration;
import java.util.UUID;

public abstract class PlayerChooseElement extends PlayerPreview implements IChooseElement {
    protected final AbstractFastChooseWidget parent;
    protected final int id;

    public PlayerChooseElement(AbstractFastChooseWidget parent, GameProfile profile, int id) {
        super(profile, 0, 0, 0, 0, false);
        this.mannequin.emotecraft$getEmote().muteNbs = true;

        this.parent = parent;
        this.id = id;

        tick();
    }

    protected abstract void updateRectangle();

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean doHoverPart = this.parent.controller.doHoverPart(this);

        updateRectangle();
        if (isHoveredOrFocused() && doHoverPart) renderHover(guiGraphics);

        EmoteHolder emoteHolder = getEmote();
        /*Optional<ResourceLocation> icon = Optional.ofNullable(emoteHolder).map(EmoteHolder::getIconIdentifier);

        if (PlatformTools.getConfig().showIconsIfPossible.get() && icon.isPresent()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon.orElseThrow(), getX(), getY(), 0.0F, 0.0F, getWidth(), getHeight(), 256, 256, 256, 256);
        } else*/ {
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

    protected abstract void renderHover(GuiGraphics guiGraphics);

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
