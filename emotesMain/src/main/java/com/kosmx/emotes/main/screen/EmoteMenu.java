package com.kosmx.emotes.main.screen;

import com.kosmx.emotes.common.tools.MathHelper;
import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.dataTypes.InputKey;
import com.kosmx.emotes.executor.dataTypes.Text;
import com.kosmx.emotes.executor.dataTypes.other.TextFormatting;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IWidget;
import com.kosmx.emotes.main.ClientInit;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.main.config.ClientConfig;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import com.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import com.kosmx.emotes.main.config.Serializer;
import com.kosmx.emotes.main.screen.widget.IEmoteListWidgetHelper;
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
@SuppressWarnings("unchecked")
public abstract class EmoteMenu<MATRIX, SCREEN, WIDGET> extends AbstractScreenLogic<MATRIX, SCREEN> {
    protected int activeKeyTime = 0;
    private IEmoteListWidgetHelper<MATRIX, WIDGET> emoteList;
    private FastChooseWidget fastMenu;
    //protected List<buttons> buttons is already exists
    private static final Text unboundText = EmoteInstance.instance.getDefaults().getUnknownKey().getLocalizedText();
    private IButton setKeyButton;
    public boolean save = false;
    public boolean warn = false;
    private ITextInputWidget<MATRIX, ITextInputWidget> searchBox;
    private List<PositionedText> texts = new ArrayList<>();
    private IButton resetKey;

    public boolean exportGeckoEmotes = false;

    public EmoteMenu(IScreenSlave screen) {
        super(screen);
    }

    @Override
    public void emotes_initScreen(){
        if(warn && ((ClientConfig)EmoteInstance.config).enableQuark){
            warn = false;
            IConfirmScreen csr = createConfigScreen((bool)->{
                ((ClientConfig)EmoteInstance.config).enableQuark = bool;
                screen.openThisScreen();
            }, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.quark"), EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.quark2"));
            EmoteInstance.instance.getClientMethods().openScreen(csr);
            csr.setTimeout(56);
        }

        this.texts = new ArrayList<>();
        ClientInit.loadEmotes();

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

