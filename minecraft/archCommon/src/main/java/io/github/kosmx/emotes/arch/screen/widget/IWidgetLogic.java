package io.github.kosmx.emotes.arch.screen.widget;

import io.github.kosmx.emotes.inline.dataTypes.screen.widgets.IWidget;
import io.github.kosmx.emotes.arch.screen.IRenderHelper;

public interface IWidgetLogic extends IRenderHelper, IWidget {
    default boolean emotes_mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean emotes_mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }
}
