package io.github.kosmx.emotes.main.screen;

import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
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
import java.nio.file.InvalidPathException;
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
                Path file = createFileName(emoteHolder, exportDir, format);
                OutputStream stream = Files.newOutputStream(file);
                UniversalEmoteSerializer.writeEmoteData(stream, emote, format);
                stream.close();

                if(format == EmoteFormat.JSON_EMOTECRAFT && emote.iconData != null){
                    Path iconPath = exportDir.resolve(file.getFileName().toString().substring(0, file.getFileName().toString().lastIndexOf(".")-1) + ".png");
                    if(iconPath.toFile().isFile()){
                        throw new IOException("File already exists: " + iconPath);
                    }
                    OutputStream iconStream = Files.newOutputStream(iconPath);
                    iconStream.write(AbstractNetworkInstance.safeGetBytesFromBuffer(emote.iconData));
                    iconStream.close();
                }
            }catch (IOException | EmoteSerializerException | InvalidPathException e) {
                e.printStackTrace();
                EmoteInstance.instance.getClientMethods().toastExportMessage(
                        EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.export.error." + format.getExtension()),
                        emoteHolder.name.getString());
            }
        }
        EmoteInstance.instance.getClientMethods().toastExportMessage(
                EmoteInstance.instance.getDefaults().newTranslationText("emotecraft.export.done." + format.getExtension()),
                "emotes/" + format.getExtension() + "_export/");
        EmoteInstance.instance.getLogger().log(Level.FINER, "All emotes are saved in " + format.getExtension() + " format", true);
    }

    private static Path createFileName(EmoteHolder emote, Path originPath, EmoteFormat format){
        String name = emote.name.getString();
        String finalName = null;
        while (finalName == null){
            try{
                originPath.resolve(name);
                finalName = name;
            }
            catch (InvalidPathException e){
                int i = e.getIndex();
                name = name.substring(0, i) + "#" + name.substring(i+1);
            }
        }
        int i = 2;
        Path file = originPath.resolve(finalName + "." + format.getExtension());
        while (file.toFile().isFile()){
            file = originPath.resolve(finalName + "_" + i++ + "." + format.getExtension());
        }
        return file;
    }

    @Override
    public void emotes_renderScreen(MATRIX matrices, int mouseX, int mouseY, float tickDelta) {
        screen.emotesRenderBackgroundTexture(0);
    }
}
