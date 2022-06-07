package io.github.kosmx.emotes.arch.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.ClientSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;


/**
 * Config with {@link SerializableConfig.ConfigEntry} objects
 * Every line will be auto-generated
 * I won't ever again need to add here anything
 * just to reimplement it in different environments (Forge/Fabric/1.16/1.12 etc...)
 */
public class ConfigScreen extends OptionsSubScreen {
    private OptionsList options;


    public ConfigScreen(Screen parent) {
        super(parent, null, Component.literal("emotecraft.otherconfig"));
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

        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 27, 150, 20, Component.translatable("controls.reset"), (button) -> {
            this.minecraft.setScreen(new ConfirmScreen(
                    this::resetAll,
                    Component.translatable("emotecraft.resetConfig.title"),
                    Component.translatable("emotecraft.resetConfig.message")));
        }));

        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 27, 150, 20, CommonComponents.GUI_DONE, (button) -> {
            ClientSerializer.saveConfig();
            this.minecraft.setScreen(this.lastScreen);
        }));

        this.addWidget(options);
    }

    private void addConfigEntry(SerializableConfig.ConfigEntry<?> entry, OptionsList options) {
        if (entry.showEntry() || ((ClientConfig) EmoteInstance.config).showHiddenConfig.get()) {
            if (entry instanceof SerializableConfig.BooleanConfigEntry) {
                if (entry.hasTooltip) {
                    options.addBig(OptionInstance.createBoolean("emotecraft.otherconfig." + entry.getName(),
                            minecraft -> aBoolean -> splitTooltip(Component.translatable("emotecraft.otherconfig." + entry.getName() + ".tooltip")), ((SerializableConfig.BooleanConfigEntry) entry).get(),
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
                                minecraft -> o ->
                                        splitTooltip(Component.translatable("emotecraft.otherconfig." + entry.getName() + ".tooltip"))
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

    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.options.render(matrices, mouseX, mouseY, delta);
        drawCenteredString(matrices, this.font, this.title, this.width / 2, 5, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
        List<FormattedCharSequence> list = tooltipAt(this.options, mouseX, mouseY);
        if (list != null) {
            this.renderTooltip(matrices, list, mouseX, mouseY);
        }

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
        public Function<OptionInstance, AbstractWidget> createButton(OptionInstance.TooltipSupplier tooltipSupplier, Options options, int x, int y, int width) {
            return optionInstance -> new DummyButton(x, y, width, 20, text);
        }

        @Override
        public Optional validateValue(Object object) {
            return Optional.empty();
        }

        @Override
        public Codec codec() {
            return null;
        }
    }

    static class DummyButton extends AbstractWidget {

        public DummyButton(int x, int y, int width, int height, Component message) {
            super(x, y, width, height, message);
        }

        @Override
        public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
            drawCenteredString(matrices, Minecraft.getInstance().font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 16777215 | Mth.ceil(this.alpha * 255.0F) << 24);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }

        @Override
        public void updateNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput); //TODO test in-game
        }
    }
}
