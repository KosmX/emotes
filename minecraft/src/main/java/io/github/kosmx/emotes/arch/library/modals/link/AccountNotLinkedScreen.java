package io.github.kosmx.emotes.arch.library.modals.link;

import io.github.kosmx.emotes.arch.library.EmoteLibrary;
import io.github.kosmx.emotes.arch.library.modals.BaseModalScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.redlance.emotecraftlibrary.sdk.GameLinkRequest;

import java.net.URI;

/** Shown when a request needs the player's game account linked to a library account, guiding them to do it. */
public class AccountNotLinkedScreen extends BaseModalScreen {
    protected static final URI WEBSITE_URI = URI.create("https://emotes.redlance.org");

    public static final Component TITLE = Component.translatable("emotecraft.library.link.title").withStyle(ChatFormatting.BOLD);
    private static final Component BODY = Component.translatable("emotecraft.library.link.description");
    private static final Component LINK = Component.translatable("emotecraft.library.link.start");

    private final @Nullable GameLinkRequest link;
    private boolean linking;

    public AccountNotLinkedScreen(final @Nullable Screen backgroundScreen, final @Nullable GameLinkRequest link) {
        super(TITLE, backgroundScreen);
        this.link = link;
    }

    @Override
    protected LayoutElement addBody() {
        return createTextWidget(BODY);
    }

    @Override
    protected void addButtons(GridLayout gridLayout) {
        GridLayout.RowHelper buttons = gridLayout.createRowHelper(2);

        buttons.addChild(Button.builder(CommonComponents.GUI_CANCEL, _ -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        buttons.addChild(Button.builder(LINK, _ -> {
            if (this.link == null) {
                ConfirmLinkScreen.confirmLinkNow(this, WEBSITE_URI);
            } else {
                this.linking = true;
                Util.getPlatform().openUri(this.link.getUrl());
                this.minecraft.gui.setScreen(new AccountLinkPendingScreen(this.backgroundScreen, this.link));
            }
        }).width(Button.SMALL_WIDTH).build());
    }

    @Override
    public void removed() {
        super.removed();
        if (this.link != null && !this.linking) cancelLink(this.link);
    }

    static void cancelLink(GameLinkRequest link) {
        EmoteLibrary.executeAuthorized(client -> {
            client.cancelLink(link);
            return null;
        });
    }
}
