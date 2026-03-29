package io.github.kosmx.emotes.arch.gui.screen;

import com.mojang.serialization.Codec;
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

    private static final Component RESET_CONFIG_TITLE = Component.translatable("emotecraft.resetConfig.title");
    private static final Component RESET_CONFIG_MSG = Component.translatable("emotecraft.resetConfig.message");

    private static final Component EXPORT = Component.translatable("emotecraft.options.export");

    protected final Serializer<?> serializer;
    protected final String namespace;

    public ConfigScreen(Screen parent) {
        this(parent, Serializer.INSTANCE, CommonData.MOD_ID, TITLE);
    }

    public ConfigScreen(Screen parent, Serializer<?> serializer, String namespace, Component title) {
        super(parent, Minecraft.getInstance().options, title);
        this.serializer = serializer;
        this.namespace = namespace;
    }

    @Override
    protected void addOptions() {
        assert this.list != null;
        this.serializer.readConfig(false).getCategories().forEach(this::addCategory);
    }

    protected void addCategory(String category, List<SerializableConfig.ConfigEntry<?>> entries) {
        this.list.addHeader(Component.translatable(this.namespace + ".otherconfig.category." + category));
        entries.forEach(entry -> addConfigEntry(entry, this.list));
    }

    @Override
    protected void addFooter() {
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(Button.DEFAULT_SPACING));

        boolean addExport = this.serializer.readConfig(false) instanceof ClientConfig;

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
    protected <T> void addConfigEntry(SerializableConfig.ConfigEntry<T> entry, OptionsList options) {
        if (entry.showEntry() || (this.serializer.readConfig(false) instanceof ClientConfig clientConfig && clientConfig.showHiddenConfig.get())) {
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
            } else if (entry instanceof SerializableConfig.NumberConfigEntry<?> numberEntry) {
                addNumberEntry(options, tooltip, numberEntry);
            } else if (entry instanceof SerializableConfig.EnumConfigEntry<?> enumEntry) {
                addEnumEntry(options, tooltip, enumEntry);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Number & Comparable<T>> void addNumberEntry(OptionsList options, OptionInstance.TooltipSupplier<?> tooltip, SerializableConfig.NumberConfigEntry<T> entry) {
        NumberRange<T> range = new NumberRange<>(entry);
        options.addBig(new OptionInstance<>(
                this.namespace + ".otherconfig." + entry.getName(),
                (OptionInstance.TooltipSupplier<@NonNull T>) tooltip,
                (component, object) -> Options.genericValueLabel(component, Component.literal(object.toString())),
                range,
                range.codec(),
                entry.get(),
                entry::set
        ));
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
        if (bl) this.serializer.readConfig(false).iterate(SerializableConfig.ConfigEntry::resetToDefault);
        this.minecraft.setScreen(this);
        if (this.list != null) {
            this.list.clearEntries();
            addOptions();
        }
    }

    @Override
    public void onClose() {
        this.serializer.saveConfig();
        super.onClose();
    }

    public record NumberRange<T extends Number & Comparable<T>>(SerializableConfig.NumberConfigEntry<T> entry, boolean applyValueImmediately) implements OptionInstance.SliderableValueSet<@NonNull T> {
        public NumberRange(SerializableConfig.NumberConfigEntry<T> entry) {
            this(entry, true);
        }

        @Override
        public @NonNull Optional<@NonNull T> validateValue(final T value) {
            return value.compareTo(this.entry.min) >= 0 && value.compareTo(this.entry.max) <= 0 ? Optional.of(value) : Optional.empty();
        }

        @Override
        public @NonNull Codec<T> codec() {
            var checker = Codec.checkRange(this.entry.min, this.entry.max);
            return Codec.DOUBLE.xmap(this.entry::fromDouble, Number::doubleValue).flatXmap(checker, checker);
        }

        @Override
        public @NonNull Optional<@NonNull T> next(final T current) {
            return Optional.of(this.entry.fromDouble(current.doubleValue() + 1));
        }

        @Override
        public @NonNull Optional<@NonNull T> previous(final T current) {
            return Optional.of(this.entry.fromDouble(current.doubleValue() - 1));
        }

        @Override
        public double toSliderValue(final T value) {
            if (value.compareTo(this.entry.min) <= 0) {
                return 0.0;
            } else if (value.compareTo(this.entry.max) >= 0) {
                return 1.0;
            } else {
                return Mth.map(value.doubleValue(), this.entry.min.doubleValue(), this.entry.max.doubleValue(), 0.0, 1.0);
            }
        }

        @Override
        public T fromSliderValue(double slider) {
            return this.entry.fromDouble(Mth.map(slider, 0.0, 1.0, this.entry.min.doubleValue(), this.entry.max.doubleValue()));
        }
    }
}
