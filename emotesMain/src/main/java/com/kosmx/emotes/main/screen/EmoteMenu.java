package com.kosmx.emotes.main.screen;

import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.executor.dataTypes.Text;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import com.kosmx.emotes.main.EmoteHolder;
import com.kosmx.emotes.main.config.ClientConfig;
import com.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import com.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import com.kosmx.emotes.main.config.Serializer;

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
 */
public abstract class EmoteMenu extends AbstractScreenLogic {
    private int activeKeyTime = 0;
    private EmoteListWidget emoteList;
    private FastChooseWidget fastMenu;
    //protected List<buttons> buttons is already exists
    private static final Text unboundText = EmoteInstance.instance.getDefaults().getUnknownKey().getLocalizedText();
    private IButton setKeyButton;
    public boolean save = false;
    public boolean warn = false;
    private ITextInputWidget searchBox;
    private List<PositionedText> texts = new ArrayList<>();
    private ButtonWidget resetKey;

    public boolean exportGeckoEmotes = false;


    @Override
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

        this.searchBox.setChangedListener((string)->this.emoteList.filter(string::toLowerCase));
        this.children.add(searchBox);

        this.buttons.add(new ButtonWidget(this.getHeight() / 2 - 154, this.getHeight() - 30, 150, 20, new TranslatableText("emotecraft.openFolder"), (buttonWidget)->Util.getOperatingSystem().open(Client.externalEmotes)));

