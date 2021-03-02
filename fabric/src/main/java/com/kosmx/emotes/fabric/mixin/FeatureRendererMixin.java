package com.kosmx.emotes.fabric.mixin;

import com.kosmx.emotes.executor.emotePlayer.IUpperPartHelper;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FeatureRenderer.class)
public class FeatureRendererMixin implements IUpperPartHelper {
    private boolean Emotecraft_isUpperPart = true;
    @Override
    public boolean isUpperPart() {
        return this.Emotecraft_isUpperPart;
    }

    @Override
    public void setUpperPart(boolean bl) {
        this.Emotecraft_isUpperPart = bl;
    }
}
