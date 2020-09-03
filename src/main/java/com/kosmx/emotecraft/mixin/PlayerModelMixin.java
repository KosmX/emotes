package com.kosmx.emotecraft.mixin;


import com.kosmx.bendylib.IModelPart;
import com.kosmx.bendylib.MutableModelPart;
import com.kosmx.bendylib.TestClass;
import com.kosmx.bendylib.objects.BendableCuboid;
import com.kosmx.emotecraft.BendableModelPart;
import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.Main;
import com.kosmx.emotecraft.playerInterface.EmotePlayerInterface;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public class PlayerModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {
    @Shadow @Final public ModelPart jacket;
    @Shadow @Final public ModelPart rightSleeve;
    //private MutableModelPart mutatedTorso;
    //private MutableModelPart mutatedJacket;
    private MutableModelPart mutatedRightArm;
    private MutableModelPart mutatedRightArm2;

    public PlayerModelMixin(float scale) {
        super(scale);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initBendableStuff(float scale, boolean thinArms, CallbackInfo ci){
        mutatedRightArm = new BendableModelPart(this.rightArm);
        mutatedRightArm2 = new BendableModelPart(this.rightSleeve);
        //mutatedRightArm.addICuboid(new BendableCuboid(mutatedRightArm.textureOffsetU, mutatedRightArm.textureOffsetV, -4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, false, mutatedRightArm.textureWidth, mutatedRightArm.textureHeight, Direction.UP, 0, 0, 0, scale, scale, scale));
        //mutatedRightArm2.addICuboid(new BendableCuboid(mutatedRightArm2.textureOffsetU, mutatedRightArm2.textureOffsetV, -4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, false, mutatedRightArm2.textureWidth, mutatedRightArm2.textureHeight, Direction.UP, 0,0, 0, scale + 0.25f, scale + 0.25f, scale + 0.25f));
        mutatedRightArm.addICuboid(new BendableCuboid(mutatedRightArm.textureOffsetU, mutatedRightArm.textureOffsetV, -3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, false, mutatedRightArm.textureWidth, mutatedRightArm.textureHeight, Direction.UP, 0, -2, 0, scale, scale, scale));
        mutatedRightArm2.addICuboid(new BendableCuboid(mutatedRightArm2.textureOffsetU, mutatedRightArm2.textureOffsetV, -3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, false, mutatedRightArm2.textureWidth, mutatedRightArm2.textureHeight, Direction.UP, 0,-2, 0, scale + 0.25f, scale + 0.25f, scale + 0.25f));
    }

    private void setDefaultPivot(){
        this.leftLeg.setPivot(1.9F, 12.0F, 0.0F);
        this.rightLeg.setPivot(-1.9F, 12.0F, 0.0F);
        this.head.setPivot(0.0F, 0.0F, 0.0F);
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
        this.head.roll = 0f;
        this.torso.pivotY = 0.0F;
        ((IModelPart)this.rightArm).mutate(mutatedRightArm);
        ((IModelPart)this.rightSleeve).mutate(mutatedRightArm2);
    }

    @Redirect(method = "setAngles", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V"))
    private void setEmote(BipedEntityModel<?> idk,T livingEntity, float f, float g, float h, float i, float j){
        setDefaultPivot();  //to not make everything wrong
        super.setAngles(livingEntity, f, g, h, i, j);
        TestClass.featureTest((PlayerEntityModel) idk);
        if(livingEntity instanceof AbstractClientPlayerEntity && Emote.isRunningEmote(((EmotePlayerInterface)livingEntity).getEmote())){
            Emote emote = ((EmotePlayerInterface) livingEntity).getEmote();
            emote.head.setBodyPart(this.head);
            this.helmet.copyPositionAndRotation(this.head);
            emote.leftArm.setBodyPart(this.leftArm);
            emote.rightArm.setBodyPart(this.rightArm);
            emote.leftLeg.setBodyPart(this.leftLeg);
            emote.rightLeg.setBodyPart(this.rightLeg);
        }
    }
}
