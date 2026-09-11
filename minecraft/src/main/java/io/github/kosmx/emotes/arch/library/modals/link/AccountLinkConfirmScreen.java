package io.github.kosmx.emotes.arch.library.modals.link;

import io.github.kosmx.emotes.arch.library.EmoteLibrary;
import io.github.kosmx.emotes.arch.library.modals.BaseModalScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.redlance.emotecraftlibrary.sdk.GameLinkRequest;

/** Asks the player whether the website account that approved an in-game link is theirs. */
public class AccountLinkConfirmScreen extends BaseModalScreen {
    private static final Component LINK = Component.translatable("emotecraft.library.link.start");

    private final GameLinkRequest link;
    private final String account;
    private boolean confirming;

    public AccountLinkConfirmScreen(final @Nullable Screen backgroundScreen, final GameLinkRequest link, final String account) {
        super(AccountNotLinkedScreen.TITLE, backgroundScreen);
        this.link = link;
        this.account = account;
    }

    @Override
    protected LayoutElement addBody() {
        return createTextWidget(Component.translatable("emotecraft.library.link.confirm",
                this.minecraft.getUser().getName(), Component.literal(this.account).withStyle(ChatFormatting.BOLD)
        ));
    }

    @Override
    protected void addButtons(GridLayout gridLayout) {
        GridLayout.RowHelper buttons = gridLayout.createRowHelper(2);

        Button cancel = buttons.addChild(Button.builder(CommonComponents.GUI_CANCEL, _ -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        buttons.addChild(Button.builder(LINK, button -> {
            this.confirming = true;
            button.active = false;
            cancel.active = false;
            EmoteLibrary.executeAuthorized(client -> {
                client.confirmLink(this.link);
                return null;
            }).whenComplete((_, th) -> {
                if (th != null) AccountNotLinkedScreen.cancelLink(this.link);
            }).whenCompleteAsync((_, th) -> this.minecraft.gui.setScreen(th != null ? new AccountLinkFailedScreen(this.backgroundScreen) : this.backgroundScreen), this.screenExecutor);
        }).width(Button.SMALL_WIDTH).build());
    }

    @Override
    public void removed() {
        super.removed();
        if (!this.confirming) AccountNotLinkedScreen.cancelLink(this.link);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
