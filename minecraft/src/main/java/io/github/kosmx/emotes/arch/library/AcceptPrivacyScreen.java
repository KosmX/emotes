package io.github.kosmx.emotes.arch.library;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.net.URI;

public class AcceptPrivacyScreen extends Screen {
    private static final URI TERMS_URI = URI.create("https://emotes.redlance.org/terms");
    private static final URI PRIVACY_URI = URI.create("https://emotes.redlance.org/privacy");
    private static final URI WEBSITE_URI = URI.create("https://emotes.redlance.org");

    public static final Component TITLE = Component.translatable("emotecraft.library.privacy.title")
            .withStyle(ChatFormatting.BOLD);
    private static final Component BODY = Component.empty()
            .append(Component.translatable("emotecraft.library.privacy.description",
                    link("emotecraft.library.privacy.terms", TERMS_URI),
                    link("emotecraft.library.privacy.privacy", PRIVACY_URI)
            ))
            .append("\n\n")
            .append(Component.translatable("emotecraft.library.privacy.setup",
                    link("emotecraft.library.privacy.website", WEBSITE_URI)
            ));

    private static Component link(String key, URI uri) {
        return Component.translatable(key).withStyle(style -> style
                .withUnderlined(true)
                .withColor(ChatFormatting.BLUE)
                .withClickEvent(new ClickEvent.OpenUrl(uri))
        );
    }

    private final @Nullable Screen backgroundScreen;
    private final Runnable onAccept;

    private @Nullable LinearLayout layout;

    public AcceptPrivacyScreen(final @Nullable Screen backgroundScreen, Runnable onAccept) {
        super(Component.empty());
        this.backgroundScreen = backgroundScreen;
        this.onAccept = onAccept;
    }

    @Override
    public void added() {
        super.added();
        if (this.backgroundScreen != null) {
            this.backgroundScreen.clearFocus();
        }
    }

    @Override
    protected void init() {
        if (this.backgroundScreen != null) {
            this.backgroundScreen.init(this.width, this.height);
        }

        this.layout = LinearLayout.vertical();

        this.layout.addChild(new MultiLineTextWidget(TITLE, this.font).setMaxWidth(240).setCentered(true),
                settings -> settings.alignHorizontallyCenter().padding(2, 2, 2, 4)
        );

        MultiLineTextWidget body = new MultiLineTextWidget(BODY, this.font).setMaxWidth(240).setCentered(true);
        body.setComponentClickHandler(style -> {
            if (style.getClickEvent() instanceof ClickEvent.OpenUrl(URI uri)) {
                ConfirmLinkScreen.confirmLinkNow(this, uri);
            }
        });
        this.layout.addChild(body,
                settings -> settings.alignHorizontallyCenter().padding(2, 2, 2, 5)
        );

        GridLayout gridLayout = this.layout.addChild(new GridLayout());
        gridLayout.defaultCellSetting().padding(2, 0, 2, 2);
        GridLayout.RowHelper buttons = gridLayout.createRowHelper(2);

        buttons.addChild(Button.builder(CommonComponents.GUI_BACK, _ -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        buttons.addChild(Button.builder(CommonComponents.GUI_PROCEED, _ -> onAccept.run())
                .width(Button.SMALL_WIDTH)
                .build()
        );

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.backgroundScreen != null) {
            this.backgroundScreen.resize(this.width, this.height);
        }

        this.layout.arrangeElements();
        FrameLayout.alignInRectangle(this.layout, this.getRectangle(), 0.5F, 0.5F);
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float a) {
        if (this.backgroundScreen != null) {
            this.backgroundScreen.extractBackground(graphics, mouseX, mouseY, a);
            graphics.nextStratum();
            this.backgroundScreen.extractRenderState(graphics, -1, -1, a);
            graphics.nextStratum();
            this.extractBlurredBackground(graphics);
        } else {
            super.extractBackground(graphics, mouseX, mouseY, a);
        }

        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                FriendsOverlayScreen.BACKGROUND_SPRITE,
                this.layout.getX() - 8,
                this.layout.getY() - 8,
                this.layout.getWidth() + 16,
                this.layout.getHeight() + 16 + 1
        );
    }

    @Override
    public boolean mouseClicked(final MouseButtonEvent event, final boolean doubleClick) {
        int panelLeft = this.layout.getX() - 8;
        int panelRight = this.layout.getX() + this.layout.getWidth() + 8;
        int panelTop = this.layout.getY();
        int panelBottom = this.layout.getY() + this.layout.getHeight() + 16;
        if (!(event.x() < panelLeft) && !(event.x() > panelRight) && !(event.y() < panelTop) && !(event.y() > panelBottom)) {
            return super.mouseClicked(event, doubleClick);
        }

        this.minecraft.gui.setScreen(this.backgroundScreen);
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.backgroundScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
