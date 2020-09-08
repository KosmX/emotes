package com.kosmx.emotecraft.mixin;


import com.kosmx.emotecraft.BendableModelPart;
import com.kosmx.emotecraft.Emote;
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
    @Shadow @Final public ModelPart leftSleeve;
    @Shadow @Final public ModelPart rightPantLeg;
    @Shadow @Final public ModelPart leftPantLeg;
    public BendableModelPart.EmoteSupplier emoteSupplier = new BendableModelPart.EmoteSupplier();
    private BendableModelPart mutatedTorso;
    private BendableModelPart mutatedJacket;
    private BendableModelPart mutatedRightArm;
    private BendableModelPart mutatedRightSleeve;
    private BendableModelPart mutatedLeftArm;
    private BendableModelPart mutatedLeftSleeve;
    private BendableModelPart mutatedRightLeg;
    private BendableModelPart mutatedRightPantLeg;
    private BendableModelPart mutatedLeftLeg;
    private BendableModelPart mutatedLeftPantLeg;
    //private MutableModelPart head :D ... it were be fun XD

    public PlayerModelMixin(float scale) {
        super(scale);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initBendableStuff(float scale, boolean thinArms, CallbackInfo ci) {
        this.mutatedTorso = new BendableModelPart(this.torso, emoteSupplier);
        this.mutatedJacket = new BendableModelPart(this.jacket, emoteSupplier);
        this.mutatedRightArm = new BendableModelPart(this.rightArm, emoteSupplier);
        this.mutatedRightSleeve = new BendableModelPart(this.rightSleeve, emoteSupplier);
        this.mutatedLeftArm = new BendableModelPart(this.leftArm, emoteSupplier);
        this.mutatedLeftSleeve = new BendableModelPart(this.leftSleeve, emoteSupplier);
        this.mutatedRightLeg = new BendableModelPart(this.rightLeg, emoteSupplier);
        this.mutatedRightPantLeg = new BendableModelPart(this.rightPantLeg, emoteSupplier);
        this.mutatedLeftLeg = new BendableModelPart(this.leftLeg, emoteSupplier);
        this.mutatedLeftPantLeg = new BendableModelPart(this.leftPantLeg, emoteSupplier);
        mutatedTorso.addCuboid(-4.0F, 0.0F, -2.0F, 8, 12, 4, scale, Direction.DOWN);
        mutatedRightLeg.addCuboid(-2, 0, -2, 4, 12, 4, scale, Direction.UP);
        mutatedLeftLeg.addCuboid(-2, 0, -2, 4, 12, 4, scale, Direction.UP);
        mutatedJacket.addCuboid(-4.0F, 0.0F, -2.0F, 8, 12, 4, scale + 0.25f, Direction.DOWN);
        mutatedRightPantLeg.addCuboid(-2, 0, -2, 4, 12, 4, scale + 0.25f, Direction.UP);
        mutatedLeftPantLeg.addCuboid(-2, 0, -2, 4, 12, 4, scale + 0.25f, Direction.UP);
        if(thinArms){
            mutatedLeftArm.addCuboid(-1, -2, -2, 3, 12, 4, scale, Direction.UP);
            mutatedRightArm.addCuboid(-2, -2, -2, 3, 12, 4, scale, Direction.UP);
            mutatedLeftSleeve.addCuboid(-1, -2, -2, 3, 12, 4, scale + 0.25f, Direction.UP);
            mutatedRightSleeve.addCuboid(-2, -2, -2, 3, 12, 4, scale + 0.25f, Direction.UP);
        }
        else {
            mutatedLeftArm.addCuboid(-1, -2, -2, 4, 12, 4, scale, Direction.UP);
            mutatedRightArm.addCuboid(-3, -2, -2, 4, 12, 4, scale, Direction.UP);
            mutatedLeftSleeve.addCuboid(-1, -2, -2, 4, 12, 4, scale + 0.25f, Direction.UP);
            mutatedRightSleeve.addCuboid(-3, -2, -2, 4, 12, 4, scale + 0.25f, Direction.UP);
        }
        //TODO some bendable armor...
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
    }

    @Redirect(method = "setAngles", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V"))
    private void setEmote(BipedEntityModel<?> idk,T livingEntity, float f, float g, float h, float i, float j){
        setDefaultPivot();  //to not make everything wrong
        super.setAngles(livingEntity, f, g, h, i, j);
        if(livingEntity instanceof AbstractClientPlayerEntity && Emote.isRunningEmote(((EmotePlayerInterface)livingEntity).getEmote())){
            Emote emote = ((EmotePlayerInterface) livingEntity).getEmote();
            if(emote != emoteSupplier.get()) emoteSupplier.set(emote);
            emote.head.setBodyPart(this.head);
            this.helmet.copyPositionAndRotation(this.head);
            emote.leftArm.setBodyPart(this.leftArm);
            emote.rightArm.setBodyPart(this.rightArm);
            emote.leftLeg.setBodyPart(this.leftLeg);
            emote.rightLeg.setBodyPart(this.rightLeg);

            mutatedTorso.bend(emote.torso.getBend());
            mutatedLeftArm.bend(emote.leftArm.getBend());
            mutatedLeftLeg.bend(emote.leftLeg.getBend());
            mutatedRightArm.bend(emote.rightArm.getBend());
            mutatedRightLeg.bend(emote.rightLeg.getBend());

            mutatedJacket.copyBend(mutatedTorso);
            mutatedLeftPantLeg.copyBend(mutatedLeftLeg);
            mutatedRightPantLeg.copyBend(mutatedRightLeg);
            mutatedLeftSleeve.copyBend(mutatedLeftArm);
            mutatedRightSleeve.copyBend(mutatedRightArm);
        }
    }
}
