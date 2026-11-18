package io.github.kosmx.emotes.arch.library.modals;

import com.mojang.blaze3d.Blaze3D;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.animation.Animatable;
import io.github.kosmx.emotes.arch.gui.animation.HoverFill;
import io.github.kosmx.emotes.arch.gui.animation.Timeline;
import io.github.kosmx.emotes.arch.gui.animation.Tween;
import io.github.kosmx.emotes.arch.gui.widgets.EmoteListWidget;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Ease;
import net.minecraft.util.EasingType;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URI;

/** Port of the library app's share window, which grows out of the shared item as the Mavericks AirDrop window does. */
public final class ShareScreen extends BaseModalScreen {
    public static final Component SHARE = Component.translatable("emotecraft.share");
    private static final Component TITLE = SHARE.copy().withStyle(ChatFormatting.BOLD);
    private static final Component LINK_COPIED = Component.translatable("emotecraft.share.copied");
    private static final Component SEND_TO_CHAT = Component.translatable("emotecraft.share.chat");
    private static final Component SENT_TO_CHAT = Component.translatable("emotecraft.share.sent");
    private static final Component MESSAGES_NOT_ALLOWED = Component.translatable("chat_screen.messages_not_allowed");
    private static final WidgetSprites LINK_SPRITES = new WidgetSprites(Identifier.withDefaultNamespace("icon/link"), Identifier.withDefaultNamespace("icon/link_highlighted"));
    private static final Identifier CLIP_BACK = McUtils.newIdentifier("textures/paperclip_back.png");
    private static final Identifier CLIP_FRONT = McUtils.newIdentifier("textures/paperclip_front.png");

    private static final int WIDTH = 2 * Button.SMALL_WIDTH + 4; // The two buttons and the gap between them
    // The library's dp, at a vanilla button's 20 px for Material's 44 dp
    private static final int PICTURE_SIZE = 44;
    private static final int PICTURE_INSET = 8;
    private static final int PICTURE_OVERHANG = 5;
    private static final int LINK_PADDING = 7;
    private static final int LINK_ICON_SIZE = 15;
    private static final int CLIP_X = -35; // The paperclip's texture meets the window's edge 35 px in
    private static final int CLIP_Y = 11;
    private static final int CLIP_WIDTH = 40;
    private static final int CLIP_HEIGHT = 20;
    private static final int CLIP_TEXTURE_SCALE = 4;
    private static final float POINT = CLIP_WIDTH / 76.0F; // GUI px per OS X point: its paperclip is 76 pt wide

    // As OS X 10.9's ShareKit for an item shared by its icon: the window grows out of the icon in 0.66 s as the icon flies in
    private static final float OPENING = 13.2F;
    private static final Tween GROWTH = new Tween(OPENING, EasingType.OUT_CUBIC);
    private static final Tween FLIGHT = new Tween(OPENING, Mth::smoothstep);
    // The paperclip slides in from 600 pt past its spot and shows only in the second half: QuickLook fades it from alpha -1
    private static final float CLIP_ARRIVAL_DISTANCE = CLIP_WIDTH + 600.0F * POINT;
    private static final EasingType SOFT_EASE_OUT = x -> x * (1.25F + x * x * (2.5F + x * (2.25F * x - 5.0F))); // ShareKit's SHKSoftEaseOutFunction
    private static final Tween CLIP_ARRIVAL = new Tween(OPENING, SOFT_EASE_OUT);
    private static final Tween CLIP_FADE = new Tween(OPENING, x -> Math.max(0.0F, 2.0F * Ease.inOutSine(x) - 1.0F));
    // After an action the window flies up off the screen; cancelled, it fades out
    private static final Tween DEPARTURE = new Tween(4.0F, EasingType.IN_OUT_SINE);
    private static final Tween DISMISSAL = new Tween(2.0F, EasingType.IN_OUT_SINE);

    private final Component name;
    private final Component author;
    private final URI link;
    private final Icon icon;
    private final Timeline timeline = new Timeline();
    private final Animatable departure = new Animatable(0.0F, DEPARTURE);
    private final Animatable dismissal = new Animatable(0.0F, DISMISSAL);

