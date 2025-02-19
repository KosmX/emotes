package io.github.kosmx.emotes.arch.screen;

import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.server.serializer.type.EmoteSerializerException;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.logging.Level;

public class ExportMenu extends Screen {
    private static final Component TITLE = Component.translatable("emotecraft.options.export");

    private static final Component EXPORT_JSON = Component.translatable("emotecraft.exportjson");
    private static final Component EXPORT_BIN = Component.translatable("emotecraft.exportbin");

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    protected final Screen parent;

    public ExportMenu(Screen parent) {
        super(TITLE);

        this.parent = parent;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void init() {
        this.layout.addTitleHeader(getTitle(), this.font);

        LinearLayout contents = this.layout.addToContents(LinearLayout.vertical().spacing(Button.DEFAULT_SPACING));

        contents.addChild(Button.builder(EXPORT_JSON, button -> exportEmotesInFormat(AnimationFormat.JSON_EMOTECRAFT))
                .width(Button.BIG_WIDTH)
                .build()
        );
        contents.addChild(Button.builder(EXPORT_BIN, button -> exportEmotesInFormat(AnimationFormat.BINARY))
                .width(Button.BIG_WIDTH)
                .build()
        );

        LinearLayout footer = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));

        footer.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .build()
        );
        footer.addChild(Button.builder(EmoteMenu.OPEN_FOLDER, button ->PlatformTools.openExternalEmotesDir())
                .build()
        );

        this.layout.visitWidgets(this::addRenderableWidget);
        repositionElements();
    }

    @SuppressWarnings({"deprecation","removal"})
    private void exportEmotesInFormat(AnimationFormat format){
        for(EmoteHolder emoteHolder:EmoteHolder.list){
            KeyframeAnimation emote = emoteHolder.getEmote();
            if(emote.extraData.containsKey("isBuiltin") && !PlatformTools.getConfig().exportBuiltin.get()){
                continue;
            }
            LoggerService.INSTANCE.log(Level.FINE, "Saving " + emoteHolder.name.getString() + " into " + format.getExtension());
            try{
                Path exportDir = InstanceService.INSTANCE.getExternalEmoteDir().resolve(format.getExtension() + "_export");
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
                LoggerService.INSTANCE.log(Level.WARNING, "Failed to export!", e);
                PlatformTools.toastExportMessage(Component.translatable(
                        "emotecraft.export.error." + format.getExtension()
                ), emoteHolder.name.getString());
            }
        }
        PlatformTools.toastExportMessage(Component.translatable(
                "emotecraft.export.done." + format.getExtension()
        ), "emotes/" + format.getExtension() + "_export/");
        LoggerService.INSTANCE.log(Level.INFO, "All emotes are saved in " + format.getExtension() + " format!");
    }

    @SuppressWarnings({"deprecation","removal"})
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
    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
