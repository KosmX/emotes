package com.kosmx.emotes.main.screen;

import com.kosmx.emotes.common.tools.MathHelper;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.dataTypes.InputKey;
import com.kosmx.emotes.executor.dataTypes.Text;
import com.kosmx.emotes.executor.dataTypes.other.TextFormatting;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IWidget;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.main.config.ClientConfig;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import com.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import com.kosmx.emotes.main.config.Serializer;
import com.kosmx.emotes.main.screen.widget.AbstractEmoteListWidget;
import com.kosmx.emotes.main.screen.widget.AbstractFastChooseWidget;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * The EmoteMenu
 * Override theses stuff
 * onKeyPressed
 * onMouseClicked
 * onClose - just open the parent
 * onRemove
 * tick()
 */
public abstract class EmoteMenu<MATRIX> implements IScreenLogic<MATRIX> {
    private int activeKeyTime = 0;
    private EmoteListWidget emoteList;
    private FastChooseWidget fastMenu;
    //protected List<buttons> buttons is already exists
    private static final Text unboundText = EmoteInstance.instance.getDefaults().getUnknownKey().getLocalizedText();
    private IButton setKeyButton;
    public boolean save = false;
    public boolean warn = false;
    private ITextInputWidget<MATRIX> searchBox;
    private List<PositionedText> texts = new ArrayList<>();
    private IButton resetKey;

    public boolean exportGeckoEmotes = false;

    protected void init(){
        if(warn && ((ClientConfig)EmoteInstance.config).enableQuark){
            warn = false;
            IConfirmScreen csr = createConfigScreen((bool)->{
                ((ClientConfig)EmoteInstance.config).enableQuark = bool;
                this.openThisScreen();
            }, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.quark"), EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.quark2"));
            EmoteInstance.instance.getClientMethods().openScreen(csr);
            csr.setTimeout(56);
        }

        this.texts = new ArrayList<>();
        Client.initEmotes();

        if(this.exportGeckoEmotes){
            exportGeckoEmotes = false;
            EmoteHolder.list.forEach(emoteHolder -> {
                if(emoteHolder.isFromGeckoLib){
                    File dir = EmoteInstance.instance.getGameDirectory().resolve("emotes").resolve("GeckoLibExport").toFile();
                    if(!dir.isDirectory()){
                        if(!dir.mkdirs()){
                            EmoteInstance.instance.getLogger().log(Level.WARNING, "can't create directory for exporting emotes");
                            return;
                        }
                    }
                    Path target = dir.toPath().resolve(emoteHolder.name.getString() + ".json");
                    try {
                        BufferedWriter writer = Files.newBufferedWriter(target);
                        Serializer.serializer.toJson(emoteHolder, writer);
                        writer.close();
                    } catch (IOException e) {
                        EmoteInstance.instance.getLogger().log(Level.WARNING, "Can't create file: " + e.getMessage(), true);
                        if(EmoteInstance.config.showDebug) e.printStackTrace();
                    }
                }
            });
        }