    public ShareScreen(@Nullable Screen backgroundScreen, Component name, Component author, URI link, Icon icon) {
        super(TITLE, backgroundScreen);
        this.name = name;
        this.author = author;
        this.link = link;
        this.icon = icon;
    }

    @Override
    protected LayoutElement addBody() {
        LinearLayout body = LinearLayout.vertical();
        Component item = this.author.getString().isEmpty() ? this.name : CommonComponents.joinLines(this.name, EmoteListWidget.authorLine(this.author));
        body.addChild(createTextWidget(item), LayoutSettings::alignHorizontallyCenter);
        body.addChild(new LinkButton());
        return body;
    }

    @Override
    protected void addButtons(GridLayout gridLayout) {
        GridLayout.RowHelper buttons = gridLayout.createRowHelper(2);
        LocalPlayer player = this.minecraft.player;
        if (player != null) {
            Button chat = Button.builder(SEND_TO_CHAT, _ -> sendToChat(player)).width(WIDTH).build();
            if (!player.chatAbilities().canSendMessages()) {
                chat.active = false;
                chat.setTooltip(Tooltip.create(MESSAGES_NOT_ALLOWED));
            }
            buttons.addChild(chat, 2);
        }
        buttons.addChild(Button.builder(CommonComponents.GUI_CANCEL, _ -> onClose()).width(Button.SMALL_WIDTH).build());
        buttons.addChild(Button.builder(CommonComponents.GUI_COPY_LINK_TO_CLIPBOARD, _ -> copyLink()).width(Button.SMALL_WIDTH).build());
    }

    private void sendToChat(LocalPlayer player) {
        String item = this.author.getString().isEmpty() ? this.name.getString() : Component.translatable("emotecraft.share.text", this.name, this.author).getString();
        item = StringUtil.filterText(StringUtils.normalizeSpace(ChatFormatting.stripFormatting(item))); // The server kicks for '§' and control characters
        String link = this.link.toString();
        player.connection.sendChat(StringUtil.truncateStringIfNecessary(item, SharedConstants.MAX_CHAT_LENGTH - 1 - link.length(), true) + " " + link);
        PlatformTools.addToast(SENT_TO_CHAT);
        depart();
    }

    private void copyLink() {
        this.minecraft.keyboardHandler.setClipboard(this.link.toString());
        PlatformTools.addToast(LINK_COPIED);
        depart();
    }

    private void openLink() {
        Blaze3D.openUri(this.link);
        depart();
    }

    private void depart() {
        this.departure.animateTo(1.0F);
    }

    @Override
    public void onClose() {
        this.dismissal.animateTo(1.0F);
    }

    @Override
    public void tick() {
        if (this.departure.isAt(1.0F) || this.dismissal.isAt(1.0F)) super.onClose();
    }

    @Override
    public float blurProgress() {
        return GROWTH.at(this.timeline.elapsedTicks()) * (1.0F - this.departure.get()) * opacity();
    }

    @Override
    protected float opacity() {
        return 1.0F - this.dismissal.get();
    }

    private boolean isLeaving() {
        return !this.departure.isAt(0.0F) || !this.dismissal.isAt(0.0F);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        if (isLeaving() || isOverAttachment((int) event.x(), (int) event.y())) return true;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        return isLeaving() || super.keyPressed(event);
    }

    private boolean isOverAttachment(int x, int y) {
        ScreenRectangle window = window();
        return this.icon.texture() != null && (new ScreenRectangle(pictureX(), pictureY(), PICTURE_SIZE, PICTURE_SIZE).containsPoint(x, y)
                || new ScreenRectangle(window.right() + CLIP_X, window.top() + CLIP_Y, CLIP_WIDTH, CLIP_HEIGHT).containsPoint(x, y));
    }

    private int pictureX() {
        return window().right() - PICTURE_INSET - PICTURE_SIZE;
    }

    private int pictureY() {
        return window().top() - PICTURE_OVERHANG;
    }

