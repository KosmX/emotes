package io.github.kosmx.emotes.arch.screen.widget;

public interface FastChooseController {
    boolean doHoverPart(IChooseElement part);
    boolean isValidClickButton(int button);
    boolean onClick(IChooseElement element, int button);
    boolean doesShowInvalid();
}
