package com.kosmx.emotecraft.config;

import com.kosmx.emotecraft.screen.widget.AbstractFastChooseWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;

public class SerializableConfig {
    @Environment(EnvType.CLIENT)
    public final List<EmoteHolder> emotesWithKey = new ArrayList<>();
    @Environment(EnvType.CLIENT)
    public final EmoteHolder[] fastMenuEmotes = new EmoteHolder[8];

    public boolean validateEmote = false;
    public boolean showDebug = false;
    @Environment(EnvType.CLIENT)
    public boolean dark = false;


}
