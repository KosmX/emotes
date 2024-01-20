package io.github.kosmx.emotes.arch.gui.screen.ingame;

import io.github.kosmx.emotes.arch.gui.screen.AbstractControlledModScreen;
import io.github.kosmx.emotes.arch.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.arch.screen.IScreenSlave;
import io.github.kosmx.emotes.arch.screen.ingame.FastMenuScreenLogic;
import io.github.kosmx.emotes.arch.screen.widget.IWidgetLogic;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FastChosseScreen extends AbstractControlledModScreen {
    public FastChosseScreen(Screen parent) {
        super(Component.translatable("emotecraft.fastmenu"), parent);
    }

    @Override
    protected AbstractScreenLogic newMaster() {
        return new FastMenuScreenLogicImpl(this);
    }

    class FastMenuScreenLogicImpl extends FastMenuScreenLogic implements IScreenHelperImpl{

        protected FastMenuScreenLogicImpl(IScreenSlave screen) {
            super(screen);
        }

        @Override
        protected IScreenSlave newFullScreenMenu() {
            return new FullScreenListImpl(FastChosseScreen.this);
        }

        @Override
        protected FastMenuScreenLogic.FastMenuWidget newFastMenuWidget(int x, int y, int size) {
            return new FastMenuWidgetImpl(x, y, size);
        }
        class FastMenuWidgetImpl extends FastMenuWidget implements IWidgetLogic {
            public FastMenuWidgetImpl(int x, int y, int size) {
                super(x, y, size);
            }
            private boolean focused = false;


            @Override
            public GuiEventListener get() {
                return this;
            }

            @Override
            public void setFocused(boolean bl) {
                focused = bl;
            }

            @Override
            public boolean isFocused() {
                return focused;
            }
        }
    }
}
