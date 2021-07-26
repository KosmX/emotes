package io.github.kosmx.emotes.main.screen;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.emote.EmoteFormat;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.server.serializer.type.EmoteSerializerException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public abstract class ExportMenu<MATRIX, SCREEN> extends AbstractScreenLogic<MATRIX, SCREEN> {
    protected ExportMenu(IScreenSlave screen) {
        super(screen);
    }

    @Override
    public void emotes_initScreen() {
        int h = 10;
        screen.addToButtons(newButton(screen.getWidth() / 2 - 75, h += 30, 200, 20,
                EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.exportjson"), //TODO translation key
                iButton -> {
                    this.saveAllJson();
                }));
        screen.addToButtons(newButton(screen.getWidth() / 2 - 75, h += 30, 200, 20,
                EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.exportbin"), //TODO translation key
                iButton -> {
                    this.saveAllBinary();
                }));

        //TODO toast notification
        screen.addToButtons(newButton(screen.getWidth() / 2 + 10, screen.getHeight() - 30, 96, 20, EmoteInstance.instance.getDefaults().defaultTextsDone(), (button->screen.openParent())));
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
            if(emote.isBuiltin && !((ClientConfig)EmoteInstance.config).exportBuiltin.get()){
                continue;
            }
            EmoteInstance.instance.getLogger().log(Level.FINER, "Saving " + emoteHolder.name.getString() + " into " + format.getExtension());
            try{
                Path exportDir = EmoteInstance.instance.getExternalEmoteDir().toPath().resolve(format.getExtension() + "_export");
                if(!exportDir.toFile().isDirectory()){
                    Files.createDirectories(exportDir);
                }
                Path file = exportDir.resolve(emoteHolder.name.getString() + "." + format.getExtension());
                int i = 2;
                while (file.toFile().isFile()){
                    file = exportDir.resolve(emoteHolder.name.getString() + "_" + i++ + "." + format.getExtension());
                }
                OutputStream stream = Files.newOutputStream(file);
                UniversalEmoteSerializer.writeEmoteData(stream, emote, format);
                stream.close();
            }catch (IOException | EmoteSerializerException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void emotes_renderScreen(MATRIX matrices, int mouseX, int mouseY, float tickDelta) {
        screen.emotesRenderBackgroundTexture(0);
    }
}
