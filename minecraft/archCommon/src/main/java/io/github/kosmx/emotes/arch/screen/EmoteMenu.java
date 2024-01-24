package io.github.kosmx.emotes.arch.screen;

import com.mojang.blaze3d.platform.InputConstants;
import dev.kosmx.playerAnim.core.util.MathHelper;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.screen.ConfigScreen;
import io.github.kosmx.emotes.arch.gui.widgets.AbstractEmoteListWidget;
import io.github.kosmx.emotes.arch.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.arch.screen.widget.IChooseWheel;
import io.github.kosmx.emotes.arch.screen.widget.IWidgetLogic;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.MainClientInit;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.ClientSerializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * The EmoteMenu
 * Override these stuff
 * onKeyPressed
 * onMouseClicked
 * onClose - just open the parent
 * onRemove
 * tick()
 *
 */
public class EmoteMenu extends EmoteConfigScreen {
    protected int activeKeyTime = 0;
    private AbstractEmoteListWidget<?> emoteList;
    private FastChooseWidget fastMenu;
    //protected List<buttons> buttons is already exists
    private final MutableComponent unboundText = InputConstants.UNKNOWN.getDisplayName().plainCopy();
    private Button setKeyButton;
    private boolean save = false;
    private EditBox searchBox;
    private List<PositionedText> texts = new ArrayList<>();
    private Button resetKey;

    private final Component resetOneText = Component.translatable("controls.reset");
    private final Component resetAllText = Component.translatable("controls.resetAll");
    private boolean resetOnlySelected;
    private int keyBoundEmotes = -1;

    private ChangeListener watcher = null;

    public EmoteMenu(@org.jetbrains.annotations.Nullable Screen parent) {
        super(Component.translatable("emotecraft.menu"), parent);
    }

    @Override
    public void init(){

        this.texts = new ArrayList<>();
        MainClientInit.loadEmotes();

        try {
            if (watcher == null) {
                watcher = new ChangeListener(EmoteInstance.instance.getExternalEmoteDir().toPath());
            }
        }
        catch (IOException e){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "can't watch emotes dir for changes: " +  e.getMessage());
            if(EmoteInstance.config.showDebug.get()){
                e.printStackTrace();
            }
        }

        int x1 = this.getWidth() / 2 - (int) (getWidth() / 2.2 - 16) - 12;
        int width1 = (int) (getWidth() / 2.2 - 16);
        Component title1 = Component.translatable("emotecraft.search");
        this.searchBox = new EditBox(Minecraft.getInstance().font, x1, 12, width1, 20, title1);

        this.searchBox.setResponder((string)-> this.emoteList.filter(string::toLowerCase));
        this.addRenderableWidget(searchBox);

        int x7 = this.getWidth() / 2 - 154;
        int y1 = this.getHeight() - 30;
        Component msg4 = Component.translatable("emotecraft.openFolder");
        this.addRenderableWidget(Button.builder(msg4, ((Consumer<Button>) (buttonWidget) -> PlatformTools.openExternalEmotesDir())::accept).pos(x7, y1).size(150, 20).build());

