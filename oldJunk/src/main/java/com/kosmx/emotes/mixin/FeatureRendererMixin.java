package com.kosmx.emotes.mixin;


import com.kosmx.emotes.mixinInterface.IUpperPartHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(FeatureRenderer.class)
public class FeatureRendererMixin implements IUpperPartHelper {
    private boolean Emotecraft_isUpperPart = true;

    @Override
    public boolean isUpperPart(){
        return Emotecraft_isUpperPart;
    }

    @Override
    public void setUpperPart(boolean bl){
        Emotecraft_isUpperPart = bl;
    }
}