        this.emoteList = new EmoteListWidget(this.client, (int) (this.getHeight() / 2.2 - 16), this.getHeight(), this);
        this.emoteList.setLeftPos(this.getHeight() / 2 - (int) (this.getHeight() / 2.2 - 16) - 12);
        this.children.add(this.emoteList);
        int x = Math.min(this.getHeight() / 4, (int) (this.getHeight() / 2.5));
        this.fastMenu = new FastChooseWidget(this.getHeight() / 2 + 2, this.getHeight() / 2 - 8, x - 7);
        this.children.add(fastMenu);
        this.buttons.add(new ButtonWidget(this.getHeight() - 100, 4, 96, 20, new TranslatableText("emotecraft.options.options"), (button->this.client.openScreen(ClothConfigScreen.getConfigScreen(this)))));
        this.buttons.add(new ButtonWidget(this.getHeight() / 2 + 10, this.getHeight() - 30, 96, 20, ScreenTexts.DONE, (button->this.client.openScreen(this.parent))));
        setKeyButton = new ButtonWidget(this.getHeight() / 2 + 6, 60, 96, 20, unboundText, button->this.activateKey());
        this.buttons.add(setKeyButton);
        resetKey = new ButtonWidget(this.getHeight() / 2 + 124, 60, 96, 20, new TranslatableText("controls.reset"), (button->{
            if(emoteList.getSelected() != null){
                emoteList.getSelected().emote.keyBinding = InputUtil.UNKNOWN_KEY;
                this.save = true;
            }
        }));
        this.buttons.add(resetKey);
        emoteList.setEmotes(EmoteHolder.list);
        this.children.addAll(buttons);
        super.init();
        this.setInitialFocus(this.searchBox);
        this.texts.add(new PositionedText(new TranslatableText("emotecraft.options.keybind"), this.getHeight() / 2 + 115, 40));
        this.texts.add(new PositionedText(new TranslatableText("emotecraft.options.fastmenu"), this.getHeight() / 2 + 10 + x / 2, height / 2 - 54));
        this.texts.add(new PositionedText(new TranslatableText("emotecraft.options.fastmenu2"), this.getHeight() / 2 + 10 + x / 2, height / 2 - 40));
        this.texts.add(new PositionedText(new TranslatableText("emotecraft.options.fastmenu3"), this.getHeight() / 2 + 10 + x / 2, height / 2 - 26));
    }

    private void activateKey(){
        if(emoteList.getSelected() != null){
            this.setFocused(setKeyButton);
            activeKeyTime = 200;
        }
    }

    @Override
    public void setFocused(@Nullable Element focused){
        if(activeKeyTime == 0) super.setFocused(focused);
    }

    @Override
    public void tick(){
        super.tick();
        if(activeKeyTime == 1){
            setFocused(null);
        }
        if(activeKeyTime != 0){
            activeKeyTime--;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(this.activeKeyTime != 0 && emoteList.getSelected() != null){
            return setKey(InputUtil.Type.MOUSE.createFromCode(button));
        }else return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
        this.renderBackgroundTexture(0);
        if(this.emoteList.getSelected() == null){
            this.setKeyButton.active = false;
            this.resetKey.active = false;
        }else{
            this.setKeyButton.active = true;
            this.resetKey.active = ! this.emoteList.getSelected().emote.keyBinding.equals(InputUtil.UNKNOWN_KEY);
        }
        for(PositionedText str : texts){
            str.render(matrices, textRenderer);
        }
        this.emoteList.render(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        this.fastMenu.render(matrices, mouseX, mouseY, delta);
        updateKeyText();
        super.render(matrices, mouseX, mouseY, delta);
    }

    private boolean setKey(InputUtil.Key key){
        boolean bl = false;
        if(emoteList.getSelected() != null){
            bl = true;
            if(! applyKey(false, emoteList.getSelected().emote, key)){
                this.client.openScreen(new ConfirmScreen((bool)->confirmReturn(bool, emoteList.getSelected().emote, key), new TranslatableText("emotecraft.sure"), new TranslatableText("emotecraft.sure2")));
            }
        }
        return bl;
    }

    private void confirmReturn(boolean choice, EmoteHolder emoteHolder, InputUtil.Key key){
        if(choice){
            applyKey(true, emoteHolder, key);
            this.saveConfig();
        }
        this.client.openScreen(this);
    }

    private boolean applyKey(boolean force, EmoteHolder emote, InputUtil.Key key){
        boolean bl = true;
        for(EmoteHolder emoteHolder : EmoteHolder.list){
            if(! key.equals(InputUtil.UNKNOWN_KEY) && emoteHolder.keyBinding.equals(key)){
                bl = false;
                if(force){
                    emoteHolder.keyBinding = InputUtil.UNKNOWN_KEY;
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
    public void onClose(){
        this.client.openScreen(this.parent);
    }

    @Override
    public void removed(){
        if(save){
            this.saveConfig();
        }
        super.removed();
    }

    private void saveConfig(){
        EmoteHolder.bindKeys(Main.config);
        try{
            BufferedWriter writer = Files.newBufferedWriter(Main.CONFIGPATH);
            Serializer.serializer.toJson(Main.config, writer);
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
                message = (new LiteralText("> ")).append(message.shallowCopy().formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW);
            setKeyButton.setMessage(message);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int mod){
        if(emoteList.getSelected() != null && activeKeyTime != 0){
            if(keyCode == 256){
                return setKey(InputUtil.UNKNOWN_KEY);
            }else{
                return setKey(InputUtil.fromKeyCode(keyCode, scanCode));
            }
        }else{
            return super.keyPressed(keyCode, scanCode, mod);
        }
    }


    public static class EmoteListWidget extends AbstractEmoteListWidget<EmoteListWidget.EmoteListEntry> {
        public EmoteListWidget(MinecraftClient minecraftClient, int width, int height, Screen screen){
            super(minecraftClient, width, height , 51, height - 32, 36, screen);
        }

        @Override
        public void setEmotes(List<EmoteHolder> list){
            for(EmoteHolder emote : list){
                this.emotes.add(new EmoteListEntry(this.client, emote));
            }
            filter(()->"");
        }

        public class EmoteListEntry extends AbstractEmoteListWidget.AbstractEmoteEntry<EmoteListEntry> {
            public EmoteListEntry(MinecraftClient client, EmoteHolder emote){
                super(client, emote);
            }

            protected void onPressed(){        //setup screen -> select pack, play screen -> play
                EmoteListWidget.this.setSelected(this);
            }
        }
    }

    private class FastChooseWidget extends AbstractFastChooseWidget {

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

    private static class PositionedText {
        private final Text str;
        private final int x;
        private final int y;

        private PositionedText(Text str, int x, int y){
            this.str = str;
            this.x = x;
            this.y = y;
        }

        private void render(MatrixStack matrixStack, TextRenderer textRenderer){
            drawCenteredText(matrixStack, textRenderer, this.str, this.x, this.y, Helper.colorHelper(255, 255, 255, 255));
            textRenderer.getClass();
        }
}
