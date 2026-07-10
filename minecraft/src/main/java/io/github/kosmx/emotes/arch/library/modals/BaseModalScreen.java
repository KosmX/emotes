package io.github.kosmx.emotes.arch.library.modals;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.net.URI;

public abstract class BaseModalScreen extends Screen {
    private final @Nullable Screen backgroundScreen;
    private @Nullable LinearLayout layout;

    protected BaseModalScreen(Component title, @Nullable Screen backgroundScreen) {
        super(title);
        this.backgroundScreen = backgroundScreen;
    }

    @Override
    public void added() {
        super.added();
        if (this.backgroundScreen != null) {
            this.backgroundScreen.clearFocus();
        }
    }

    protected abstract LayoutElement addBody();

    @Override
    protected void init() {
        if (this.backgroundScreen != null) {
            this.backgroundScreen.init(this.width, this.height);
        }

        this.layout = LinearLayout.vertical();

        this.layout.addChild(new MultiLineTextWidget(this.title, this.font).setMaxWidth(240).setCentered(true),
                settings -> settings.alignHorizontallyCenter().padding(2, 2, 2, 4)
        );

        this.layout.addChild(addBody(),
                settings -> settings.alignHorizontallyCenter().padding(2, 2, 2, 5)
        );

        GridLayout gridLayout = this.layout.addChild(new GridLayout());
        gridLayout.defaultCellSetting().padding(2, 0, 2, 2);
        addButtons(gridLayout);

        this.layout.visitWidgets(this::addRenderableWidget);
        this.repositionElements();
    }

    protected abstract void addButtons(GridLayout gridLayout);

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

        onClose();
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

    /** @return whether this privacy screen is the one currently displayed, so a background screen can skip blurring under it. */
    public static boolean isShowing(Minecraft minecraft) {
        return minecraft.gui.screen() instanceof BaseModalScreen;
    }

    protected static Component link(String key, URI uri) {
        return Component.translatable(key).withStyle(style -> style
                .withUnderlined(true)
                .withColor(ChatFormatting.BLUE)
                .withClickEvent(new ClickEvent.OpenUrl(uri))
        );
    }
}
