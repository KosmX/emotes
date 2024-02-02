package io.github.kosmx.emotes.arch.screen.ingame;

import io.github.kosmx.emotes.arch.gui.widgets.AbstractEmoteListWidget;
import io.github.kosmx.emotes.arch.screen.EmoteMenu;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.arch.screen.EmoteConfigScreen;
import io.github.kosmx.emotes.main.network.ClientEmotePlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Stuff to override/implement
 * init
 * isPauseScreen
 * render
 */
public class FullMenuScreenHelper extends EmoteConfigScreen {

    private EditBox searchBox;
    private EmoteListFS emoteList;

    protected FullMenuScreenHelper(Screen screen) {
        super(Component.translatable("emotecraft.emotelist"), screen);
    }

    private EmoteConfigScreen newEmoteMenu() {
        return new EmoteMenu(this);
    }

    @Override
    public void init(){
        int x = (int) Math.min(getWidth() * 0.8, getHeight() - 60);
        int x1 = (getWidth() - x) / 2;
        this.searchBox = new EditBox(Minecraft.getInstance().font, x1, 12, x, 20, Component.translatable("emotecraft.search"));
        this.searchBox.setResponder((string)->emoteList.filter(string::toLowerCase));
        this.emoteList = newEmoteList(x, getHeight(), getWidth());
        this.emoteList.emotesSetLeftPos((getWidth() - x) / 2);
        emoteList.setEmotes(EmoteHolder.list, false);
        addToChildren(searchBox);
        addToChildren(emoteList);
        setInitialFocus(this.searchBox);
        int x3 = getWidth() - 120;
        int y1 = getHeight() - 30;
        Component msg1 = CommonComponents.GUI_CANCEL;
        addRenderableWidget(Button.builder(msg1, (button1 -> getMinecraft().setScreen(null))).pos(x3, y1).size(96, 20).build());
        int x2 = getWidth() - 120;
        int y = getHeight() - 60;
        Component msg = Component.translatable("emotecraft.config");
        addRenderableWidget(Button.builder(msg, (button -> getMinecraft().setScreen(newEmoteMenu()))).pos(x2, y).size(96, 20).build());
    }

    private EmoteListFS newEmoteList(int boxSize, int height, int width){
        int l = width > (width + boxSize)/2 + 120 ? (height + boxSize)/2 + 10 : height - 80;
        return new EmoteListFS(getMinecraft(), boxSize, height, (height-boxSize)/2+10, l, 36, this);
    }

    @Override
    public boolean isPauseScreen(){
        return false;
    }


    @Override
    public void render(@NotNull GuiGraphics matrices, int mouseX, int mouseY, float delta){
        renderDirtBackground(matrices);
        this.emoteList.renderThis(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }


    public static class EmoteListFS extends AbstractEmoteListWidget<EmoteListFS.EmotelistEntryImpl> {

        public EmoteListFS(Minecraft minecraftClient, int i, int j, int k, int l, int m, Screen screen) {
            super(minecraftClient, i, j, k, l, screen);
            setPosition(getX(), m);
        }

        @Override
        protected EmotelistEntryImpl newEmoteEntry(Minecraft client, EmoteHolder emoteHolder) {
            return new EmotelistEntryImpl(client, emoteHolder);
        }

        public static class EmotelistEntryImpl extends AbstractEmoteListWidget.AbstractEmoteEntry<EmotelistEntryImpl>{

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
