package io.github.kosmx.emotes.fabric.gui.screen;

import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.fabric.gui.EmoteMenuImpl;
import io.github.kosmx.emotes.main.config.Serializer;
import io.github.kosmx.emotes.main.screen.EmoteMenu;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.options.GameOptionsScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;

import java.util.List;


/**
 * Config with {@link SerializableConfig.ConfigEntry} objects
 * Every line will be auto-generated
 * I won't ever again need to add here anything
 * just to reimplement it in different environments (Forge/Fabric/1.16/1.12 etc...)
 */
public class ConfigScreen extends GameOptionsScreen {
    private ButtonListWidget options;


    public ConfigScreen(Screen parent) {
        super(parent, null, new TranslatableText("emotecraft.otherconfig"));
    }

    @Override
    protected void init() {
        super.init();
        options = new ButtonListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        //I just copy these values from VideoOptionsScreen...
        options.addSingleOptionEntry(new DummyEntry("emotecraft.otherconfig.category.general"));

        EmoteInstance.config.iterateGeneral(entry -> addConfigEntry(entry, options));

        options.addSingleOptionEntry(new BooleanOption(
                "emotecraft.otherconfig.exportGecko",
                new TranslatableText("emotecraft.otherconfig.exportGecko.tooltip"),
                gameOptions -> {
                    if (parent instanceof EmoteMenuImpl) {
                        return ((EmoteMenu) ((EmoteMenuImpl) parent).master).exportGeckoEmotes;
                    }
                    return false;
                },
                (gameOptions, aBoolean) -> {
                    if (parent instanceof EmoteMenuImpl) {
                        ((EmoteMenu) ((EmoteMenuImpl) parent).master).exportGeckoEmotes = aBoolean;
                    }
                }
        ));

        options.addSingleOptionEntry(new DummyEntry("emotecraft.otherconfig.category.expert"));
        options.addSingleOptionEntry(new DummyEntry(""));

        EmoteInstance.config.iterateExpert(entry -> addConfigEntry(entry, options));

        this.addButton(new ButtonWidget(this.width / 2 - 155, this.height - 27, 150, 20, new TranslatableText("controls.resetAll"), (button) -> {
            this.client.openScreen(new ConfirmScreen(
                    this::resetAll,
                    new TranslatableText("emotecraft.resetConfig.title"),
                    new TranslatableText("emotecraft.resetConfig.message")));
        }));

        this.addButton(new ButtonWidget(this.width / 2 - 155 + 160, this.height - 27, 150, 20, ScreenTexts.DONE, (button) -> {
            Serializer.saveConfig();
            this.client.openScreen(this.parent);
        }));

        this.children.add(options);
    }

    private void addConfigEntry(SerializableConfig.ConfigEntry<?> entry, ButtonListWidget options){
        if(entry instanceof SerializableConfig.BooleanConfigEntry){
            options.addSingleOptionEntry(new BooleanOption("emotecraft.otherconfig." + entry.getName(),
                                                           entry.hasTooltip ? new TranslatableText("emotecraft.otherconfig." + entry.getName() + ".tooltip") : null,
                                                           gameOptions -> ((SerializableConfig.BooleanConfigEntry) entry).get(),
                                                           (gameOptions, aBoolean) -> ((SerializableConfig.BooleanConfigEntry)entry).set(aBoolean)
            ));
        }
        else if(entry instanceof SerializableConfig.FloatConfigEntry){
            SerializableConfig.FloatConfigEntry floatEntry = (SerializableConfig.FloatConfigEntry) entry;
            options.addSingleOptionEntry(new DoubleOption(
                    EmoteInstance.config.validThreshold.getName(), floatEntry.min, floatEntry.max, floatEntry.step,
                    gameOptions -> floatEntry.getConfigVal(),
                    (gameOptions, aDouble) -> floatEntry.setConfigVal(aDouble),
                    (gameOptions, doubleOption) -> {
                        if(floatEntry.hasTooltip) doubleOption.setTooltip(MinecraftClient.getInstance().textRenderer.wrapLines(new TranslatableText("emotecraft.otherconfig." + entry.getName() + ".tooltip"), 200));
                        return new TranslatableText(floatEntry.getFormatKey(), new TranslatableText("emotecraft.otherconfig." + floatEntry.getName()), floatEntry.getTextVal());
                    }
            ));
        }
    }

    private void resetAll(boolean bl){
        if(bl) {
            EmoteInstance.config.iterate(SerializableConfig.ConfigEntry::resetToDefault);
            this.init(); //reload screen
        }
        this.client.openScreen(this);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        this.options.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 5, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
        List<OrderedText> list = getHoveredButtonTooltip(this.options, mouseX, mouseY);
        if (list != null) {
            this.renderOrderedTooltip(matrices, list, mouseX, mouseY);
        }

    }

    static class DummyEntry extends Option{

        public DummyEntry(String key) {
            super(key);
        }

        @Override
        public AbstractButtonWidget createButton(GameOptions options, int x, int y, int width) {
            return new DummyButton(x, y, width, 20, this.getDisplayPrefix());
        }
    }

    static class DummyButton extends AbstractButtonWidget{

        public DummyButton(int x, int y, int width, int height, Text message) {
            super(x, y, width, height, message);
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 16777215 | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }
    }
}
