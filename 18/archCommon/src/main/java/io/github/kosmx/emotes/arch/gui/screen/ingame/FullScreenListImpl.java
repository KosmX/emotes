package io.github.kosmx.emotes.arch.gui.screen.ingame;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kosmx.emotes.arch.gui.EmoteMenuImpl;
import io.github.kosmx.emotes.arch.gui.screen.AbstractControlledModScreen;
import io.github.kosmx.emotes.arch.gui.widgets.AbstractEmoteListWidget;
import io.github.kosmx.emotes.executor.dataTypes.screen.IScreen;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import io.github.kosmx.emotes.main.screen.AbstractScreenLogic;
import io.github.kosmx.emotes.main.screen.IScreenSlave;
import io.github.kosmx.emotes.main.screen.ingame.FullMenuScreenHelper;
import io.github.kosmx.emotes.main.screen.widget.IEmoteListWidgetHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public class FullScreenListImpl extends AbstractControlledModScreen {
    protected FullScreenListImpl(Screen parent) {
        super(new TranslatableComponent("emotecraft.emotelist"), parent);
    }

    @Override
    protected AbstractScreenLogic<PoseStack, Screen> newMaster() {
        return new FullScreenMenuImpl(this);
    }

    class FullScreenMenuImpl extends FullMenuScreenHelper<PoseStack, Screen, GuiEventListener> implements IScreenHelperImpl{

        protected FullScreenMenuImpl(IScreenSlave screen) {
            super(screen);
        }

        @Override
        public IScreen<Screen> newEmoteMenu() {
            return new EmoteMenuImpl(FullScreenListImpl.this);
        }

        @Override
        protected IEmoteListWidgetHelper<PoseStack, GuiEventListener> newEmoteList(int boxSize, int height, int k, int l, int m) {
            return new EmoteListFS(Minecraft.getInstance(), boxSize, height, k, l, m, FullScreenListImpl.this);
        }

        public class EmoteListFS extends AbstractEmoteListWidget<EmoteListFS.EmotelistEntryImpl> {

            public EmoteListFS(Minecraft minecraftClient, int i, int j, int k, int l, int m, Screen screen) {
                super(minecraftClient, i, j, k, l, m, screen);
            }

            @Override
            protected EmotelistEntryImpl newEmoteEntry(Minecraft client, EmoteHolder emoteHolder) {
                return new EmotelistEntryImpl(client, emoteHolder);
            }

            public class EmotelistEntryImpl extends AbstractEmoteListWidget.AbstractEmoteEntry<EmotelistEntryImpl>{

                public EmotelistEntryImpl(Minecraft client, EmoteHolder emote) {
                    super(client, emote);
                }

                @Override
                protected void onPressed() {
                    ClientEmotePlay.clientStartLocalEmote(this.getEmote());
                    Minecraft.getInstance().setScreen(null);
                }
            }
        }
    }
}
