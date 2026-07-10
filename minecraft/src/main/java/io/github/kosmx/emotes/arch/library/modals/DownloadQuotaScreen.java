package io.github.kosmx.emotes.arch.library.modals;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.net.URI;

/** Shown when the server rejects a download because the hourly quota is spent. */
public class DownloadQuotaScreen extends BaseModalScreen {
    private static final URI SUPPORTER_URI = URI.create("https://emotes.redlance.org/supporter");

    public static final Component TITLE = Component.translatable("emotecraft.library.quota.title").withStyle(ChatFormatting.BOLD);
    private static final Component BODY = Component.translatable("emotecraft.library.quota.description");
    private static final Component UPGRADE = Component.translatable("emotecraft.library.quota.upgrade");

    public DownloadQuotaScreen(final @Nullable Screen backgroundScreen) {
        super(TITLE, backgroundScreen);
    }

    @Override
    protected LayoutElement addBody() {
        return new MultiLineTextWidget(BODY, this.font).setMaxWidth(240).setCentered(true);
    }

    @Override
    protected void addButtons(GridLayout gridLayout) {
        GridLayout.RowHelper buttons = gridLayout.createRowHelper(2);

        buttons.addChild(Button.builder(CommonComponents.GUI_OK, _ -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        buttons.addChild(Button.builder(UPGRADE,
                        _ -> ConfirmLinkScreen.confirmLinkNow(this, SUPPORTER_URI))
                .width(Button.SMALL_WIDTH)
                .build()
        );
    }
}
