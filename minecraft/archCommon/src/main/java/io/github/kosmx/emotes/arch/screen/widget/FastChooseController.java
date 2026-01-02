package io.github.kosmx.emotes.arch.screen.widget;

import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

public interface FastChooseController {
    boolean doHoverPart(IChooseElement part);
    boolean isValidClickButton(MouseButtonInfo info);
    boolean onClick(IChooseElement element, InputWithModifiers event, boolean bl);
    boolean doesShowInvalid();
    boolean supportsKeyboardNavigation();
}
