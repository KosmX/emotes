package com.kosmx.emotes.fabric.gui.widgets;

import com.kosmx.emotes.fabric.gui.screen.IDrawableImpl;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.main.screen.widget.IEmoteListWidgetHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractEmoteListWidget<E extends AbstractEmoteListWidget.AbstractEmoteEntry<E>> extends AlwaysSelectedEntryListWidget<E> implements IEmoteListWidgetHelper<MatrixStack, Element>, IDrawableImpl {

    @Override
    public IEmoteEntry getSelectedEntry() {
        return this.getSelected();
    }

    protected List<E> emotes = new ArrayList<>();
    private final Screen screen;

    public AbstractEmoteListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, int m, Screen screen){
        super(minecraftClient, i, j, k, l, m);
        this.centerListVertically = false;
        this.screen = screen;
    }


    @Override
    public int getRowWidth(){
        return this.width - 5;
    }

    protected abstract E newEmoteEntry(MinecraftClient client, EmoteHolder emoteHolder);

    @Override
    public void emotesSetLeftPos(int left) {
        this.setLeftPos(left);
    }

    public void setEmotes(List<EmoteHolder> list){
        for(EmoteHolder emoteHolder:list){
            this.emotes.add(newEmoteEntry(MinecraftClient.getInstance(), emoteHolder));
        }
        filter(() -> "");
    }

    public void filter(Supplier<String> string){
        this.clearEntries();
        for(E emote : this.emotes){
            if(emote.emote.name.toString().toLowerCase().contains(string.get()) || emote.emote.description.toString().toLowerCase().contains(string.get()) || emote.emote.author.toString().toLowerCase().equals(string.get())){
                this.addEntry(emote);
            }
        }
    }

    @Override
    public void renderThis(MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        this.render(matrices, mouseX, mouseY, tickDelta);
    }

    @Override
    protected int getScrollbarPositionX(){
        return this.right - 6;
    }

    @Override
    protected boolean isFocused(){
        return screen.getFocused() == this;
    }

    @Override
    public AbstractEmoteListWidget<E> get() {
        return this;
    }

    public static abstract class AbstractEmoteEntry<T extends AbstractEmoteEntry<T>> extends AlwaysSelectedEntryListWidget.Entry<T> implements IEmoteEntry<MatrixStack>, IDrawableImpl {
        protected final MinecraftClient client;
        public final EmoteHolder emote;

        public AbstractEmoteEntry(MinecraftClient client, EmoteHolder emote){
            this.client = client;
            this.emote = emote;
        }


        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta){
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

        protected abstract void onPressed();
    }
}
