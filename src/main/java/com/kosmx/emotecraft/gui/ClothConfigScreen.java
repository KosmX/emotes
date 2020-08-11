package com.kosmx.emotecraft.gui;

import com.kosmx.emotecraft.Main;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

public class ClothConfigScreen {
    public static Screen getConfigScreen(Screen parent){
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(new TranslatableText("emotecraft.otherconfig"));
        builder.setSavingRunnable(()->{
            if(parent instanceof EmoteMenu){
                ((EmoteMenu)parent).save = true;    //It's parent is EmoteMenu, when you leave that and save == true -> it'll save
            }
        });
        ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("emotecraft.category.general"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.debug"), Main.config.showDebug)
                                 .setDefaultValue(false)
                                 .setSaveConsumer(newValue -> Main.config.showDebug = newValue).build());
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.validate"), Main.config.validateEmote)
                                 .setTooltip(new TranslatableText("emotecraft.otherconfig.validate.tooltip"))
                                 .setDefaultValue(false)
                                 .setSaveConsumer(newValue -> Main.config.validateEmote = newValue).build());
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.dark"), Main.config.dark)
                                 .setDefaultValue(false)
                                 .setSaveConsumer(newValue -> Main.config.dark = newValue).build());
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.quark"), Main.config.enableQuark)
                                 .setDefaultValue(false)
                                 .setSaveConsumer(newValue -> {
                                     if(newValue && parent instanceof EmoteMenu && !Main.config.enableQuark){
                                         ((EmoteMenu)parent).warn = true;
                                     }
                                     Main.config.enableQuark = newValue;
                                 }).build());
        general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("emotecraft.otherconfig.showicon"), Main.config.showIcons)
                                 .setDefaultValue(true)
                                 .setTooltip(new TranslatableText("emotecraft.otherconfig.showicon.tooltip"))
                                 .setSaveConsumer(newValue -> Main.config.showIcons = newValue).build());
        return builder.build();
    }
}