        this.searchBox = newTextInputWidget(screen.getWidth() / 2 - (int) (screen.getWidth() / 2.2 - 16) - 12, 12, (int) (screen.getWidth() / 2.2 - 16), 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.search"));

        this.searchBox.setInputListener((string)->this.emoteList.filter(string::toLowerCase));
        screen.addToChildren(searchBox);

        screen.addToButtons(newButton(screen.getWidth() / 2 - 154, screen.getHeight() - 30, 150, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.openFolder"), (buttonWidget)->this.openExternalEmotesDir()));

        //this.emoteList = new EmoteListWidget(this.client, (int) (this.getHeight() / 2.2 - 16), this.getHeight(), this);
        this.emoteList = newEmoteList((int) (screen.getWidth()/2.2-16), screen.getHeight());
        this.emoteList.emotesSetLeftPos(screen.getWidth() / 2 - (int) (screen.getWidth() / 2.2 - 16) - 12);
        screen.addToChildren(this.emoteList);
        int x = Math.min(screen.getWidth() / 4, (int) (screen.getHeight() / 2.5));
        this.fastMenu = newFastChooseWidghet(screen.getWidth() / 2 + 2, screen.getHeight() / 2 - 8, x - 7);
        screen.addToChildren(fastMenu);
        screen.addToButtons(newButton(screen.getWidth() - 100, 4, 96, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.options"), (button->openClothConfigScreen())));
        screen.addToButtons(newButton(screen.getWidth() / 2 + 10, screen.getHeight() - 30, 96, 20, EmoteInstance.instance.getDefaults().defaultTextsDone(), (button->screen.openParent())));
        setKeyButton = newButton(screen.getWidth() / 2 + 6, 60, 96, 20, unboundText, button->this.activateKey());
        screen.addToButtons(setKeyButton);
        resetKey = newButton(screen.getWidth() / 2 + 124, 60, 96, 20, EmoteInstance.instance.getDefaults().newTranslationText("controls.reset"), (button->{
            if(emoteList.getSelectedEntry() != null){
                emoteList.getSelectedEntry().getEmote().keyBinding = EmoteInstance.instance.getDefaults().getUnknownKey();
                this.save = true;
            }
        }));
        screen.addToButtons(resetKey);
        emoteList.setEmotes(EmoteHolder.list);
        screen.setInitialFocus(this.searchBox);
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.keybind"), screen.getWidth() / 2 + 115, 40));
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.fastmenu"), screen.getWidth() / 2 + 10 + x / 2, screen.getHeight() / 2 - 54));
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.fastmenu2"), screen.getWidth() / 2 + 10 + x / 2, screen.getHeight() / 2 - 40));
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.fastmenu3"), screen.getWidth() / 2 + 10 + x / 2, screen.getHeight() / 2 - 26));
        screen.addButtonsToChildren();
    }

    protected abstract FastChooseWidget newFastChooseWidghet(int x, int y, int size);
    abstract public void openExternalEmotesDir();
    abstract public void openClothConfigScreen(); //will we use cloth or nope.

    private void activateKey(){
        if(emoteList.getSelectedEntry() != null){
            this.setFocusedElement(setKeyButton);
            activeKeyTime = 200;
        }
    }

    public void setFocusedElement(@Nullable IWidget focused){
        if(activeKeyTime == 0){
            screen.setFocused(focused);
        }
    }

    @Override
    public void emotes_tickScreen(){
        if(activeKeyTime == 1){
            setFocusedElement(null);
        }
        if(activeKeyTime != 0){
            activeKeyTime--;
        }
    }

    @Override
    public boolean emotes_onMouseClicked(double mouseX, double mouseY, int button){
        if(this.activeKeyTime != 0 && emoteList.getSelectedEntry() != null){
            return setKey(EmoteInstance.instance.getDefaults().getMouseKeyFromCode(button));
        }
        return false;
    }


    @Override
    public void emotes_renderScreen(MATRIX matrices, int mouseX, int mouseY, float delta){
        screen.emotesRenderBackgroundTexture(0);
        if(this.emoteList.getSelectedEntry() == null){
            this.setKeyButton.setActive(false);
            this.resetKey.setActive(false);
        }else{
            this.setKeyButton.setActive(true);
            this.resetKey.setActive(! this.emoteList.getSelectedEntry().getEmote().keyBinding.equals(EmoteInstance.instance.getDefaults().getUnknownKey()));
        }
        for(PositionedText str : texts){
            str.render(matrices);
        }
        this.emoteList.renderThis(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        this.fastMenu.render(matrices, mouseX, mouseY, delta);
        updateKeyText();
    }

    private boolean setKey(InputKey key){
        boolean bl = false;
        if(emoteList.getSelectedEntry() != null){
            bl = true;
            if(! applyKey(false, emoteList.getSelectedEntry().getEmote(), key)){
                EmoteInstance.instance.getClientMethods().openScreen(createConfigScreen(aBoolean -> confirmReturn(aBoolean, emoteList.getSelectedEntry().getEmote(), key), EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.sure"), EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.sure2")));
            }
        }
        return bl;
    }

    private void confirmReturn(boolean choice, EmoteHolder emoteHolder, InputKey key){
        if(choice){
            applyKey(true, emoteHolder, key);
            this.saveConfig();
        }
        screen.openThisScreen();
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


    @Override
    public void emotes_onRemove(){
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
        if(emoteList.getSelectedEntry() != null){
            Text message = emoteList.getSelectedEntry().getEmote().keyBinding.getLocalizedText();
            if(activeKeyTime != 0)
                //message = (new LiteralText("> ")).append(message.shallowCopy().formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW);
                message = EmoteInstance.instance.getDefaults().textFromString("> ").append(message).formatted(TextFormatting.YELLOW).append(" <").formatted(TextFormatting.YELLOW);
            setKeyButton.setMessage(message);
        }
    }

    @Override
    public boolean emotes_onKeyPressed(int keyCode, int scanCode, int mod){
        if(emoteList.getSelectedEntry() != null && activeKeyTime != 0) {
            if (keyCode == 256) {
                return setKey(EmoteInstance.instance.getDefaults().getUnknownKey());
            }
            else {
                return setKey(EmoteInstance.instance.getDefaults().getKeyFromCode(keyCode, scanCode));
            }
        }
        return false;
    }

    protected abstract IEmoteListWidgetHelper<MATRIX, WIDGET> newEmoteList(int width, int height);

    protected abstract class FastChooseWidget extends AbstractFastChooseWidget<MATRIX, WIDGET> {

        public FastChooseWidget(int x, int y, int size){
            super(x, y, size);
        }

        @Override
        protected boolean isValidClickButton(int button){
            return (button == 0 || button == 1) && activeKeyTime == 0;
        }

        @Override
        protected boolean EmotesOnClick(FastChooseElement element, int button){
            if(activeKeyTime != 0) return false;
            if(button == 1){
                element.clearEmote();
                save = true;
                return true;
            }else if(emoteList.getSelectedEntry() != null){
                element.setEmote(emoteList.getSelectedEntry().getEmote());
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
            drawCenteredText(matrixStack, this.str, this.x, this.y, MathHelper.colorHelper(255, 255, 255, 255));
            //textRenderer.getClass();
        }
    }
}
