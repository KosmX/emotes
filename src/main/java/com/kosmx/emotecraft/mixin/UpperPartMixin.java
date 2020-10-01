package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.mixinInterface.IUpperPartHelper;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelPart.class)
public class UpperPartMixin implements IUpperPartHelper {
    private boolean Emotecraft_isUpperPart = false;

    @Override
    public boolean isUpperPart(){
        return Emotecraft_isUpperPart;
    }

    @Override
    public void setUpperPart(boolean bl){
        this.Emotecraft_isUpperPart = bl;
    }
}