        this.searchBox = newTextInputWidget(this.getHeight() / 2 - (int) (this.getHeight() / 2.2 - 16) - 12, 12, (int) (this.getHeight() / 2.2 - 16), 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.search"));

        this.searchBox.setInputListener((string)->this.emoteList.filter(string::toLowerCase));
        addToChildren(searchBox);

        addToButtons(newButton(this.getHeight() / 2 - 154, this.getHeight() - 30, 150, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.openFolder"), (buttonWidget)->this.openExternalEmotesDir()));

        //this.emoteList = new EmoteListWidget(this.client, (int) (this.getHeight() / 2.2 - 16), this.getHeight(), this);
        this.emoteList = newEmoteList();
        this.emoteList.setLeftPos(this.getHeight() / 2 - (int) (this.getHeight() / 2.2 - 16) - 12);
        addToChildren(this.emoteList);
        int x = Math.min(this.getHeight() / 4, (int) (this.getHeight() / 2.5));
        this.fastMenu = newFastChooseWidghet(this.getHeight() / 2 + 2, this.getHeight() / 2 - 8, x - 7);
        addToChildren(fastMenu);
        addToButtons(newButton(this.getHeight() - 100, 4, 96, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.options"), (button->openClothConfigScreen())));
        addToButtons(newButton(this.getHeight() / 2 + 10, this.getHeight() - 30, 96, 20, EmoteInstance.instance.getDefaults().defaultTextsDone(), (button->openParent())));
        setKeyButton = newButton(this.getHeight() / 2 + 6, 60, 96, 20, unboundText, button->this.activateKey());
        addToButtons(setKeyButton);
        resetKey = newButton(this.getHeight() / 2 + 124, 60, 96, 20, EmoteInstance.instance.getDefaults().newTranslationText("controls.reset"), (button->{
            if(emoteList.getSelected() != null){
                emoteList.getSelected().emote.keyBinding = EmoteInstance.instance.getDefaults().getUnknownKey();
                this.save = true;
            }
        }));
        addToButtons(resetKey);
        emoteList.setEmotes(EmoteHolder.list);
        this.setInitialFocus(this.searchBox);
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.keybind"), this.getHeight() / 2 + 115, 40));
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.fastmenu"), this.getHeight() / 2 + 10 + x / 2, getHeight() / 2 - 54));
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.fastmenu2"), this.getHeight() / 2 + 10 + x / 2, getHeight() / 2 - 40));
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.fastmenu3"), this.getHeight() / 2 + 10 + x / 2, getHeight() / 2 - 26));
    }

    abstract FastChooseWidget newFastChooseWidghet(int x, int y, int size);
    abstract public void openExternalEmotesDir();
    abstract public void openClothConfigScreen(); //will we use cloth or nope.

    private void activateKey(){
        if(emoteList.getSelected() != null){
            this.setFocusedElement(setKeyButton);
            activeKeyTime = 200;
        }
    }

    public void setFocusedElement(@Nullable IWidget focused){
        if(activeKeyTime == 0){
            this.setFocused(focused);
        }
    }

    public void tick(){
        if(activeKeyTime == 1){
            setFocusedElement(null);
        }
        if(activeKeyTime != 0){
            activeKeyTime--;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(this.activeKeyTime != 0 && emoteList.getSelected() != null){
            return setKey(EmoteInstance.instance.getDefaults().getMouseKeyFromCode(button));
        }
        return false;
    }


    public void render(MATRIX matrices, int mouseX, int mouseY, float delta){
        this.renderBackgroundTexture(0);
        if(this.emoteList.getSelected() == null){
            this.setKeyButton.setActive(false);
            this.resetKey.setActive(false);
        }else{
            this.setKeyButton.setActive(true);
            this.resetKey.setActive(! this.emoteList.getSelected().emote.keyBinding.equals(EmoteInstance.instance.getDefaults().getUnknownKey()));
        }
        for(PositionedText str : texts){
            str.render(matrices);
        }
        this.emoteList.render(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        this.fastMenu.render(matrices, mouseX, mouseY, delta);
        updateKeyText();
    }

    private boolean setKey(InputKey key){
        boolean bl = false;
        if(emoteList.getSelected() != null){
            bl = true;
            if(! applyKey(false, emoteList.getSelected().emote, key)){
                EmoteInstance.instance.getClientMethods().openScreen(createConfigScreen(aBoolean -> confirmReturn(aBoolean, emoteList.getSelected().emote, key), EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.sure"), EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.sure2")));
            }
        }
        return bl;
    }

    private void confirmReturn(boolean choice, EmoteHolder emoteHolder, InputKey key){
        if(choice){
            applyKey(true, emoteHolder, key);
            this.saveConfig();
        }
        openThisScreen();
    }

    private boolean applyKey(boolean force, EmoteHolder emote, InputKey key){
        boolean bl = true;
        for(EmoteHolder emoteHolder : EmoteHolder.list){
            if(! key.equals(EmoteInstance.instance.getDefaults().getUnknownKey()) && emoteHolder.keyBinding.equals(key)){
                bl = false;
                if(force){
                    emoteHolder.keyBinding = EmoteInstance.instance.getDefaults().getUnknownKey();
                }
            }
        }
        if(bl || force){
            emote.keyBinding = key;
            this.save = true;
        }
        this.activeKeyTime = 0;
        return bl;
    }


    public void removed(){
        if(save){
            this.saveConfig();
        }
    }

    private void saveConfig(){
        EmoteHolder.bindKeys((ClientConfig) EmoteInstance.config);
        try{
            BufferedWriter writer = Files.newBufferedWriter(EmoteInstance.instance.getConfigPath());
            Serializer.serializer.toJson(EmoteInstance.config, writer);
            writer.close();
            //FileUtils.write(Main.CONFIGPATH, Serializer.serializer.toJson(Main.config), "UTF-8", false);
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void updateKeyText(){
        if(emoteList.getSelected() != null){
            Text message = emoteList.getSelected().emote.keyBinding.getLocalizedText();
            if(activeKeyTime != 0)
                //message = (new LiteralText("> ")).append(message.shallowCopy().formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW);
                message = EmoteInstance.instance.getDefaults().textFromString("> ").append(message).formatted(TextFormatting.YELLOW).append(" <").formatted(TextFormatting.YELLOW);
            setKeyButton.setMessage(message);
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int mod){
        if(emoteList.getSelected() != null && activeKeyTime != 0) {
            if (keyCode == 256) {
                return setKey(EmoteInstance.instance.getDefaults().getUnknownKey());
            }
            else {
                return setKey(EmoteInstance.instance.getDefaults().getKeyFromCode(keyCode, scanCode));
            }
        }
        return false;
    }

    protected abstract EmoteListWidget newEmoteList();

    public abstract class EmoteListWidget extends AbstractEmoteListWidget<EmoteListWidget.EmoteListEntry, MATRIX> {
        //public EmoteListWidget(MinecraftClient minecraftClient, int width, int height, Screen screen){
        //    super(minecraftClient, width, height , 51, height - 32, 36, screen);
        //}
/*
        @Override
        public void setEmotes(List<EmoteHolder> list){
            for(EmoteHolder emote : list){
                this.emotes.add(new EmoteListEntry(emote));
            }
            filter(()->"");
        }
 */
        public abstract class EmoteListEntry extends AbstractEmoteListWidget<EmoteListEntry, MATRIX>.AbstractEmoteEntry {
            public EmoteListEntry(EmoteHolder emote) {
                super(emote);
            }

            protected void onPressed(){        //setup screen -> select pack, play screen -> play
                EmoteListWidget.this.setSelected(this);
            }
        }
    }

    protected abstract class FastChooseWidget extends AbstractFastChooseWidget<MATRIX> {

        public FastChooseWidget(int x, int y, int size){
            super(x, y, size);
        }

        @Override
        protected boolean isValidClickButton(int button){
            return (button == 0 || button == 1) && activeKeyTime == 0;
        }

        @Override
        protected boolean onClick(FastChooseElement element, int button){
            if(activeKeyTime != 0) return false;
            if(button == 1){
                element.clearEmote();
                save = true;
                return true;
            }else if(emoteList.getSelected() != null){
                element.setEmote(emoteList.getSelected().emote);
                save = true;
                return true;
            }else{
                return false;
            }
        }

        @Override
        protected boolean doHoverPart(FastChooseElement part){
            return activeKeyTime == 0;
        }
    }

    private class PositionedText {
        private final Text str;
        private final int x;
        private final int y;

        private PositionedText(Text str, int x, int y) {
            this.str = str;
            this.x = x;
            this.y = y;
        }

        private void render(MATRIX matrixStack) {
            textDraw(matrixStack, this.str, this.x, this.y, MathHelper.colorHelper(255, 255, 255, 255));
            //textRenderer.getClass();
        }
    }
}
