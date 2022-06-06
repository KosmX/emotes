package io.github.kosmx.emotes.arch.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kosmx.emotes.main.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.main.screen.ExportMenu;
import io.github.kosmx.emotes.main.screen.IScreenSlave;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ExportMenuScreenImpl extends AbstractControlledModScreen {

    public ExportMenuScreenImpl(Screen parent) {
        super(Component.translatable("emotecraft.exportMenu"), parent);
    }

    @Override
    protected AbstractScreenLogic<PoseStack, Screen> newMaster() {
        return new MasterImpl(this);
    }

    private static class MasterImpl extends ExportMenu<PoseStack, Screen> implements IScreenHelperImpl {
        protected MasterImpl(IScreenSlave screen) {
            super(screen);
        }
    }
}
