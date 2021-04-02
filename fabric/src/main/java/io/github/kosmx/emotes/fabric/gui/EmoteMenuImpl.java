package io.github.kosmx.emotes.fabric.gui;

import io.github.kosmx.emotes.fabric.executor.EmotesMain;
import io.github.kosmx.emotes.fabric.gui.screen.AbstractControlledModScreen;
import io.github.kosmx.emotes.fabric.gui.screen.ConfigScreen;
import io.github.kosmx.emotes.fabric.gui.screen.IDrawableImpl;
import io.github.kosmx.emotes.fabric.gui.screen.IWidgetLogicImpl;
import io.github.kosmx.emotes.fabric.gui.widgets.AbstractEmoteListWidget;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.main.screen.EmoteMenu;
import io.github.kosmx.emotes.main.screen.IScreenSlave;
import io.github.kosmx.emotes.main.screen.widget.IEmoteListWidgetHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

public class EmoteMenuImpl extends AbstractControlledModScreen {

    protected EmoteMenuImpl(Text title, Screen parent) {
        super(title, parent);
    }

    public EmoteMenuImpl(Screen parent){
        this(new TranslatableText("emotecraft.menu"), parent);
    }


    @Override
    protected AbstractScreenLogic<MatrixStack, Screen> newMaster() {
        return new EmoteMenuController(this);
    }

    public class EmoteMenuController extends EmoteMenu<MatrixStack, Screen, Element> implements IScreenHelperImpl{

        public EmoteMenuController(IScreenSlave screen) {
            super(screen);
        }

        @Override
        protected FastChooseWidget newFastChooseWidghet(int x, int y, int size) {
            return new FastMenuImpl(x, y, size);
        }

        @Override
        public void openExternalEmotesDir() {
            Util.getOperatingSystem().open(EmotesMain.instance.getExternalEmoteDir());
        }

        @Override
        public void openClothConfigScreen() {
            MinecraftClient.getInstance().openScreen(new ConfigScreen(EmoteMenuImpl.this));
        }

        @Override
        protected IEmoteListWidgetHelper<MatrixStack, Element> newEmoteList(int width, int height) {
            return new EmoteListImpl(MinecraftClient.getInstance(), width, height, 51, height-32, 36, EmoteMenuImpl.this);
            //super(minecraftClient, width, height , 51, height - 32, 36, screen);
        }

        public class EmoteListImpl extends AbstractEmoteListWidget<EmoteListImpl.EmoteListEntryImpl> {

            public EmoteListImpl(MinecraftClient minecraftClient, int i, int j, int k, int l, int m, Screen screen) {
                super(minecraftClient, i, j, k, l, m, screen);

            }

            @Override
            protected EmoteListEntryImpl newEmoteEntry(MinecraftClient client, EmoteHolder emoteHolder) {
                return new EmoteListEntryImpl(client, emoteHolder);
            }


            public class EmoteListEntryImpl extends AbstractEmoteListWidget.AbstractEmoteEntry<EmoteListEntryImpl>{

                public EmoteListEntryImpl(MinecraftClient client, EmoteHolder emote) {
                    super(client, emote);
                }

                @Override
                protected void onPressed() {
                    EmoteListImpl.this.setSelected(this);
                }
            }
        }

        public class FastMenuImpl extends EmoteMenu<MatrixStack, Screen, Element>.FastChooseWidget implements IDrawableImpl, IWidgetLogicImpl {

            public FastMenuImpl(int x, int y, int size) {
                super(x, y, size);
            }

            @Override
            public Element get() {
                return this;
            }

        }
    }
}
