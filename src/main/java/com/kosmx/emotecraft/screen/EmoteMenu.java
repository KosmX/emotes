package com.kosmx.emotecraft.screen;

import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.screen.widget.AbstractEmoteListWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

import java.util.List;

public class EmoteMenu extends Screen {
    private final Screen parent;
    private EmoteListWidget emoteList;
    public EmoteMenu(Screen parent){
        super(new TranslatableText("menu_title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        //TODO search box
        this.emoteList = new EmoteListWidget(this.client, (int)(this.width/2.2-16), this.height, this);
        this.emoteList.setLeftPos(this.width/2-(int)(this.width/2.2-16)-12);
        this.children.add(this.emoteList);
        emoteList.setEmotes(EmoteHolder.list);
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        this.emoteList.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void onClose(){
        //TODO
        this.client.openScreen(this.parent);
    }

    public static class EmoteListWidget extends AbstractEmoteListWidget<EmoteListWidget.EmoteListEntry> {
        public EmoteListWidget(MinecraftClient minecraftClient, int width, int height, Screen screen) {
            super(minecraftClient, width, height, 51, height-32, 36, screen);
        }

        @Override
        public void setEmotes(List<EmoteHolder> list) {
            for(EmoteHolder emote : list){
                this.emotes.add(new EmoteListEntry(this.client, emote));
            }
            filter("");
        }

        public  class EmoteListEntry extends AbstractEmoteListWidget.AbstractEmoteEntry<EmoteListEntry> {
            public EmoteListEntry(MinecraftClient client, EmoteHolder emote) {
                super(client, emote);
            }

            protected void onPressed() {        //setup screen -> select pack, play screen -> play
                EmoteListWidget.this.setSelected(this);
            }
        }
    }
}
