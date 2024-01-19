package io.github.kosmx.emotes.arch.gui;

import io.github.kosmx.emotes.arch.gui.screen.*;
import io.github.kosmx.emotes.arch.gui.widgets.AbstractEmoteListWidget;
import io.github.kosmx.emotes.arch.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.arch.screen.EmoteMenu;
import io.github.kosmx.emotes.arch.screen.IScreenSlave;
import io.github.kosmx.emotes.arch.screen.widget.IEmoteListWidgetHelper;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EmoteMenuImpl extends AbstractControlledModScreen {

    protected EmoteMenuImpl(Component title, Screen parent) {
        super(title, parent);
    }

    public EmoteMenuImpl(Screen parent){
        this(Component.translatable("emotecraft.menu"), parent);
    }


    @Override
    protected AbstractScreenLogic newMaster() {
        return new EmoteMenuController(this);
    }

    public class EmoteMenuController extends EmoteMenu implements IScreenHelperImpl {

        public EmoteMenuController(IScreenSlave screen) {
            super(screen);
        }

        @Override
        protected FastChooseWidget newFastChooseWidghet(int x, int y, int size) {
            return new FastMenuImpl(x, y, size);
        }

        @Override
        public void openClothConfigScreen() {
            Minecraft.getInstance().setScreen(new ConfigScreen(EmoteMenuImpl.this));
        }

        @Override
        public void openExportMenuScreen() {
            Minecraft.getInstance().setScreen(new ExportMenuScreenImpl(EmoteMenuImpl.this));
        }

        @Override
        protected IEmoteListWidgetHelper newEmoteList(int width, int height) {
            return new EmoteListImpl(Minecraft.getInstance(), width, height, 51, height-32, 36, EmoteMenuImpl.this);
            //super(minecraftClient, width, height , 51, height - 32, 36, screen);
        }

        public class EmoteListImpl extends AbstractEmoteListWidget<EmoteListImpl.EmoteListEntryImpl> {

            public EmoteListImpl(Minecraft minecraftClient, int i, int j, int k, int l, int m, Screen screen) {
                super(minecraftClient, i, j, k, l, m, screen);

            }

            @Override
            protected EmoteListEntryImpl newEmoteEntry(Minecraft client, EmoteHolder emoteHolder) {
                return new EmoteListEntryImpl(client, emoteHolder);
            }


            public class EmoteListEntryImpl extends AbstractEmoteListWidget.AbstractEmoteEntry<EmoteListEntryImpl>{

                public EmoteListEntryImpl(Minecraft client, EmoteHolder emote) {
                    super(client, emote);
                }

                @Override
                protected void onPressed() {
                    EmoteListImpl.this.setSelected(this);
                }
            }
        }

        public class FastMenuImpl extends EmoteMenu.FastChooseWidget implements IWidgetLogicImpl {
            private boolean focused = true;

            public FastMenuImpl(int x, int y, int size) {
                super(x, y, size);
            }

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
