package com.kosmx.emotecraft.screen;

import com.kosmx.emotecraft.config.EmoteHolder;
import com.kosmx.emotecraft.screen.widget.AbstractEmoteListWidget;
import com.kosmx.emotecraft.screen.widget.AbstractFastChooseWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import javax.annotation.Nullable;
import java.util.List;

public class EmoteMenu extends Screen {
    private final Screen parent;
    private int activeKeyTime = 0;
    private EmoteListWidget emoteList;
    private FastChooseWidget fastMenu;
    //protected List<buttons> buttons is already exists
    private static final Text unboundText = InputUtil.UNKNOWN_KEY.getLocalizedText();
    private ButtonWidget setKeyButton;
    private boolean save = false;


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
        int x = Math.min(this.width/4, this.height/2);
        this.fastMenu = new FastChooseWidget(this.width/2 + 2, this.height/2 - 8, x-7);
        this.children.add(fastMenu);
        this.buttons.add(new ButtonWidget(this.width - 100, 4, 96, 20, new TranslatableText("emotecraft.options.options"), (button -> {
            this.client.openScreen(this.parent);
        })));
        this.buttons.add(new ButtonWidget(this.width/2 + 10, this.height - 30, 96, 20, ScreenTexts.DONE, (button -> {
            this.client.openScreen(this.parent);
        })));
        setKeyButton = new ButtonWidget(this.width/2 + 6, 60, 96, 20, unboundText, button -> {
            this.activateKey();
        });
        this.buttons.add(setKeyButton);
        this.buttons.add(new ButtonWidget(this.width/2 + 124, 60, 96, 20, new TranslatableText("controls.reset"), (button -> {
            if(emoteList.getSelected() != null) emoteList.getSelected().emote.keyBinding = InputUtil.UNKNOWN_KEY;
        })));
        emoteList.setEmotes(EmoteHolder.list);
        this.children.addAll(buttons);
        super.init();
    }

    private void activateKey(){
        if(emoteList.getSelected() != null) {
            this.setFocused(setKeyButton);
            activeKeyTime = 200;
        }
    }

    @Override
    public void setFocused(@Nullable Element focused) {
        if(activeKeyTime == 0) super.setFocused(focused);
    }

    @Override
    public void tick() {
        super.tick();
        if(activeKeyTime == 1){
            setFocused(null);
        }
        if(activeKeyTime != 0){
            activeKeyTime--;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.activeKeyTime != 0 && emoteList.getSelected() != null){
            emoteList.getSelected().emote.keyBinding = InputUtil.Type.MOUSE.createFromCode(button);
            activeKeyTime = 0;
            return true;
        }
        else return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(0);
        this.emoteList.render(matrices, mouseX, mouseY, delta);
        this.fastMenu.render(matrices, mouseX, mouseY, delta);
        updateKeyText();
        super.render(matrices, mouseX, mouseY, delta);
    }

    public void onClose(){
        //TODO
        this.client.openScreen(this.parent);
    }

    private void updateKeyText(){
        if(emoteList.getSelected() != null){
            Text message = emoteList.getSelected().emote.keyBinding.getLocalizedText();
            if(activeKeyTime != 0)message = (new LiteralText("> ")).append(message.shallowCopy().formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW);
            setKeyButton.setMessage(message);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int mod) {
        if(emoteList.getSelected() != null && activeKeyTime != 0){
            if(keyCode == 256){
                emoteList.getSelected().emote.keyBinding = InputUtil.UNKNOWN_KEY;
            }
            else {
                emoteList.getSelected().emote.keyBinding = InputUtil.fromKeyCode(keyCode, scanCode);
            }
            activeKeyTime = 0;
            return true;
        }
        else {
            return super.keyPressed(keyCode, scanCode, mod);
        }
    }



    public class EmoteListWidget extends AbstractEmoteListWidget<EmoteListWidget.EmoteListEntry> {
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

    private class FastChooseWidget extends AbstractFastChooseWidget{

        public FastChooseWidget(int x, int y, int size) {
            super(x, y, size);
        }

        @Override
        protected boolean isValidClickButton(int button) {
            return (button == 0 || button == 1) && activeKeyTime == 0;
        }

        @Override
        protected boolean onClick(FastChooseElement element, int button) {
            if(activeKeyTime != 0)return false;
            if(button == 1){
                element.clearEmote();
                return true;
            }
            else if (emoteList.getSelected() != null){
                element.setEmote(emoteList.getSelected().emote);
                save = true;
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        protected boolean doHoverPart(FastChooseElement part) {
            return activeKeyTime == 0;
        }
    }
}
