package io.github.kosmx.emotes.arch.gui.screen;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.ClientSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Config with {@link SerializableConfig.ConfigEntry} objects
 * Every line will be auto-generated
 * I won't ever again need to add here anything
 * just to reimplement it in different environments (Forge/Fabric/1.16/1.12 etc...)
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ConfigScreen extends OptionsSubScreen {
    private OptionsList options;


    public ConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.literal("emotecraft.otherconfig"));
    }

    @Override
    protected void init() {
        super.init();
        options = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        //I just copy these values from VideoOptionsScreen...
        options.addBig(DummyEntry.of("emotecraft.otherconfig.category.general"));

        EmoteInstance.config.iterateGeneral(entry -> addConfigEntry(entry, options));

        options.addBig(DummyEntry.of("emotecraft.otherconfig.category.expert"));
        options.addBig(DummyEntry.of(""));

        EmoteInstance.config.iterateExpert(entry -> addConfigEntry(entry, options));

        this.addRenderableWidget(new Button.Builder(Component.translatable("controls.reset"), (button) -> {
            this.minecraft.setScreen(new ConfirmScreen(
                    this::resetAll,
                    Component.translatable("emotecraft.resetConfig.title"),
                    Component.translatable("emotecraft.resetConfig.message")));
        }).pos(this.width / 2 - 155, this.height - 27).width(150).build());

        this.addRenderableWidget(new Button.Builder(CommonComponents.GUI_DONE, (button -> {
            ClientSerializer.saveConfig();
            this.minecraft.setScreen(this.lastScreen);
        })).pos(this.width / 2 - 155 + 160, this.height - 27).width(150).build());

        this.addWidget(options);
    }

    private void addConfigEntry(SerializableConfig.ConfigEntry<?> entry, OptionsList options) {
        if (entry.showEntry() || ((ClientConfig) EmoteInstance.config).showHiddenConfig.get()) {
            if (entry instanceof SerializableConfig.BooleanConfigEntry) {
                if (entry.hasTooltip) {
                    options.addBig(OptionInstance.createBoolean("emotecraft.otherconfig." + entry.getName(),
                            aBoolean -> Tooltip.create(Component.translatable("emotecraft.otherconfig." + entry.getName() + ".tooltip")), ((SerializableConfig.BooleanConfigEntry) entry).get(),
                            (aBoolean) -> ((SerializableConfig.BooleanConfigEntry) entry).set(aBoolean)
                    ));
                } else {
                    options.addBig(OptionInstance.createBoolean("emotecraft.otherconfig." + entry.getName(),
                            OptionInstance.noTooltip(), ((SerializableConfig.BooleanConfigEntry) entry).get(),
                            (aBoolean) -> ((SerializableConfig.BooleanConfigEntry) entry).set(aBoolean)
                    ));
                }
            } else if (entry instanceof SerializableConfig.FloatConfigEntry floatEntry) {
                /*options.addBig(new ProgressOption(
                        EmoteInstance.config.validThreshold.getName(), floatEntry.min, floatEntry.max, floatEntry.step,
                        gameOptions -> floatEntry.getConfigVal(),
                        (gameOptions, aDouble) -> floatEntry.setConfigVal(aDouble),
                        (gameOptions, doubleOption) -> new TranslatableComponent(floatEntry.getFormatKey(), new TranslatableComponent("emotecraft.otherconfig." + floatEntry.getName()), floatEntry.getTextVal()),
                        minecraft -> floatEntry.hasTooltip ?
                                Minecraft.getInstance().font.split(new TranslatableComponent("emotecraft.otherconfig." + entry.getName() + ".tooltip"), 200)
                                : ImmutableList.of()
                ));*/

                int mapSize = 1024; //whatever
                double range = floatEntry.max - floatEntry.min;

                DecimalFormat formatter = new DecimalFormat("0.00");

                Function<Integer, Double> i2d = integer -> integer / (double) mapSize * range + floatEntry.min;
                Function<Double, Integer> d2i = aDouble -> (int) ((aDouble - floatEntry.min) / range * mapSize);

                options.addBig(new OptionInstance<>(
                        floatEntry.getName(),
                        floatEntry.hasTooltip ?
                                o ->
                                        Tooltip.create(Component.translatable("emotecraft.otherconfig." + entry.getName() + ".tooltip"))
                                : OptionInstance.noTooltip(),
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
            EmoteInstance.config.iterate(SerializableConfig.ConfigEntry::resetToDefault);
            this.init(); //reload screen
        }
        this.minecraft.setScreen(this);
    }

    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        this.options.render(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 5, 16777215);
        super.render(graphics, mouseX, mouseY, delta);
    }



    protected static List<FormattedCharSequence> splitTooltip(Component component) {
        return Minecraft.getInstance().font.split(component, 200);
    }

    public static class DummyEntry implements OptionInstance.ValueSet {
        private final MutableComponent text;

        public static OptionInstance of(String key) {
            return new OptionInstance(key, OptionInstance.noTooltip(), (component, object) -> Component.empty(), new DummyEntry(key), 42, o -> {});
        }

        public DummyEntry(String key) {
            this.text = Component.translatable(key);
        }


        @Override
        public @NotNull Function<OptionInstance, AbstractWidget> createButton(OptionInstance.@NotNull TooltipSupplier tooltipSupplier, @NotNull Options options, int x, int y, int width, @NotNull Consumer c) {
            return optionInstance -> new DummyButton(x, y, width, 20, text);
        }

        @Override
        public @NotNull Optional validateValue(@NotNull Object object) {
            return Optional.empty();
        }

        @Override
        public @NotNull Codec codec() {
            return new Codec() {
                @Override
                public DataResult<Pair> decode(DynamicOps ops, Object input) {
                    return null;
                }

                @Override
                public DataResult encode(Object input, DynamicOps ops, Object prefix) {
                    return null;
                }
            };
        }
    }

    static class DummyButton extends AbstractWidget {

        public DummyButton(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            graphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, 16777215 | Mth.ceil(this.alpha * 255.0F) << 24);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput); //TODO test in-game
        }
    }
}