    @Override
    protected void extractWindow(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        float elapsed = this.timeline.elapsedTicks();
        ScreenRectangle window = window();
        Identifier picture = this.icon.texture();
        Matrix3x2fStack pose = graphics.pose();

        graphics.enableScissor(0, 0, this.width, this.height); // Scrolling text clips only to an open scissor, and the renderer rejects one above the screen
        pose.pushMatrix();
        pose.translate(0.0F, -window.bottom() * this.departure.get()); // Until its bottom passes the screen's top
        if (picture != null) extractClip(graphics, CLIP_BACK, window, elapsed);

        pose.pushMatrix();
        zoomFrom(pose, this.icon.bounds(), window.left(), window.top(), window.width(), window.height(), 1.0F - GROWTH.at(elapsed));
        super.extractWindow(graphics, mouseX, mouseY, a);
        pose.popMatrix();

        if (picture != null) {
            int size = this.icon.textureSize();
            pose.pushMatrix();
            zoomFrom(pose, this.icon.bounds(), pictureX(), pictureY(), PICTURE_SIZE, PICTURE_SIZE, 1.0F - FLIGHT.at(elapsed));
            graphics.blit(RenderPipelines.GUI_TEXTURED, picture, pictureX(), pictureY(), 0.0F, 0.0F, PICTURE_SIZE, PICTURE_SIZE, size, size, size, size, ARGB.white(opacity()));
            pose.popMatrix();
            extractClip(graphics, CLIP_FRONT, window, elapsed);
        }
        pose.popMatrix();
        graphics.disableScissor();
    }

    /** One layer of the paperclip, which slides in from beyond the window's edge. */
    private void extractClip(GuiGraphicsExtractor graphics, Identifier layer, ScreenRectangle window, float elapsed) {
        float alpha = CLIP_FADE.at(elapsed) * opacity();
        if (alpha == 0.0F) return;

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(CLIP_ARRIVAL_DISTANCE * (1.0F - CLIP_ARRIVAL.at(elapsed)), 0.0F);
        graphics.blit(RenderPipelines.GUI_TEXTURED, layer, window.right() + CLIP_X, window.top() + CLIP_Y, 0.0F, 0.0F, CLIP_WIDTH, CLIP_HEIGHT,
                CLIP_WIDTH * CLIP_TEXTURE_SCALE, CLIP_HEIGHT * CLIP_TEXTURE_SCALE, CLIP_WIDTH * CLIP_TEXTURE_SCALE, CLIP_HEIGHT * CLIP_TEXTURE_SCALE, ARGB.white(alpha)
        );
        pose.popMatrix();
    }

    /** Draws what lies at the given bounds as if {@code remaining} of the way back to {@code from}, scaled to its width. */
    private static void zoomFrom(Matrix3x2fStack pose, ScreenRectangle from, int x, int y, int width, int height, float remaining) {
        float centerX = x + width / 2.0F;
        float centerY = y + height / 2.0F;
        pose.translate((from.left() + from.width() / 2.0F - centerX) * remaining, (from.top() + from.height() / 2.0F - centerY) * remaining);
        pose.scaleAround(1.0F - (1.0F - (float) from.width() / width) * remaining, centerX, centerY);
    }

    public record Icon(ScreenRectangle bounds, @Nullable Identifier texture, int textureSize) {}

    private final class LinkButton extends AbstractButton {
        private final HoverFill hover = new HoverFill(HoverFill.ROW);
        private final FormattedCharSequence address;

        private LinkButton() {
            super(0, 0, WIDTH, Button.DEFAULT_HEIGHT, Component.literal(link.toString()));
            int textWidth = WIDTH - LINK_ICON_SIZE - 3 * LINK_PADDING;
            this.address = font.width(getMessage()) > textWidth ? ComponentRenderUtils.clipText(getMessage(), font, textWidth) : getMessage().getVisualOrderText();
        }

        @Override
        public void onPress(@NonNull InputWithModifiers input) {
            openLink();
        }

        @Override
        protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
            boolean highlighted = isHoveredOrFocused();
            this.hover.extract(graphics, this, highlighted, this.alpha);
            int left = getX() + LINK_PADDING;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LINK_SPRITES.get(true, highlighted), left, getY() + (getHeight() - LINK_ICON_SIZE) / 2, LINK_ICON_SIZE, LINK_ICON_SIZE, this.alpha);
            left += LINK_ICON_SIZE + LINK_PADDING;
            graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE)
                    .accept(left, getY() + (getHeight() - font.lineHeight) / 2, this.address);
        }

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
