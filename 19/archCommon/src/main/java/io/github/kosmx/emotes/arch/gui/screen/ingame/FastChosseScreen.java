package io.github.kosmx.emotes.arch.gui.screen.ingame;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kosmx.emotes.arch.gui.screen.AbstractControlledModScreen;
import io.github.kosmx.emotes.arch.gui.screen.IDrawableImpl;
import io.github.kosmx.emotes.arch.gui.screen.IWidgetLogicImpl;
import io.github.kosmx.emotes.main.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.main.screen.IScreenSlave;
import io.github.kosmx.emotes.main.screen.ingame.FastMenuScreenLogic;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FastChosseScreen extends AbstractControlledModScreen {
    public FastChosseScreen(Screen parent) {
        super(Component.translatable("emotecraft.fastmenu"), parent);
    }

    @Override
    protected AbstractScreenLogic<PoseStack, Screen> newMaster() {
        return new FastMenuScreenLogicImpl(this);
    }

    class FastMenuScreenLogicImpl extends FastMenuScreenLogic<PoseStack, Screen, GuiEventListener> implements IScreenHelperImpl{

        protected FastMenuScreenLogicImpl(IScreenSlave screen) {
            super(screen);
        }

        @Override
        protected IScreenSlave<PoseStack, Screen> newFullScreenMenu() {
            return new FullScreenListImpl(FastChosseScreen.this);
        }

        @Override
        protected FastMenuScreenLogic<PoseStack, Screen, GuiEventListener>.FastMenuWidget newFastMenuWidget(int x, int y, int size) {
            return new FastMenuWidgetImpl(x, y, size);
        }
        class FastMenuWidgetImpl extends FastMenuWidget implements IDrawableImpl, IWidgetLogicImpl {
            public FastMenuWidgetImpl(int x, int y, int size) {
                super(x, y, size);
            }

            @Override
            public GuiEventListener get() {
                return this;
            }
        }
    }
}
