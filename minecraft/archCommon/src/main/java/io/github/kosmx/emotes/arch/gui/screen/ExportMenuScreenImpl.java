package io.github.kosmx.emotes.arch.gui.screen;

import io.github.kosmx.emotes.arch.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.arch.screen.ExportMenu;
import io.github.kosmx.emotes.arch.screen.IScreenSlave;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ExportMenuScreenImpl extends AbstractControlledModScreen {

    public ExportMenuScreenImpl(Screen parent) {
        super(Component.translatable("emotecraft.exportMenu"), parent);
    }

    @Override
    protected AbstractScreenLogic<GuiGraphics, Screen> newMaster() {
        return new MasterImpl(this);
    }

    private static class MasterImpl extends ExportMenu<GuiGraphics, Screen> implements IScreenHelperImpl {
        protected MasterImpl(IScreenSlave screen) {
            super(screen);
        }
    }
}
