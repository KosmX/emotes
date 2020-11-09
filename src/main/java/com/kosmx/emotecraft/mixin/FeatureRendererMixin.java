package com.kosmx.emotecraft.mixin;


import com.kosmx.emotecraft.mixinInterface.IUpperPartHelper;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

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
