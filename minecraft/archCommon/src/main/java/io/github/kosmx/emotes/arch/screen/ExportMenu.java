package io.github.kosmx.emotes.arch.screen;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.main.EmoteHolder;
import io.github.kosmx.emotes.server.serializer.EmoteSerializer;
import io.github.kosmx.emotes.server.serializer.EmoteWriter;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.server.serializer.type.IWriter;
import io.github.kosmx.emotes.server.services.InstanceService;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExportMenu extends Screen {
    private static final Component TITLE = Component.translatable("emotecraft.options.export");
    private static final Component DATA_LOSS_MSG = Component.translatable("emotecraft.dataloss.msg");
    private static final Component DATA_LOSS_DESCR = Component.translatable("emotecraft.dataloss.descr");

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

        for (IWriter serializer : UniversalEmoteSerializer.WRITERS) {
            rowHelper.addChild(Button.builder(Component.translatable("emotecraft.export", serializer.getExtension()), button -> {
                if (!serializer.possibleDataLoss()) {
                    exportEmotesInFormat(serializer);
                    return;
                }

                this.minecraft.setScreen(new ConfirmScreen(choice -> {
                    if (choice) exportEmotesInFormat(serializer);
                    this.minecraft.setScreen(this);
                }, DATA_LOSS_MSG, DATA_LOSS_DESCR) {
                    @Override
                    protected void addButtons(LinearLayout layout) {
                        super.addButtons(layout);
                        setDelay(100);
                    }
                });
            }).width(Button.BIG_WIDTH).build());
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

    private void exportEmotesInFormat(IWriter format) {
        Path exportDir = InstanceService.INSTANCE.getExternalEmoteDir().resolve(format.getExtension() + "_export");
        try {
            Files.createDirectories(exportDir);
        } catch (IOException ignored) {}

        for (EmoteHolder emoteHolder : EmoteHolder.list) {
            Animation emote = emoteHolder.getEmote();
            if (emote.data().has(EmoteSerializer.BUILT_IN_KEY)) continue;

            CommonData.LOGGER.debug("Saving {} into {}", emoteHolder.name.getString(), format.getExtension());
            try {
                EmoteWriter.writeAnimationInFormat(emote, exportDir, format);
            } catch (Exception e) {
                CommonData.LOGGER.warn("Failed to export!", e);
                PlatformTools.addToast(Component.translatable("emotecraft.export.error", format.getExtension()), emoteHolder.name);
            }
        }

        String exportPath = InstanceService.INSTANCE.getGameDirectory()
                .relativize(exportDir)
                .normalize()
                .toString()
                .replace(File.separator, "/");

        PlatformTools.addToast(Component.translatable("emotecraft.export.done", format.getExtension()), Component.literal(exportPath));
        CommonData.LOGGER.info("All emotes are saved in {} format!", format.getExtension());
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
