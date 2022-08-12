package io.github.kosmx.emotes.main.screen;

import dev.kosmx.playerAnim.core.util.MathHelper;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.executor.dataTypes.InputKey;
import io.github.kosmx.emotes.executor.dataTypes.Text;
import io.github.kosmx.emotes.executor.dataTypes.other.EmotesTextFormatting;
import io.github.kosmx.emotes.executor.dataTypes.screen.IConfirmScreen;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.IButton;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.ITextInputWidget;
import io.github.kosmx.emotes.executor.dataTypes.screen.widgets.IWidget;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.MainClientInit;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.ClientSerializer;
import io.github.kosmx.emotes.main.screen.widget.AbstractFastChooseWidget;
import io.github.kosmx.emotes.main.screen.widget.IChooseWheel;
import io.github.kosmx.emotes.main.screen.widget.IEmoteListWidgetHelper;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * The EmoteMenu
 * Override theses stuff
 * onKeyPressed
 * onMouseClicked
 * onClose - just open the parent
 * onRemove
 * tick()
 *
 */
@SuppressWarnings("unchecked")
public abstract class EmoteMenu<MATRIX, SCREEN, WIDGET> extends AbstractScreenLogic<MATRIX, SCREEN> {
    protected int activeKeyTime = 0;
    private IEmoteListWidgetHelper<MATRIX, WIDGET> emoteList;
    private FastChooseWidget fastMenu;
    //protected List<buttons> buttons is already exists
    private final Text unboundText = EmoteInstance.instance.getDefaults().getUnknownKey().getLocalizedText();
    private IButton setKeyButton;
    public boolean save = false;
    public boolean warn = false;
    private ITextInputWidget<MATRIX, ITextInputWidget> searchBox;
    private List<PositionedText> texts = new ArrayList<>();
    private IButton resetKey;

    private Text resetOneText = EmoteInstance.instance.getDefaults().newTranslationText("controls.reset");
    private Text resetAllText = EmoteInstance.instance.getDefaults().newTranslationText("controls.resetAll");
    private boolean resetOnlySelected;
    private int keyBoundEmotes = -1;

    private ChangeListener watcher = null;

    public EmoteMenu(IScreenSlave screen) {
        super(screen);
    }

    @Override
    public void emotes_initScreen(){
        if(warn && EmoteInstance.config.enableQuark.get()){
            warn = false;
            IConfirmScreen csr = createConfigScreen((bool)->{
                EmoteInstance.config.enableQuark.set(bool);
                screen.openThisScreen();
            }, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.quark"), EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.quark2"));
            EmoteInstance.instance.getClientMethods().openScreen(csr);
            csr.setTimeout(56);
        }

        this.texts = new ArrayList<>();
        MainClientInit.loadEmotes();

