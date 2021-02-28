package com.kosmx.emotes.fabric.gui;

import com.kosmx.emotes.fabric.executor.EmotesMain;
import com.kosmx.emotes.fabric.gui.screen.AbstractControlledModScreen;
import com.kosmx.emotes.fabric.gui.screen.IDrawableImpl;
import com.kosmx.emotes.fabric.gui.widgets.AbstractEmoteListWidget;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.main.screen.AbstractScreenLogic;
import com.kosmx.emotes.main.screen.EmoteMenu;
import com.kosmx.emotes.main.screen.IScreenSlave;
import com.kosmx.emotes.main.screen.widget.IEmoteListWidgetHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

import java.util.List;

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
            //TODO
        }

        @Override
        protected IEmoteListWidgetHelper<MatrixStack, Element> newEmoteList() {
            return new EmoteListImpl(MinecraftClient.getInstance(), EmoteMenuImpl.this.width, height, 51, height-32, 36, EmoteMenuImpl.this);
            //super(minecraftClient, width, height , 51, height - 32, 36, screen);
        }

        public class EmoteListImpl extends AbstractEmoteListWidget<EmoteListImpl.EmoteListEntryImpl>{

            public EmoteListImpl(MinecraftClient minecraftClient, int i, int j, int k, int l, int m, Screen screen) {
                super(minecraftClient, i, j, k, l, m, screen);
            }

            @Override
            public void setEmotes(List list) {

            }
            public class EmoteListEntryImpl extends AbstractEmoteListWidget.AbstractEmoteEntry<EmoteListEntryImpl>{

                public EmoteListEntryImpl(MinecraftClient client, EmoteHolder emote) {
                    super(client, emote);
                }

                @Override
                protected void onPressed() {

                }
            }
        }

        public class FastMenuImpl extends EmoteMenu<MatrixStack, Screen, Element>.FastChooseWidget implements IDrawableImpl, Element {

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
