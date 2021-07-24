package io.github.kosmx.emotes.main.screen;

import io.github.kosmx.emotes.common.emote.EmoteFormat;
import io.github.kosmx.emotes.executor.EmoteInstance;

public abstract class ExportMenu<MATRIX, SCREEN> extends AbstractScreenLogic<MATRIX, SCREEN> {
    protected ExportMenu(IScreenSlave screen) {
        super(screen);
    }

    @Override
    public void emotes_initScreen() {
        int h = 10;
        screen.addToButtons(newButton(screen.getWidth() / 2 - 75, h += 30, 150, 20,
                EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.exportjson"), //TODO translation key
                iButton -> {
                    this.saveAllJson();
                }));
        screen.addToButtons(newButton(screen.getWidth() / 2 - 75, h += 30, 150, 20,
                EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.exportbin"), //TODO translation key
                iButton -> {
                    this.saveAllBinary();
                }));
    }

    private void saveAllJson(){
        exportEmotesInFormat(EmoteFormat.JSON_EMOTECRAFT);
    }
    private void saveAllBinary(){
        exportEmotesInFormat(EmoteFormat.BINARY);
    }
    private void exportEmotesInFormat(EmoteFormat format){
        //TODO
    }
}
