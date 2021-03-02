package com.kosmx.emotes.fabric.gui.screen.ingame;

import com.kosmx.emotes.executor.dataTypes.screen.IScreen;
import com.kosmx.emotes.fabric.gui.EmoteMenuImpl;
import com.kosmx.emotes.fabric.gui.screen.AbstractControlledModScreen;
import com.kosmx.emotes.fabric.gui.widgets.AbstractEmoteListWidget;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.main.network.ClientEmotePlay;
import com.kosmx.emotes.main.screen.AbstractScreenLogic;
import com.kosmx.emotes.main.screen.IScreenSlave;
import com.kosmx.emotes.main.screen.ingame.FullMenuScreenHelper;
import com.kosmx.emotes.main.screen.widget.IEmoteListWidgetHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

public class FullScreenListImpl extends AbstractControlledModScreen {
    protected FullScreenListImpl(Screen parent) {
        super(new TranslatableText("emotecraft.emotelist"), parent);
    }

    @Override
    protected AbstractScreenLogic<MatrixStack, Screen> newMaster() {
        return new FullScreenMenuImpl(this);
    }

    class FullScreenMenuImpl extends FullMenuScreenHelper<MatrixStack, Screen, Element> implements IScreenHelperImpl{

        protected FullScreenMenuImpl(IScreenSlave screen) {
            super(screen);
        }

        @Override
        public IScreen<Screen> newEmoteMenu() {
            return new EmoteMenuImpl(FullScreenListImpl.this);
        }

        @Override
        protected IEmoteListWidgetHelper<MatrixStack, Element> newEmoteList(int boxSize, int height, int k, int l, int m) {
            return new EmoteListFS(MinecraftClient.getInstance(), boxSize, height, k, l, m, FullScreenListImpl.this);
        }

        public class EmoteListFS extends AbstractEmoteListWidget<EmoteListFS.EmotelistEntryImpl>{

            public EmoteListFS(MinecraftClient minecraftClient, int i, int j, int k, int l, int m, Screen screen) {
                super(minecraftClient, i, j, k, l, m, screen);
            }

            @Override
            protected EmotelistEntryImpl newEmoteEntry(MinecraftClient client, EmoteHolder emoteHolder) {
                return new EmotelistEntryImpl(client, emoteHolder);
            }

            public class EmotelistEntryImpl extends AbstractEmoteListWidget.AbstractEmoteEntry<EmotelistEntryImpl>{

                public EmotelistEntryImpl(MinecraftClient client, EmoteHolder emote) {
                    super(client, emote);
                }

                @Override
                protected void onPressed() {
                    ClientEmotePlay.clientStartLocalEmote(this.getEmote());
                    MinecraftClient.getInstance().openScreen(null);
                }
            }
        }
    }
}
