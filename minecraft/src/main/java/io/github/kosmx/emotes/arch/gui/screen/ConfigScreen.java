package io.github.kosmx.emotes.arch.gui.screen;

import com.mojang.serialization.Codec;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.screen.EmoteMenu;
import io.github.kosmx.emotes.arch.screen.ExportMenu;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.server.config.Serializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    protected final SerializableConfig config;
    protected final String namespace;

    public ConfigScreen(Screen parent) {
        this(parent, PlatformTools.getConfig(), CommonData.MOD_ID, TITLE);
    }

    public ConfigScreen(Screen parent, SerializableConfig config, String namespace, Component title) {
        super(parent, Minecraft.getInstance().options, title);
        this.config = config;
        this.namespace = namespace;
    }

    @Override
    protected void addOptions() {
        assert this.list != null;

        this.list.addSmall(Collections.singletonList(new StringWidget(CATEGORY_GENERAL, this.font)));
        this.config.basics.forEach(entry -> addConfigEntry(entry, list));

        this.list.addSmall(Collections.singletonList(new StringWidget(CATEGORY_EXPERT, this.font)));
        this.config.expert.forEach(entry -> addConfigEntry(entry, list));

        if (config instanceof ClientConfig clientConfig) {
            this.list.addSmall(Collections.singletonList(new StringWidget(CATEGORY_LEGACY, this.font)));
            clientConfig.legacy.forEach(entry -> addConfigEntry(entry, list));
        }
    }

    @Override
    protected void addFooter() {
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));

        boolean addExport = config instanceof ClientConfig;

        linearLayout.addChild(Button.builder(EmoteMenu.RESET, _ -> this.minecraft.setScreen(new ConfirmScreen(
                this::resetAll, RESET_CONFIG_TITLE, RESET_CONFIG_MSG
        ))).width(addExport ? Button.SMALL_WIDTH : Button.DEFAULT_WIDTH).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> onClose()).build());
        if (addExport) linearLayout.addChild(Button.builder(EXPORT, _ -> this.minecraft.setScreen(new ExportMenu(this)))
                .width(Button.SMALL_WIDTH)
                .build()
        );
    }

    @SuppressWarnings("unchecked")
    private <T> void addConfigEntry(SerializableConfig.ConfigEntry<T> entry, OptionsList options) {
        if (entry.showEntry() || (this.config instanceof ClientConfig clientConfig && clientConfig.showHiddenConfig.get())) {
            OptionInstance.TooltipSupplier<?> tooltip;
            if (entry.hasTooltip) {
                tooltip = _ -> Tooltip.create(
                        Component.translatable(this.namespace + ".otherconfig." + entry.getName() + ".tooltip")
                );
            } else {
                tooltip = OptionInstance.noTooltip();
            }

            if (entry.get() instanceof Boolean b) {
                options.addBig(OptionInstance.createBoolean(this.namespace + ".otherconfig." + entry.getName(),
                        (OptionInstance.TooltipSupplier<Boolean>) tooltip, b, (aBoolean) -> entry.set((T) aBoolean)
                ));
            } else if (entry instanceof SerializableConfig.FloatConfigEntry floatEntry) {
                options.addBig(new OptionInstance<>(
                        this.namespace + ".otherconfig." + floatEntry.getName(), (OptionInstance.TooltipSupplier<Float>) tooltip,
                        (component, object) -> Options.genericValueLabel(component, Component.literal(object.toString())),
                        new FloatRange(floatEntry.min, floatEntry.max),
                        Codec.FLOAT,
                        floatEntry.get(),
                        floatEntry::set
                ));
            } else if (entry instanceof SerializableConfig.EnumConfigEntry<?> enumEntry) {
                addEnumEntry(options, tooltip, enumEntry);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> void addEnumEntry(OptionsList options, OptionInstance.TooltipSupplier<?> tooltip, SerializableConfig.EnumConfigEntry<T> entry) {
        Class<T> enumClass = entry.getEnumClass();
        T[] values = enumClass.getEnumConstants();

        Codec<T> codec = Codec.STRING.xmap(
                s -> Enum.valueOf(enumClass, s),
                Object::toString
        );

        options.addBig(new OptionInstance<>(
                this.namespace + ".otherconfig." + entry.getName(),
                (OptionInstance.TooltipSupplier<T>) tooltip,
                (_, value) -> Component.literal(value.name()),
                new OptionInstance.Enum<>(List.of(values), codec),
                codec,
                entry.get(),
                entry::set
        ));
    }

    private void resetAll(boolean bl) {
        if (bl) this.config.iterate(SerializableConfig.ConfigEntry::resetToDefault);
        this.minecraft.setScreen(this);
    }

    @Override
    public void removed() {
        Serializer.INSTANCE.saveConfig();
    }

    public record FloatRange(float minInclusive, float maxInclusive, boolean applyValueImmediately) implements FloatRangeBase {
        public FloatRange(final float minInclusive, final float maxInclusive) {
            this(minInclusive, maxInclusive, true);
        }

        @Override
        public @NonNull Optional<Float> validateValue(final Float value) {
            return value.compareTo(minInclusive()) >= 0 && value.compareTo(maxInclusive()) <= 0 ? Optional.of(value) : Optional.empty();
        }

        @Override
        public @NonNull Codec<Float> codec() {
            return Codec.floatRange(this.minInclusive, this.maxInclusive);
        }
    }

    interface FloatRangeBase extends OptionInstance.SliderableValueSet<Float> {
        float minInclusive();
        float maxInclusive();

        @Override
        default @NonNull Optional<Float> next(final Float current) {
            return Optional.of(current + 1);
        }

        @Override
        default @NonNull Optional<Float> previous(final Float current) {
            return Optional.of(current - 1);
        }

        @Override
        default double toSliderValue(final Float value) {
            if (value == minInclusive()) {
                return 0.0;
            } else {
                return value == maxInclusive() ? 1.0 : Mth.map(value.intValue() + 0.5, minInclusive(), maxInclusive(), 0.0, 1.0);
            }
        }

        @Override
        default Float fromSliderValue(double slider) {
            return Mth.map((float) slider, 0.0F, 1.0F, minInclusive(), maxInclusive());
        }
    }
}
