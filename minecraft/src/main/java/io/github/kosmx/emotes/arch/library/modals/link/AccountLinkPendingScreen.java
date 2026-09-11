package io.github.kosmx.emotes.arch.library.modals.link;

import io.github.kosmx.emotes.arch.library.EmoteLibrary;
import io.github.kosmx.emotes.arch.library.modals.BaseModalScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.redlance.emotecraftlibrary.sdk.EmoteLibraryException;
import org.redlance.emotecraftlibrary.sdk.GameLinkRequest;
import org.redlance.emotecraftlibrary.sdk.LinkListener;

import java.util.concurrent.CompletableFuture;

/** Waits for the website to approve an in-game account link opened in the browser. */
public class AccountLinkPendingScreen extends BaseModalScreen implements LinkListener {
    private static final Component WAITING = Component.translatable("emotecraft.library.link.waiting");

    private final GameLinkRequest link;
    private CompletableFuture<AutoCloseable> connection;
    private volatile boolean closed;
    private boolean approved;

    public AccountLinkPendingScreen(final @Nullable Screen backgroundScreen, final GameLinkRequest link) {
        super(AccountNotLinkedScreen.TITLE, backgroundScreen);
        this.link = link;
    }

    @Override
    public void added() {
        super.added();

        this.closed = false;
        this.connection = EmoteLibrary.executeAuthorized(client -> {
            AutoCloseable connection = client.openLinkConnection(this.link, this, this.screenExecutor);
            if (!this.closed) return connection;
            EmoteLibrary.close(connection);
            return null;
        });
        this.connection.whenCompleteAsync((_, th) -> {
            if (th != null) onRefused();
        }, this.screenExecutor);
    }

    @Override
    public void removed() {
        super.removed();
        this.closed = true;
        this.connection.thenAccept(EmoteLibrary::close);
        if (!this.approved) AccountNotLinkedScreen.cancelLink(this.link);
    }

    @Override
    protected LayoutElement addBody() {
        return createTextWidget(WAITING);
    }

    @Override
    protected void addButtons(GridLayout gridLayout) {
        GridLayout.RowHelper buttons = gridLayout.createRowHelper(2);

        buttons.addChild(Button.builder(CommonComponents.GUI_CANCEL, _ -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        buttons.addChild(Button.builder(CommonComponents.GUI_COPY_TO_CLIPBOARD, _ -> this.minecraft.keyboardHandler.setClipboard(this.link.getUrl()))
                .width(Button.SMALL_WIDTH)
                .build()
        );
    }

    @Override
    public void onApproved(String account) {
        this.approved = true;
        this.minecraft.gui.setScreen(new AccountLinkConfirmScreen(this.backgroundScreen, this.link, account));
    }

    @Override
    public void onRefused() {
        this.minecraft.gui.setScreen(new AccountLinkFailedScreen(this.backgroundScreen));
    }

    @Override
    public void onReset() {}

    @Override
    public void onError(EmoteLibraryException error, boolean fatal) {
        if (fatal) onRefused();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
