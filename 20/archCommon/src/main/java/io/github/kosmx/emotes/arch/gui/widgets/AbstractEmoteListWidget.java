package io.github.kosmx.emotes.arch.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kosmx.emotes.arch.executor.types.TextImpl;
import io.github.kosmx.emotes.arch.gui.screen.IDrawableImpl;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.screen.widget.IEmoteListWidgetHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractEmoteListWidget<E extends AbstractEmoteListWidget.AbstractEmoteEntry<E>> extends ObjectSelectionList<E> implements IEmoteListWidgetHelper<GuiGraphics, GuiEventListener>, IDrawableImpl {

    @Override
    public IEmoteEntry getSelectedEntry() {
        return this.getSelected();
    }

    protected List<E> emotes = new ArrayList<>();
    private final Screen screen;

    public AbstractEmoteListWidget(Minecraft minecraftClient, int i, int j, int k, int l, int m, Screen screen){
        super(minecraftClient, i, j, k, m);
        this.centerListVertically = false;
        this.screen = screen;
    }


    @Override
    public int getRowWidth(){
        return this.width - 5;
    }

    protected abstract E newEmoteEntry(Minecraft client, EmoteHolder emoteHolder);

    @Override
    public void emotesSetLeftPos(int left) {
        // this.setPosition(left);
    }

    @Override
    public void setEmotes(Iterable<EmoteHolder> list, boolean showInvalid){
        this.emotes = new ArrayList<>();
        for(EmoteHolder emoteHolder:list){
            this.emotes.add(newEmoteEntry(Minecraft.getInstance(), emoteHolder));
        }
        if(showInvalid) {
            for (EmoteHolder emoteHolder : getEmptyEmotes()) {
                this.emotes.add(newEmoteEntry(Minecraft.getInstance(), emoteHolder));
            }
        }
        this.emotes.sort(Comparator.comparing(o -> o.emote.name.getString().toLowerCase()));
        filter(() -> "");
    }

    public void filter(Supplier<String> string){
        this.clearEntries();
        for(E emote : this.emotes){
            if(emote.emote.name.getString().toLowerCase().contains(string.get()) || emote.emote.description.getString().toLowerCase().contains(string.get()) || emote.emote.author.getString().toLowerCase().equals(string.get())){
                this.addEntry(emote);
            }
        }
        this.setScrollAmount(0);
    }

    @Override
    public void renderThis(GuiGraphics matrices, int mouseX, int mouseY, float tickDelta) {
        this.render(matrices, mouseX, mouseY, tickDelta);
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() - 6;
    }

    @Override
    public boolean isFocused(){
        return screen.getFocused() == this;
    }

    @Override
    public AbstractEmoteListWidget<E> get() {
        return this;
    }

    public static abstract class AbstractEmoteEntry<T extends AbstractEmoteEntry<T>> extends ObjectSelectionList.Entry<T> implements IEmoteEntry<GuiGraphics>, IDrawableImpl {
        protected final Minecraft client;
        public final EmoteHolder emote;

        public AbstractEmoteEntry(Minecraft client, EmoteHolder emote){
            this.client = client;
            this.emote = emote;
        }


        @Override
        public void render(GuiGraphics matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta){
            this.renderThis(matrices, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }

        @Override
        public EmoteHolder getEmote() {
            return this.emote;
        }


        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button){
            if(button == 0){
                this.onPressed();
                return true;
            }else{
                return false;
            }
        }

        @Override
        public Component getNarration() {
            return ((TextImpl)this.emote.name).get();
        }

        protected abstract void onPressed();
    }
}
