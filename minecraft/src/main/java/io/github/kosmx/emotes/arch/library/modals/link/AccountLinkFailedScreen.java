package io.github.kosmx.emotes.arch.library.modals.link;

import io.github.kosmx.emotes.arch.library.modals.BaseModalScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/** Shown when an in-game account link ends without linking. */
public class AccountLinkFailedScreen extends BaseModalScreen {
    private static final Component TITLE = Component.translatable("emotecraft.library.link.failed.title").withStyle(ChatFormatting.BOLD);
    private static final Component BODY = Component.translatable("emotecraft.library.link.failed");

    public AccountLinkFailedScreen(final @Nullable Screen backgroundScreen) {
        super(TITLE, backgroundScreen);
    }

    @Override
    protected LayoutElement addBody() {
        return createTextWidget(BODY);
    }

    @Override
    protected void addButtons(GridLayout gridLayout) {
        gridLayout.createRowHelper(1).addChild(Button.builder(CommonComponents.GUI_OK, _ -> onClose()).build());
    }
}
