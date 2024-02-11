package io.github.kosmx.emotes.arch.screen;

import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.inline.TmpGetters;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.server.serializer.type.EmoteSerializerException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.logging.Level;

public class ExportMenu extends EmoteConfigScreen {
    protected ExportMenu(Screen screen) {
        super(Component.translatable("emotecraft.exportMenu"), screen);
    }

    @Override
    public void init() {
        int h = 10;
        int x3 = getWidth() / 2 - 100;
        int y3 = h += 30;
        Component msg3 = Component.translatable("emotecraft.exportjson");
        addRenderableWidget(Button.builder(msg3, (iButton1 -> this.saveAllJson())).pos(x3, y3).size(200, 20).build());
        int x2 = getWidth() / 2 - 100;
        int y2 = h += 30;
        Component msg2 = Component.translatable("emotecraft.exportbin");
        addRenderableWidget(Button.builder(msg2, (iButton -> this.saveAllBinary())).pos(x2, y2).size(200, 20).build());

        //TODO toast notification
        int x1 = getWidth() / 2 + 10;
        int y1 = getHeight() - 30;
        Component msg1 = CommonComponents.GUI_DONE;
        addRenderableWidget(Button.builder(msg1, (button -> openParent())).pos(x1, y1).size(96, 20).build());
        int x = getWidth() / 2 - 154;
        int y = getHeight() - 30;
        Component msg = Component.translatable("emotecraft.openFolder");
        addRenderableWidget(Button.builder(msg, ((Consumer<Button>) (buttonWidget) -> PlatformTools.openExternalEmotesDir())::accept).pos(x, y).size(150, 20).build());
    }

    private void saveAllJson(){
        exportEmotesInFormat(AnimationFormat.JSON_EMOTECRAFT);
    }
    private void saveAllBinary(){
        exportEmotesInFormat(AnimationFormat.BINARY);
    }
    private void exportEmotesInFormat(AnimationFormat format){
        for(EmoteHolder emoteHolder:EmoteHolder.list){
            KeyframeAnimation emote = emoteHolder.getEmote();
            if(emote.extraData.containsKey("isBuiltin") && !((ClientConfig)EmoteInstance.config).exportBuiltin.get()){
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
                UniversalEmoteSerializer.writeKeyframeAnimation(stream, emote, format);
                stream.close();

                if(format == AnimationFormat.JSON_EMOTECRAFT && emote.extraData.containsKey("iconData")){
                    Path iconPath = exportDir.resolve(file.getFileName().toString().substring(0, file.getFileName().toString().lastIndexOf(".")) + ".png");
                    if(iconPath.toFile().isFile()){
                        throw new IOException("File already exists: " + iconPath);
                    }
                    OutputStream iconStream = Files.newOutputStream(iconPath);
                    iconStream.write(AbstractNetworkInstance.safeGetBytesFromBuffer((ByteBuffer) emote.extraData.get("iconData")));
                    iconStream.close();
                }
            }catch (IOException | EmoteSerializerException | InvalidPathException e) {
                EmoteInstance.instance.getLogger().log(Level.WARNING, e.getMessage(), e);
                TmpGetters.getClientMethods().toastExportMessage( 2,
                        Component.translatable("emotecraft.export.error." + format.getExtension()),
                        emoteHolder.name.getString());
            }
        }
        TmpGetters.getClientMethods().toastExportMessage(1,
                Component.translatable("emotecraft.export.done." + format.getExtension()),
                "emotes/" + format.getExtension() + "_export/");
        EmoteInstance.instance.getLogger().log(Level.FINER, "All emotes are saved in " + format.getExtension() + " format", true);
    }

    private static Path createFileName(EmoteHolder emote, Path originPath, AnimationFormat format){
        String name = emote.name.getString().replaceAll("[\\\\/]", "#");
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
        if (!file.getParent().equals(originPath)) {
            finalName = Integer.toString(emote.hashCode());
            file = originPath.resolve(finalName + "." + format.getExtension());
        }
        while (file.toFile().isFile()){
            file = originPath.resolve(finalName + "_" + i++ + "." + format.getExtension());
        }
        return file;
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        renderDirtBackground(guiGraphics);
    }

    @Override
    public void render(@NotNull GuiGraphics matrices, int mouseX, int mouseY, float tickDelta) {
        super.render(matrices, mouseX, mouseY, tickDelta);
    }
}