        //this.emoteList = new EmoteListWidget(this.client, (int) (this.getHeight() / 2.2 - 16), this.getHeight(), this);
        this.emoteList = newEmoteList((int) (getWidth()/2.2-16), this.getHeight());
        this.emoteList.emotesSetLeftPos( getWidth() / 2 - (int) ( getWidth() / 2.2 - 16) - 12);
        this.addToChildren(this.emoteList);
        int x = Math.min( getWidth() / 4, (int) ( getHeight() / 2.5));
        this.fastMenu = newFastChooseWidghet( getWidth() / 2 + 2, this.getHeight() / 2 - 8, x - 7);
        this.addToChildren(fastMenu);
        int x6 = this.getWidth() - 100;
        Component msg3 = Component.translatable("emotecraft.options.options");
        this.addRenderableWidget(Button.builder(msg3, (button3 -> openClothConfigScreen())).pos(x6, 4).size(96, 20).build());
        int x5 = this.getWidth() - 200;
        Component msg2 = Component.translatable("emotecraft.options.export");
        this.addRenderableWidget(Button.builder(msg2, (button2 -> openExportMenuScreen())).pos(x5, 4).size(96, 20).build());
        int x4 = this.getWidth() / 2 + 10;
        int y = this.getHeight() - 30;
        Component msg1 = CommonComponents.GUI_DONE;
        this.addRenderableWidget(Button.builder(msg1, (button1 -> this.openParent())).pos(x4, y).size(96, 20).build());
        int x3 = this.getWidth() / 2 + 6;
        setKeyButton = Button.builder(unboundText, ((Consumer<Button>) button -> this.activateKey())::accept).pos(x3, 60).size(96, 20).build();
        this.addRenderableWidget(setKeyButton);
        int x2 = this.getWidth() / 2 + 124;
        Component msg = Component.translatable("controls.reset");
        resetKey = Button.builder(msg, (this::resetKeyAction)).pos(x2, 60).size(96, 20).build();
        this.addRenderableWidget(resetKey);
        emoteList.setEmotes(EmoteHolder.list, true);
        this.setInitialFocus(this.searchBox);
        this.texts.add(new PositionedText("emotecraft.options.keybind", this.getWidth() / 2 + 115, 40));
        this.texts.add(new PositionedText(Component.translatable("emotecraft.options.fastmenu"), this.getWidth() / 2 + 10 + x / 2, this.getHeight() / 2 - 54));
        this.texts.add(new PositionedText(Component.translatable("emotecraft.options.fastmenu2"), this.getWidth() / 2 + 10 + x / 2, this.getHeight() / 2 - 40));
        this.texts.add(new PositionedText(Component.translatable("emotecraft.options.fastmenu3"), this.getWidth() / 2 + 10 + x / 2, this.getHeight() / 2 - 26));
    }

    protected FastChooseWidget newFastChooseWidghet(int x, int y, int size) {
        return new FastMenuImpl(x, y, size);
    }

    public void openClothConfigScreen() {
        Minecraft.getInstance().setScreen(new ConfigScreen(this));
    }

    public void openExportMenuScreen() {
        Minecraft.getInstance().setScreen(new ExportMenu(this));
    }

    private void activateKey(){
        if(emoteList.getSelectedEntry() != null){
            this.setFocusedElement(setKeyButton);
            activeKeyTime = 200;
        }
    }

    public void setFocusedElement(@Nullable GuiEventListener focused){
        if(activeKeyTime == 0){
            this.setFocused(focused);
        }
    }

    private void resetKeyAction(Button button){
        if(resetOnlySelected) {
            if (emoteList.getSelectedEntry() == null) return;
            //emoteList.getSelectedEntry().getEmote().keyBinding = TmpGetters.getDefaults().getUnknownKey();
            ((ClientConfig)EmoteInstance.config).emoteKeyMap.removeL(emoteList.getSelectedEntry().getEmote().getUuid());
            keyBoundEmotes = -1;
            this.save = true;
        } else {
            Component text = Component.translatable("emotecraft.resetAllKeys.message").append(" (" + keyBoundEmotes + ")");
            //reset :D
            Minecraft.getInstance().setScreen(
                    new ConfirmScreen(((Consumer<Boolean>) aBoolean -> {
                        if (aBoolean) {
                            ((ClientConfig) EmoteInstance.config).emoteKeyMap.clear(); //reset :D
                            this.saveConfig();
                            keyBoundEmotes = -1;
                        }
                        getMinecraft().setScreen(EmoteMenu.this);
                    })::accept, Component.translatable("emotecraft.resetAllKeys.title"), text)
            );
        }
    }

    @Override
    public void tick(){
        if(activeKeyTime == 1){
            setFocusedElement(null);
        }
        if(activeKeyTime != 0){
            activeKeyTime--;
        }
        if(watcher != null && watcher.isChanged()){
            reload();
        }
        super.tick();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(this.activeKeyTime != 0 && emoteList.getSelectedEntry() != null){
            return setKey(InputConstants.Type.MOUSE.getOrCreate(button));
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void countEmotesWithKeyBind(){
        keyBoundEmotes = ((ClientConfig)EmoteInstance.config).emoteKeyMap.size();
    }

    @Override
    public void render(@NotNull GuiGraphics matrices, int mouseX, int mouseY, float delta){
        this.renderDirtBackground(matrices);
        if(this.emoteList.getSelectedEntry() == null){
            this.setKeyButton.active = false;
            //this.resetKey.setActive(false);
            resetOnlySelected = false;
        }else{
            this.setKeyButton.active = true;
            //this.resetKey.setActive(! this.emoteList.getSelectedEntry().getEmote().keyBinding.equals(TmpGetters.getDefaults().getUnknownKey()));
            resetOnlySelected = ((ClientConfig)EmoteInstance.config).emoteKeyMap.containsL(this.emoteList.getSelectedEntry().getEmote().getUuid());
        }
        if(resetOnlySelected){
            this.resetKey.active = true;
            this.resetKey.setMessage(resetOneText);
        }
        else {
            if(keyBoundEmotes < 0) countEmotesWithKeyBind();
            if(keyBoundEmotes > 0){
                this.resetKey.active = true;
                this.resetKey.setMessage(resetAllText.copy().append(" (" + keyBoundEmotes + ")"));
            }
            else {
                this.resetKey.active = false;
                this.resetKey.setMessage(resetOneText);
            }
        }
        for(PositionedText str : texts){
            str.render(matrices);
        }
        this.emoteList.renderThis(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);
        this.fastMenu.render(matrices, mouseX, mouseY, delta);
        updateKeyText();
        super.render(matrices, mouseX, mouseY, delta);
    }

    private boolean setKey(InputConstants.Key key){
        boolean bl = false;
        if(emoteList.getSelectedEntry() != null){
            bl = true;
            if(! applyKey(false, emoteList.getSelectedEntry().getEmote(), key)){
                Component title1 = Component.translatable("emotecraft.sure");
                Component text = Component.translatable("emotecraft.sure2");
                Screen confirmScreen = new ConfirmScreen(((Consumer<Boolean>) aBoolean -> confirmReturn(aBoolean, emoteList.getSelectedEntry().getEmote(), key))::accept, title1, text);
                Minecraft.getInstance().setScreen(confirmScreen);
            }
        }
        return bl;
    }

    private void confirmReturn(boolean choice, EmoteHolder emoteHolder, InputConstants.Key key){
        if(choice){
            applyKey(true, emoteHolder, key);
            this.saveConfig();
        }
        getMinecraft().setScreen(this);
    }

    private boolean applyKey(boolean force, EmoteHolder emote, InputConstants.Key key){
        boolean bl = true;
        for(EmoteHolder emoteHolder : EmoteHolder.list){
            if(! key.equals(InputConstants.UNKNOWN) && getKey(emoteHolder.getUuid()).equals(key)){
                bl = false;
                if(force){
                    //emoteHolder.keyBinding = TmpGetters.getDefaults().getUnknownKey();
                    ((ClientConfig)EmoteInstance.config).emoteKeyMap.removeL(emoteHolder.getUuid());
                }
            }
        }
        if(bl || force){
            ((ClientConfig)EmoteInstance.config).emoteKeyMap.put(emote.getUuid(), key);
            this.save = true;
            keyBoundEmotes = -1; //recount needed;
        }
        this.activeKeyTime = 0;
        return bl;
    }

    @Nonnull
    public static InputConstants.Key getKey(UUID emoteID){
        InputConstants.Key key;
        if((key = ((ClientConfig)EmoteInstance.config).emoteKeyMap.getR(emoteID)) == null){
            return InputConstants.UNKNOWN;
        }
        return key;
    }

    @Override
    public void removed(){
        if(save){
            this.saveConfig();
        }
        watcher.close();
    }

    private void saveConfig(){
        ClientSerializer.saveConfig();
    }

    private void reload(){
        if(this.save){
            saveConfig();
        }
        MainClientInit.loadEmotes();
        emoteList.setEmotes(EmoteHolder.list, true);
    }

    private void updateKeyText(){
        if(emoteList.getSelectedEntry() != null){
            Component message = getKey(emoteList.getSelectedEntry().getEmote().getUuid()).getDisplayName();
            if(activeKeyTime != 0)
                //message = (new LiteralText("> ")).append(message.shallowCopy().formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW);
                message = Component.literal("> ").append(message).withStyle(ChatFormatting.YELLOW).append(" <").withStyle(ChatFormatting.YELLOW);
            setKeyButton.setMessage(message);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int mod){
        if(emoteList.getSelectedEntry() != null && activeKeyTime != 0) {
            if (keyCode == 256) {
                return setKey(InputConstants.UNKNOWN);
            }
            else {
                return setKey(InputConstants.getKey(keyCode, scanCode));
            }
        }
        return super.keyPressed(keyCode, scanCode, mod);
    }

    @Override
    public void onFilesDrop(@NotNull List<Path> paths){
        addEmotes(paths);
        List<Path> folders = paths.stream().filter(path -> path.toFile().isDirectory()).toList();
        for(Path folder : folders){
            List<Path> collect = new ArrayList<>();
            Arrays.stream(Objects.requireNonNull(folder.toFile().listFiles((dir, name) -> name.endsWith(".json")||name.endsWith(".png")))).forEach(file -> collect.add(file.toPath()));
            addEmotes(collect);
        }
    }

    private void addEmotes(List<Path> emotes){

        List<Path> newEmotes = emotes.stream().filter(path -> {
            if(path.toFile().isFile() &&
                    ( path.toFile().getName().endsWith(".png") ||
                            path.toFile().getName().endsWith(".emote") && EmoteInstance.config.enableQuark.get())){
                return true; //can be an emote icon
            }
            try {
                return !UniversalEmoteSerializer.readData(Files.newInputStream(path), path.getFileName().toString()).isEmpty();
            }
            catch (Exception e){
                return false; //what is this file
            }
        }).toList();

        Path emotesDir = EmoteInstance.instance.getExternalEmoteDir().toPath();
        for(Path path:newEmotes){
            try{
                Files.copy(path, emotesDir.resolve(path.getFileName()));
            }
            catch (IOException e){
                if(e instanceof FileAlreadyExistsException){
                    EmoteInstance.instance.getLogger().log(Level.INFO, path.getFileName() + " is already in the emotes directory", true);
                }else {
                    EmoteInstance.instance.getLogger().log(Level.FINEST, "Unknown error while copying " + path.getFileName() + ": " + e.getMessage(), true);
                    if(EmoteInstance.config.showDebug.get()){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected AbstractEmoteListWidget<?> newEmoteList(int width, int height) {
        return new EmoteListImpl(getMinecraft(), width, height, 51, height-32, 36, this);
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    protected abstract class FastChooseWidget extends AbstractFastChooseWidget {

        public FastChooseWidget(int x, int y, int size){
            super(x, y, size);
        }

        @Override
        protected boolean isValidClickButton(int button){
            return (button == 0 || button == 1) && activeKeyTime == 0;
        }

        @Override
        protected boolean onClick(IChooseWheel.IChooseElement element, int button){
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
        protected boolean doHoverPart(IChooseWheel.IChooseElement part){
            return activeKeyTime == 0;
        }

        @Override
        protected boolean doesShowInvalid() {
            return true;
        }
    }

    private static class PositionedText {
        private final Component str;
        private final int x;
        private final int y;

        private PositionedText(Component str, int x, int y) {
            this.str = str;
            this.x = x;
            this.y = y;
        }

        private PositionedText(String str, int x, int y) {
            this.str = Component.translatable(str);
            this.x = x;
            this.y = y;
        }

        private void render(GuiGraphics matrixStack) {
            matrixStack.drawCenteredString(Minecraft.getInstance().font, this.str, this.x, this.y, MathHelper.colorHelper(255, 255, 255, 255));
            //textRenderer.getClass();
        }
    }

    private static class ChangeListener implements AutoCloseable{
        private final WatchService watcher;

        ChangeListener(Path path) throws IOException{
            this.watcher = path.getFileSystem().newWatchService();
            path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        boolean isChanged(){
            boolean bl = false;
            WatchKey key;
            if((key = watcher.poll()) != null){
                bl = !key.pollEvents().isEmpty();//there is something...
                key.reset();
            }
            return bl;
        }

        @Override
        public void close() {
            try {
                watcher.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static class EmoteListImpl extends AbstractEmoteListWidget<EmoteListImpl.EmoteListEntryImpl> {

        public EmoteListImpl(Minecraft minecraftClient, int i, int j, int k, int l, int m, Screen screen) {
            super(minecraftClient, i, j, k, l, m, screen);

        }

        @Override
        protected EmoteListEntryImpl newEmoteEntry(Minecraft client, EmoteHolder emoteHolder) {
            return new EmoteListEntryImpl(client, emoteHolder);
        }


        public class EmoteListEntryImpl extends AbstractEmoteListWidget.AbstractEmoteEntry<EmoteListEntryImpl>{

            public EmoteListEntryImpl(Minecraft client, EmoteHolder emote) {
                super(client, emote);
            }

            @Override
            protected void onPressed() {
                EmoteListImpl.this.setSelected(this);
            }
        }
    }

    public class FastMenuImpl extends EmoteMenu.FastChooseWidget implements IWidgetLogic {
        private boolean focused = true;

        public FastMenuImpl(int x, int y, int size) {
            super(x, y, size);
        }

        @Override
        public void setFocused(boolean bl) {
            focused = bl;
        }

        @Override
        public boolean isFocused() {
            return focused;
        }
    }
}
