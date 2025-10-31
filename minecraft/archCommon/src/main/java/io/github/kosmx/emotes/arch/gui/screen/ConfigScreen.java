package io.github.kosmx.emotes.arch.gui.screen;

import com.mojang.serialization.Codec;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.EmoteMenu;
import io.github.kosmx.emotes.arch.screen.ExportMenu;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.server.config.Serializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.function.Function;

/**
 * Config with {@link SerializableConfig.ConfigEntry} objects
 * Every line will be auto-generated
 * I won't ever again need to add here anything
 * just to reimplement it in different environments (Forge/Fabric/1.16/1.12 etc...)
 */
public class ConfigScreen extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("emotecraft.otherconfig");

    private static final Component CATEGORY_GENERAL = Component.translatable("emotecraft.otherconfig.category.general");
    private static final Component CATEGORY_EXPERT = Component.translatable("emotecraft.otherconfig.category.expert");
    private static final Component CATEGORY_LEGACY = Component.translatable("emotecraft.otherconfig.category.legacy");

    private static final Component RESET_CONFIG_TITLE = Component.translatable("emotecraft.resetConfig.title");
    private static final Component RESET_CONFIG_MSG = Component.translatable("emotecraft.resetConfig.message");

    private static final Component EXPORT = Component.translatable("emotecraft.options.export");

    public ConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, TITLE);
    }

    @Override
    protected void addOptions() {
        assert this.list != null;

        this.list.addSmall(Collections.singletonList(new StringWidget(CATEGORY_GENERAL, this.font)));
        PlatformTools.getConfig().basics.forEach(entry -> addConfigEntry(entry, list));

        this.list.addSmall(Collections.singletonList(new StringWidget(CATEGORY_EXPERT, this.font)));
        PlatformTools.getConfig().expert.forEach(entry -> addConfigEntry(entry, list));

        this.list.addSmall(Collections.singletonList(new StringWidget(CATEGORY_LEGACY, this.font)));
        PlatformTools.getConfig().legacy.forEach(entry -> addConfigEntry(entry, list));
    }

    @Override
    protected void addFooter() {
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));

        linearLayout.addChild(Button.builder(EmoteMenu.RESET, button -> this.minecraft.setScreen(new ConfirmScreen(
                this::resetAll, RESET_CONFIG_TITLE, RESET_CONFIG_MSG
        ))).width(Button.SMALL_WIDTH).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).build());
        linearLayout.addChild(Button.builder(EXPORT, button -> this.minecraft.setScreen(new ExportMenu(this)))
                .width(Button.SMALL_WIDTH)
                .build()
        );
    }

    @SuppressWarnings("unchecked")
    private <T> void addConfigEntry(SerializableConfig.ConfigEntry<T> entry, OptionsList options) {
        if (entry.showEntry() || PlatformTools.getConfig().showHiddenConfig.get()) {
            OptionInstance.TooltipSupplier<?> tooltip;
            if (entry.hasTooltip) {
                tooltip = b -> Tooltip.create(
                        Component.translatable("emotecraft.otherconfig." + entry.getName() + ".tooltip")
                );
            } else {
                tooltip = OptionInstance.noTooltip();
            }

            if (entry.get() instanceof Boolean b) {
                options.addBig(OptionInstance.createBoolean("emotecraft.otherconfig." + entry.getName(),
                        (OptionInstance.TooltipSupplier<Boolean>) tooltip, b, (aBoolean) -> entry.set((T) aBoolean)
                ));
            } else if (entry instanceof SerializableConfig.FloatConfigEntry floatEntry) {
                int mapSize = 1024; //whatever
                double range = floatEntry.max - floatEntry.min;

                DecimalFormat formatter = new DecimalFormat("0.00");

                Function<Integer, Double> i2d = integer -> integer / (double) mapSize * range + floatEntry.min;
                Function<Double, Integer> d2i = aDouble -> (int) ((aDouble - floatEntry.min) / range * mapSize);

                options.addBig(new OptionInstance<>(
                        floatEntry.getName(), (OptionInstance.TooltipSupplier<Integer>) tooltip,
                        (component, object) -> Options.genericValueLabel(component, Component.literal(formatter.format(floatEntry.getTextVal()))),
                        new OptionInstance.IntRange(0, mapSize),
                        Codec.DOUBLE.xmap(d2i, i2d),
                        d2i.apply(floatEntry.getConfigVal()),
                        integer -> floatEntry.setConfigVal(i2d.apply(integer))
                ));
            }
        }
    }

    private void resetAll(boolean bl) {
        if (bl) {
            PlatformTools.getConfig().iterate(SerializableConfig.ConfigEntry::resetToDefault);
        }
        this.minecraft.setScreen(this);
    }

    @Override
    public void removed() {
        Serializer.INSTANCE.saveConfig();
    }
}
