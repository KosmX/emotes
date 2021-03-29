/*
package com.kosmx.emotes.fabric.gui;

import com.kosmx.emotes.executor.EmoteInstance;
import com.kosmx.emotes.main.config.ClientConfig;
import com.kosmx.emotes.main.screen.EmoteMenu;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

public class ClothConfigScreen {
    public static Screen getConfigScreen(Screen parent){
        ClientConfig config = (ClientConfig) EmoteInstance.config;
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(new TranslatableText("emotecraft.otherconfig"));
        builder.setSavingRunnable(()->{
            if(parent instanceof EmoteMenuImpl){
                ((EmoteMenu)((EmoteMenuImpl) parent).master).save = true;    //It's parent is EmoteMenu, when you leave that and save == true -> it'll save
            }
        });
        ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("emotecraft.otherconfig.category.general")); //configs what most people will use
        ConfigCategory expert = builder.getOrCreateCategory(new TranslatableText("emotecraft.otherconfig.category.expert")); //expert configs
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.dark"), config.dark).setDefaultValue(false).setSaveConsumer(newValue->config.dark = newValue).build());
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.perspective"), config.enablePerspective).setDefaultValue(true).setSaveConsumer(newValue -> config.enablePerspective = newValue).build());
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.loadbuiltin"), config.loadBuiltinEmotes).setDefaultValue(true).setTooltip(new TranslatableText("emotecraft.otherconfig.loadbuiltin.tooltip")).setSaveConsumer(newValue->config.loadBuiltinEmotes = newValue).build());
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.showicon"), config.showIcons).setDefaultValue(true).setTooltip(new TranslatableText("emotecraft.otherconfig.showicon.tooltip")).setSaveConsumer(newValue->config.showIcons = newValue).build());
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.quark"), config.enableQuark).setDefaultValue(false).setSaveConsumer(newValue->{
            if(newValue && parent instanceof EmoteMenuImpl && ! config.enableQuark){
                ((EmoteMenu)((EmoteMenuImpl) parent).master).warn = true;
            }
            config.enableQuark = newValue;
        }).build());
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.exportGecko"), false).setDefaultValue(false).setTooltip(new TranslatableText("emotecraft.otherconfig.exportGecko.tooltip")).setSaveConsumer(newValue->{
            if(parent instanceof EmoteMenuImpl){
                ((EmoteMenu)((EmoteMenuImpl) parent).master).exportGeckoEmotes = newValue;
            }
        }).build());

        //Expert options

        expert.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.debug"), config.showDebug).setDefaultValue(false).setSaveConsumer(newValue->config.showDebug = newValue).build());
        expert.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.playersafety"), config.enablePlayerSafety).setDefaultValue(true).setTooltip(new TranslatableText("emotecraft.otherconfig.playersafety.tooltip")).setSaveConsumer(newValue->config.enablePlayerSafety = newValue).build());
        expert.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.validate"), config.validateEmote).setTooltip(new TranslatableText("emotecraft.otherconfig.validate.tooltip")).setDefaultValue(false).setSaveConsumer(newValue->config.validateEmote = newValue).build());
        expert.addEntry(entryBuilder.startFloatField(new TranslatableText("emotecraft.otherconfig.validationThreshold"), config.validThreshold).setDefaultValue(8f).setSaveConsumer(newValue->config.validThreshold = newValue).setTooltip(new TranslatableText("emotecraft.otherconfig.validationThreshold.tooltip")).build());
        expert.addEntry(entryBuilder.startFloatField(new TranslatableText("emotecraft.otherconfig.stopthreshold"), config.stopThreshold).setDefaultValue(0.04f).setTooltip(new TranslatableText("emotecraft.otherconfig.stopthreshold.tooltip")).setSaveConsumer(newValue->config.stopThreshold = newValue).build());
        expert.addEntry(entryBuilder.startIntSlider(new TranslatableText("emotecraft.otherconfig.yratio"), (int) (config.yRatio * 100), 0, 100).setDefaultValue(75).setTooltip(new TranslatableText("emotecraft.otherconfig.yratio.tooltip")).setSaveConsumer(newValue -> config.yRatio = newValue / 100f).build());
        if(FabricLoader.getInstance().isModLoaded("perspectivemod")){
            expert.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.prespective_redux"), config.perspectiveReduxIntegration).setDefaultValue(true).setSaveConsumer(newValue->config.perspectiveReduxIntegration = newValue).build());
        }
        return builder.build();
    }
}

 */
