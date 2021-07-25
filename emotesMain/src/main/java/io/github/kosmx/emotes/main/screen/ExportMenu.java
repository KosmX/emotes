package io.github.kosmx.emotes.main.screen;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.emote.EmoteFormat;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.server.serializer.type.EmoteSerializerException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
        for(EmoteHolder emoteHolder:EmoteHolder.list){
            EmoteData emote = emoteHolder.getEmote();
            try{
                Path exportDir = EmoteInstance.instance.getExternalEmoteDir().toPath().resolve(format.getExtension() + "_export");
                OutputStream stream = Files.newOutputStream(exportDir.resolve(emote.name + "." + format.getExtension()));
                UniversalEmoteSerializer.writeEmoteData(stream, emote, format);
                stream.close();
            }catch (IOException | EmoteSerializerException e){
                e.printStackTrace();
            }
        }
    }
}
