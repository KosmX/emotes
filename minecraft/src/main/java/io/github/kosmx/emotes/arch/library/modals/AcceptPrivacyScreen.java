package io.github.kosmx.emotes.arch.library.modals;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.net.URI;

public class AcceptPrivacyScreen extends BaseModalScreen {
    private static final URI TERMS_URI = URI.create("https://emotes.redlance.org/terms");
    private static final URI PRIVACY_URI = URI.create("https://emotes.redlance.org/privacy");

    public static final Component TITLE = Component.translatable("emotecraft.library.privacy.title").withStyle(ChatFormatting.BOLD);
    private static final Component BODY = Component.translatable("emotecraft.library.privacy.description",
            link("mco.terms.sentence.2", TERMS_URI),
            link("emotecraft.library.privacy.privacy", PRIVACY_URI)
    );

    private final Runnable onAccept;

    public AcceptPrivacyScreen(final @Nullable Screen backgroundScreen, Runnable onAccept) {
        super(TITLE, backgroundScreen);
        this.onAccept = onAccept;
    }

    @Override
    protected LayoutElement addBody() {
        return createTextWidget(BODY);
    }

    @Override
    protected void addButtons(GridLayout gridLayout) {
        GridLayout.RowHelper buttons = gridLayout.createRowHelper(2);

        buttons.addChild(Button.builder(CommonComponents.GUI_BACK, _ -> onClose())
                .width(Button.SMALL_WIDTH)
                .build()
        );
        buttons.addChild(Button.builder(CommonComponents.GUI_PROCEED, _ -> onAccept.run())
                .width(Button.SMALL_WIDTH)
                .build()
        );
    }

}