        try {
            watcher = new ChangeListener(EmoteInstance.instance.getExternalEmoteDir().toPath());
        }
        catch (IOException e){
            EmoteInstance.instance.getLogger().log(Level.WARNING, "can't watch emotes dir for changes: " +  e.getMessage());
            if(EmoteInstance.config.showDebug.get()){
                e.printStackTrace();
            }
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
        screen.addToButtons(newButton(screen.getWidth() - 200, 4, 96, 20, EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.export"), (button->openExportMenuScreen())));
        screen.addToButtons(newButton(screen.getWidth() / 2 + 10, screen.getHeight() - 30, 96, 20, EmoteInstance.instance.getDefaults().defaultTextsDone(), (button->screen.openParent())));
        setKeyButton = newButton(screen.getWidth() / 2 + 6, 60, 96, 20, unboundText, button->this.activateKey());
        screen.addToButtons(setKeyButton);
        resetKey = newButton(screen.getWidth() / 2 + 124, 60, 96, 20, EmoteInstance.instance.getDefaults().newTranslationText("controls.reset"), (this::resetKeyAction));
        screen.addToButtons(resetKey);
        emoteList.setEmotes(EmoteHolder.list, true);
        screen.setInitialFocus(this.searchBox);
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.keybind"), screen.getWidth() / 2 + 115, 40));
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.fastmenu"), screen.getWidth() / 2 + 10 + x / 2, screen.getHeight() / 2 - 54));
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.fastmenu2"), screen.getWidth() / 2 + 10 + x / 2, screen.getHeight() / 2 - 40));
        this.texts.add(new PositionedText(EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.options.fastmenu3"), screen.getWidth() / 2 + 10 + x / 2, screen.getHeight() / 2 - 26));
        screen.addButtonsToChildren();
    }

    protected abstract FastChooseWidget newFastChooseWidghet(int x, int y, int size);
    abstract public void openClothConfigScreen(); //will we use cloth or nope.
    abstract public void openExportMenuScreen();

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

    private void resetKeyAction(IButton button){
        if(resetOnlySelected) {
            if (emoteList.getSelectedEntry() == null) return;
            //emoteList.getSelectedEntry().getEmote().keyBinding = EmoteInstance.instance.getDefaults().getUnknownKey();
            ((ClientConfig)EmoteInstance.config).emoteKeyMap.removeL(emoteList.getSelectedEntry().getEmote().getUuid());
            keyBoundEmotes = -1;
            this.save = true;
        } else {
            EmoteInstance.instance.getClientMethods().openScreen(
                    createConfigScreen(
                            aBoolean -> {
                                if(aBoolean) {
                                    ((ClientConfig)EmoteInstance.config).emoteKeyMap.clear(); //reset :D
                                    this.saveConfig();
                                    keyBoundEmotes = -1;
                                }
                                screen.openThisScreen();
                            },
                            EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.resetAllKeys.title"),
                            EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.resetAllKeys.message").append(" (" + keyBoundEmotes + ")"))
            );
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
        if(watcher != null && watcher.isChanged()){
            reload();
        }
    }

    @Override
    public boolean emotes_onMouseClicked(double mouseX, double mouseY, int button){
        if(this.activeKeyTime != 0 && emoteList.getSelectedEntry() != null){
            return setKey(EmoteInstance.instance.getDefaults().getMouseKeyFromCode(button));
        }
        return false;
    }

    private void countEmotesWithKeyBind(){
        keyBoundEmotes = ((ClientConfig)EmoteInstance.config).emoteKeyMap.size();
    }

    @Override
    public void emotes_renderScreen(MATRIX matrices, int mouseX, int mouseY, float delta){
        screen.emotesRenderBackgroundTexture(0);
        if(this.emoteList.getSelectedEntry() == null){
            this.setKeyButton.setActive(false);
            //this.resetKey.setActive(false);
            resetOnlySelected = false;
        }else{
            this.setKeyButton.setActive(true);
            //this.resetKey.setActive(! this.emoteList.getSelectedEntry().getEmote().keyBinding.equals(EmoteInstance.instance.getDefaults().getUnknownKey()));
            resetOnlySelected = ((ClientConfig)EmoteInstance.config).emoteKeyMap.containsL(this.emoteList.getSelectedEntry().getEmote().getUuid());
        }
        if(resetOnlySelected){
            this.resetKey.setActive(true);
            this.resetKey.setMessage(resetOneText);
        }
        else {
            if(keyBoundEmotes < 0) countEmotesWithKeyBind();
            if(keyBoundEmotes > 0){
                this.resetKey.setActive(true);
                this.resetKey.setMessage(resetAllText.copyIt().append(" (" + keyBoundEmotes + ")"));
            }
            else {
                this.resetKey.setActive(false);
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
            if(! key.equals(EmoteInstance.instance.getDefaults().getUnknownKey()) && getKey(emoteHolder.getUuid()).equals(key)){
                bl = false;
                if(force){
                    //emoteHolder.keyBinding = EmoteInstance.instance.getDefaults().getUnknownKey();
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
    public static InputKey getKey(UUID emoteID){
        InputKey key;
        if((key = ((ClientConfig)EmoteInstance.config).emoteKeyMap.getR(emoteID)) == null){
            return EmoteInstance.instance.getDefaults().getUnknownKey();
        }
        return key;
    }

    @Override
    public void emotes_onRemove(){
        if(save){
            this.saveConfig();
        }
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
            Text message = getKey(emoteList.getSelectedEntry().getEmote().getUuid()).getLocalizedText();
            if(activeKeyTime != 0)
                //message = (new LiteralText("> ")).append(message.shallowCopy().formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW);
                message = EmoteInstance.instance.getDefaults().textFromString("> ").append(message).formatted(EmotesTextFormatting.YELLOW).append(" <").formatted(EmotesTextFormatting.YELLOW);
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

    @Override
    public void emotes_filesDropped(List<Path> paths){
        addEmotes(paths);
        List<Path> folders = paths.stream().filter(path -> path.toFile().isDirectory()).collect(Collectors.toList());
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
                            path.toFile().getName().endsWith(".emote") && ((ClientConfig)EmoteInstance.config).enableQuark.get())){
                return true; //can be an emote icon
            }
            try {
                return UniversalEmoteSerializer.readData(Files.newInputStream(path), path.getFileName().toString()).size() != 0;
            }
            catch (Exception e){
                return false; //what is this file
            }
        }).collect(Collectors.toList());

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
        protected boolean EmotesOnClick(IChooseWheel.IChooseElement element, int button){
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

    private static class ChangeListener implements AutoCloseable{
        private final WatchService watcher;
        private final Path path;

        ChangeListener(Path path) throws IOException{
            this.watcher = path.getFileSystem().newWatchService();
            this.path = path;
            this.path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        boolean isChanged(){
            boolean bl = false;
            WatchKey key;
            if((key = watcher.poll()) != null){
                bl = key.pollEvents().size() != 0;//there is something...
                key.reset();
            }
            return bl;
        }

        @Override
        public void close() throws Exception {
            watcher.close();
        }
    }
}
