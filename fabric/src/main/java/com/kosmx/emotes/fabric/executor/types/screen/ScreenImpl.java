package com.kosmx.emotes.fabric.executor.types.screen;

import com.kosmx.emotes.executor.dataTypes.screen.IScreen;
import net.minecraft.client.gui.screen.Screen;

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
