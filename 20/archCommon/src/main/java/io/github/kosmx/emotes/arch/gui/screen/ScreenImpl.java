package io.github.kosmx.emotes.arch.gui.screen;

import io.github.kosmx.emotes.executor.dataTypes.screen.IScreen;
import net.minecraft.client.gui.screens.Screen;

public class ScreenImpl implements IScreen<Screen> {
    final Screen MCScreen;

    public ScreenImpl(Screen mcScreen) {
        MCScreen = mcScreen;
    }

    @Override
    public Screen getScreen() {
        return this.MCScreen;
    }
}
