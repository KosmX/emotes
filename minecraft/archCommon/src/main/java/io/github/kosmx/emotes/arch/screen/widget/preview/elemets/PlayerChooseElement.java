package io.github.kosmx.emotes.arch.screen.widget.preview.elemets;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.authlib.GameProfile;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.IChooseElement;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public abstract class PlayerChooseElement extends AbstractWidget /*PlayerPreview*/ implements IChooseElement {
    protected final AbstractFastChooseWidget parent;
    protected final int id;

    public PlayerChooseElement(AbstractFastChooseWidget parent, GameProfile profile, int id) {
        super(0, 0, 0, 0, CommonComponents.EMPTY);

        this.parent = parent;
        this.id = id;

        // super.pause(true);
        // super.tick();
    }

    protected abstract void updateRectangle();

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean doHoverPart = this.parent.controller.doHoverPart(this);

        updateRectangle();
        if (isHoveredOrFocused() && doHoverPart) renderHover(guiGraphics);

        EmoteHolder emoteHolder = getEmote();
        Optional<ResourceLocation> icon = Optional.ofNullable(emoteHolder).map(EmoteHolder::getIconIdentifier);

        /*if (PlatformTools.getConfig().showIconsIfPossible.get() && icon.isPresent()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon.orElseThrow(), getX(), getY(), 0.0F, 0.0F, getWidth(), getHeight(), 256, 256, 256, 256);
        } else {
            super.renderWidget(guiGraphics, getX() + (getWidth() / 2), getY() + (getHeight() / 2), partialTick);
        }*/

        if (icon.isPresent()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, icon.orElseThrow(), getX(), getY(), 0.0F, 0.0F, getWidth(), getHeight(), 256, 256, 256, 256);
        } else if (getEmote() != null) {
            renderScrollingString(guiGraphics, Minecraft.getInstance().font, getEmote().name, getX(), getY(), getRight(), getBottom(), -1);
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
        // this.player.stopEmote();
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

    /*@Override
    public void tick() {
        EmoteHolder holder = getEmote();
        boolean updated = playAnimation(holder == null ? null : holder.getEmote(), true, 1.5F);
        super.pause(!isHoveredOrFocused());
        if (updated || isHoveredOrFocused()) super.tick();
    }*/

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
        playButtonClickSound(handler);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
