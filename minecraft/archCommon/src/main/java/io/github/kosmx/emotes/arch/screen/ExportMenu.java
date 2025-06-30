package io.github.kosmx.emotes.arch.screen;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.api.proxy.AbstractNetworkInstance;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.server.serializer.type.ISerializer;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.raphimc.noteblocklib.NoteBlockLib;
import net.raphimc.noteblocklib.model.Song;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class ExportMenu extends Screen {
    private static final Component TITLE = Component.translatable("emotecraft.options.export");

    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    protected final Screen parent;

    public ExportMenu(Screen parent) {
        super(TITLE);

        this.parent = parent;
    }

    @Override
    public void init() {
        this.layout.addTitleHeader(getTitle(), this.font);

        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().paddingHorizontal(Button.DEFAULT_SPACING).paddingBottom(4).alignHorizontallyCenter();
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);

        for (ISerializer serializer : UniversalEmoteSerializer.getSerializers().toList()) {
            rowHelper.addChild(Button.builder(Component.translatable("emotecraft.export", serializer.getExtension()),
                            button -> exportEmotesInFormat(serializer)
            ).width(Button.BIG_WIDTH).build());
        }

        this.layout.addToContents(gridLayout);

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

    private void exportEmotesInFormat(ISerializer format) {
        for(EmoteHolder emoteHolder:EmoteHolder.list){
            Animation emote = emoteHolder.getEmote();
            if(emote.data().has("isBuiltin") && !PlatformTools.getConfig().exportBuiltin.get()){
                continue;
            }

            CommonData.LOGGER.debug("Saving {} into {}", emoteHolder.name.getString(), format.getExtension());
            try {
                Path exportDir = InstanceService.INSTANCE.getExternalEmoteDir().resolve(format.getExtension() + "_export");
                if (!exportDir.toFile().isDirectory()) {
                    Files.createDirectories(exportDir);
                }

                Path file = createFileName(emoteHolder, exportDir, format.getExtension());
                try (OutputStream stream = Files.newOutputStream(file)) {
                    UniversalEmoteSerializer.writeKeyframeAnimation(stream, emote, "emote." + format.getExtension());
                }

                if (format.onlyEmoteFile()) {
                    String fileName = FilenameUtils.removeExtension(file.getFileName().toString());

                    if (emote.data().has("iconData")) {
                        Path iconPath = exportDir.resolve(fileName + ".png");
                        if (Files.exists(iconPath)) throw new IOException("File already exists: " + iconPath);
                        try (OutputStream iconStream = Files.newOutputStream(iconPath)) {
                            iconStream.write(AbstractNetworkInstance.safeGetBytesFromBuffer((ByteBuffer) emote.data().getRaw("iconData")));
                            iconStream.flush();
                        }
                    }
                    if (emote.data().has("song")) {
                        Path songPath = exportDir.resolve(fileName + ".nbs");
                        if (Files.exists(songPath)) throw new IOException("File already exists: " + songPath);
                        NoteBlockLib.writeSong((Song) emote.data().getRaw("song"), songPath);
                    }
                }
            } catch (Exception e) {
                CommonData.LOGGER.warn("Failed to export!", e);
                PlatformTools.addToast(Component.translatable(
                        "emotecraft.export.error", format.getExtension()
                ), emoteHolder.name);
            }
        }
        PlatformTools.addToast(Component.translatable(
                "emotecraft.export.done", format.getExtension()
        ), Component.literal("emotes/" + format.getExtension() + "_export/"));
        CommonData.LOGGER.info("All emotes are saved in {} format!", format.getExtension());
    }

    private static Path createFileName(EmoteHolder emote, Path originPath, String format) {
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
        Path file = originPath.resolve(finalName + "." + format);
        if (!file.getParent().equals(originPath)) {
            finalName = Integer.toString(emote.hashCode());
            file = originPath.resolve(finalName + "." + format);
        }
        while (file.toFile().isFile()){
            file = originPath.resolve(finalName + "_" + i++ + "." + format);
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
