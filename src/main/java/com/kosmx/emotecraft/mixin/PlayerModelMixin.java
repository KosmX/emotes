package com.kosmx.emotecraft.mixin;


import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.playerInterface.ClientPlayerEmotes;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntityModel.class)
public class PlayerModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {


    public PlayerModelMixin(float scale) {
        super(scale);
    }

    private void setPivot(){
        this.rightArm.pivotZ = 0.0F;
        this.rightArm.pivotX = -5.0F;
        this.leftArm.pivotZ = 0.0F;
        this.leftArm.pivotX = 5.0F;
        this.torso.pitch = 0.0F;
        this.rightLeg.pivotZ = 0.1F;
        this.leftLeg.pivotZ = 0.1F;
        this.rightLeg.pivotY = 12.0F;
        this.leftLeg.pivotY = 12.0F;
        this.head.pivotY = 0.0F;
        this.torso.pivotY = 0.0F;
        this.leftArm.pivotY = 2.0F;
        this.rightArm.pivotY = 2.0F;
    }

    @Redirect(method = "setAngles", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V"))
    private void setEmote(BipedEntityModel idk,T livingEntity, float f, float g, float h, float i, float j){
        if(((ClientPlayerEmotes)livingEntity).isPlayingEmote()){
            Emote emote = ((ClientPlayerEmotes) livingEntity).getEmote();
            //TODO
        }
        else {
            super.setAngles(livingEntity, f, g, h, i, j);
        }
    }
}
