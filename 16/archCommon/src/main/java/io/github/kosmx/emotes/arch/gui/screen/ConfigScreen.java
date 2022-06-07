package io.github.kosmx.emotes.arch.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.main.config.ClientConfig;
import io.github.kosmx.emotes.main.config.ClientSerializer;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;


/**
 * Config with {@link SerializableConfig.ConfigEntry} objects
 * Every line will be auto-generated
 * I won't ever again need to add here anything
 * just to reimplement it in different environments (Forge/Fabric/1.16/1.12 etc...)
 */
public class ConfigScreen extends OptionsSubScreen {
    private OptionsList options;


    public ConfigScreen(Screen parent) {
        super(parent, null, new TranslatableComponent("emotecraft.otherconfig"));
    }

    @Override
    protected void init() {
        super.init();
        options = new OptionsList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        //I just copy these values from VideoOptionsScreen...
        options.addBig(new DummyEntry("emotecraft.otherconfig.category.general"));

        EmoteInstance.config.iterateGeneral(entry -> addConfigEntry(entry, options));

        options.addBig(new DummyEntry("emotecraft.otherconfig.category.expert"));
        options.addBig(new DummyEntry(""));

        EmoteInstance.config.iterateExpert(entry -> addConfigEntry(entry, options));

        this.addButton(new Button(this.width / 2 - 155, this.height - 27, 150, 20, new TranslatableComponent("controls.reset"), (button) -> {
            this.minecraft.setScreen(new ConfirmScreen(
                    this::resetAll,
                    new TranslatableComponent("emotecraft.resetConfig.title"),
                    new TranslatableComponent("emotecraft.resetConfig.message")));
        }));

        this.addButton(new Button(this.width / 2 - 155 + 160, this.height - 27, 150, 20, CommonComponents.GUI_DONE, (button) -> {
            ClientSerializer.saveConfig();
            this.minecraft.setScreen(this.lastScreen);
        }));

        this.children.add(options);
    }

    private void addConfigEntry(SerializableConfig.ConfigEntry<?> entry, OptionsList options){
        if(entry.showEntry() || ((ClientConfig)EmoteInstance.config).showHiddenConfig.get()) {
            if (entry instanceof SerializableConfig.BooleanConfigEntry) {
                options.addBig(new BooleanOption("emotecraft.otherconfig." + entry.getName(),
                        entry.hasTooltip ? new TranslatableComponent("emotecraft.otherconfig." + entry.getName() + ".tooltip") : null,
                        gameOptions -> ((SerializableConfig.BooleanConfigEntry) entry).get(),
                        (gameOptions, aBoolean) -> ((SerializableConfig.BooleanConfigEntry) entry).set(aBoolean)
                ));
            } else if (entry instanceof SerializableConfig.FloatConfigEntry) {
                SerializableConfig.FloatConfigEntry floatEntry = (SerializableConfig.FloatConfigEntry) entry;
                options.addBig(new ProgressOption(
                        EmoteInstance.config.validThreshold.getName(), floatEntry.min, floatEntry.max, floatEntry.step,
                        gameOptions -> floatEntry.getConfigVal(),
                        (gameOptions, aDouble) -> floatEntry.setConfigVal(aDouble),
                        (gameOptions, doubleOption) -> {
                            if (floatEntry.hasTooltip)
                                doubleOption.setTooltip(Minecraft.getInstance().font.split(new TranslatableComponent("emotecraft.otherconfig." + entry.getName() + ".tooltip"), 200));
                            return new TranslatableComponent(floatEntry.getFormatKey(), new TranslatableComponent("emotecraft.otherconfig." + floatEntry.getName()), floatEntry.getTextVal());
                        }
                ));
            }
        }
    }

    private void resetAll(boolean bl){
        if(bl) {
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

    static class DummyEntry extends Option{

        public DummyEntry(String key) {
            super(key);
        }

        @Override
        public AbstractWidget createButton(Options options, int x, int y, int width) {
            return new DummyButton(x, y, width, 20, this.getCaption());
        }
    }

    static class DummyButton extends AbstractWidget{

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
    }
}
