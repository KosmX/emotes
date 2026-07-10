package io.github.kosmx.emotes.arch.library.modals;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.net.URI;

/** Shown when a request needs the player's game account linked to a library account, guiding them to do it. */
public class AccountNotLinkedScreen extends BaseModalScreen {
    protected static final URI WEBSITE_URI = URI.create("https://emotes.redlance.org");

    public static final Component TITLE = Component.translatable("emotecraft.library.link.title").withStyle(ChatFormatting.BOLD);
    private static final Component BODY = Component.translatable("emotecraft.library.link.description",
            link("known_server_link.website", WEBSITE_URI)
    );
    private static final Component WEBSITE = Component.translatable("known_server_link.website");

    public AccountNotLinkedScreen(final @Nullable Screen backgroundScreen) {
        super(TITLE, backgroundScreen);
    }

    @Override
    protected LayoutElement addBody() {
        MultiLineTextWidget body = new MultiLineTextWidget(BODY, this.font).setMaxWidth(240).setCentered(true);
        body.setComponentClickHandler(style -> {
            if (style.getClickEvent() instanceof ClickEvent.OpenUrl(URI uri)) {
                ConfirmLinkScreen.confirmLinkNow(this, uri);
            }
        });
        body.active = true; // MultiLineTextWidget starts inactive; without this mouseClicked bails and links don't fire
        return body;
    }

    @Override
    protected void addButtons(GridLayout gridLayout) {
        GridLayout.RowHelper buttons = gridLayout.createRowHelper(2);

        buttons.addChild(Button.builder(CommonComponents.GUI_OK, _ -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        buttons.addChild(Button.builder(WEBSITE,
                        _ -> ConfirmLinkScreen.confirmLinkNow(this, WEBSITE_URI))
                .width(Button.SMALL_WIDTH)
                .build()
        );
    }
}
