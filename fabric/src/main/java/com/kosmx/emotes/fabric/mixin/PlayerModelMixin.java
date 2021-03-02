package com.kosmx.emotes.fabric.mixin;

import com.kosmx.emotes.common.tools.SetableSupplier;
import com.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import com.kosmx.emotes.executor.emotePlayer.IMutatedBipedModel;
import com.kosmx.emotes.fabric.BendableModelPart;
import com.kosmx.emotes.fabric.emote.EmotePlayImpl;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unchecked")
@Mixin(value = PlayerEntityModel.class, priority = 2000)//Apply after NotEnoughAnimation's inject
public class PlayerModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {
    @Shadow
    @Final
    public ModelPart jacket;
    @Shadow
    @Final
    public ModelPart rightSleeve;
    @Shadow
    @Final
    public ModelPart leftSleeve;
    @Shadow
    @Final
    public ModelPart rightPantLeg;
    @Shadow
    @Final
    public ModelPart leftPantLeg;
    public SetableSupplier<EmotePlayImpl> emoteSupplier = new SetableSupplier<>();
    //private BendableModelPart mutatedTorso;
    private BendableModelPart mutatedJacket;
    private BendableModelPart mutatedRightSleeve;
    private BendableModelPart mutatedLeftSleeve;
    private BendableModelPart mutatedRightPantLeg;
    private BendableModelPart mutatedLeftPantLeg;
    //private MutableModelPart head :D ... it were be funny XD
    private IMutatedBipedModel<BendableModelPart, EmotePlayImpl> thisWithMixin;


    public PlayerModelMixin(float scale){
        super(scale);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initBendableStuff(float scale, boolean thinArms, CallbackInfo ci){
        thisWithMixin = (IMutatedBipedModel<BendableModelPart, EmotePlayImpl>) this;
        emoteSupplier.set(null);
        this.mutatedJacket = new BendableModelPart(this.jacket, false, emoteSupplier);
        this.mutatedRightSleeve = new BendableModelPart(this.rightSleeve, true, emoteSupplier);
        this.mutatedLeftSleeve = new BendableModelPart(this.leftSleeve, true, emoteSupplier);
        this.mutatedRightPantLeg = new BendableModelPart(this.rightPantLeg, emoteSupplier);
        this.mutatedLeftPantLeg = new BendableModelPart(this.leftPantLeg, emoteSupplier);

        thisWithMixin.setLeftArm(new BendableModelPart(this.leftArm, true));
        thisWithMixin.setRightArm(new BendableModelPart(this.rightArm, true));

        thisWithMixin.setEmoteSupplier(emoteSupplier);

        thisWithMixin.setLeftLeg(new BendableModelPart(this.leftLeg, false, emoteSupplier));
        thisWithMixin.getLeftLeg().addCuboid(- 2, 0, - 2, 4, 12, 4, scale, Direction.UP);

        mutatedJacket.addCuboid(- 4.0F, 0.0F, - 2.0F, 8, 12, 4, scale + 0.25f, Direction.DOWN);
        mutatedRightPantLeg.addCuboid(- 2, 0, - 2, 4, 12, 4, scale + 0.25f, Direction.UP);
        mutatedLeftPantLeg.addCuboid(- 2, 0, - 2, 4, 12, 4, scale + 0.25f, Direction.UP);
        if(thinArms){
            thisWithMixin.getLeftArm().addCuboid(- 1, - 2, - 2, 3, 12, 4, scale, Direction.UP);
            thisWithMixin.getRightArm().addCuboid(- 2, - 2, - 2, 3, 12, 4, scale, Direction.UP);
            mutatedLeftSleeve.addCuboid(- 1, - 2, - 2, 3, 12, 4, scale + 0.25f, Direction.UP);
            mutatedRightSleeve.addCuboid(- 2, - 2, - 2, 3, 12, 4, scale + 0.25f, Direction.UP);
        }else{
            thisWithMixin.getLeftArm().addCuboid(- 1, - 2, - 2, 4, 12, 4, scale, Direction.UP);
            thisWithMixin.getRightArm().addCuboid(- 3, - 2, - 2, 4, 12, 4, scale, Direction.UP);
            mutatedLeftSleeve.addCuboid(- 1, - 2, - 2, 4, 12, 4, scale + 0.25f, Direction.UP);
            mutatedRightSleeve.addCuboid(- 3, - 2, - 2, 4, 12, 4, scale + 0.25f, Direction.UP);
        }
    }

    private void setDefaultPivot(){
        this.leftLeg.setPivot(1.9F, 12.0F, 0.0F);
        this.rightLeg.setPivot(- 1.9F, 12.0F, 0.0F);
        this.head.setPivot(0.0F, 0.0F, 0.0F);
        this.rightArm.pivotZ = 0.0F;
        this.rightArm.pivotX = - 5.0F;
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

    @Inject(method = "setAngles", at = @At(value = "HEAD"))
    private void setDefaultBeforeRender(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci){
        setDefaultPivot();
    }

    @Inject(method = "setAngles", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;copyPositionAndRotation(Lnet/minecraft/client/model/ModelPart;)V", ordinal = 0))
    private void setEmote(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci){
        setDefaultPivot();  //to not make everything wrong
        super.setAngles(livingEntity, f, g, h, i, j);
        if(livingEntity instanceof AbstractClientPlayerEntity && ((IEmotePlayerEntity<EmotePlayImpl>)livingEntity).isPlayingEmote()){
            EmotePlayImpl emote = ((IEmotePlayerEntity<EmotePlayImpl>) livingEntity).getEmote();
            emoteSupplier.set(emote);
            emote.head.updateBodyPart(this.head);
            this.helmet.copyPositionAndRotation(this.head);
            emote.leftArm.updateBodyPart(this.leftArm);
            emote.rightArm.updateBodyPart(this.rightArm);
            emote.leftLeg.updateBodyPart(this.leftLeg);
            emote.rightLeg.updateBodyPart(this.rightLeg);

            thisWithMixin.getTorso().bend(emote.torso.getBend());
            thisWithMixin.getLeftArm().bend(emote.leftArm.getBend());
            thisWithMixin.getLeftLeg().bend(emote.leftLeg.getBend());
            thisWithMixin.getRightArm().bend(emote.rightArm.getBend());
            thisWithMixin.getRightLeg().bend(emote.rightLeg.getBend());

            mutatedJacket.copyBend(thisWithMixin.getTorso());
            mutatedLeftPantLeg.copyBend(thisWithMixin.getLeftLeg());
            mutatedRightPantLeg.copyBend(thisWithMixin.getRightLeg());
            mutatedLeftSleeve.copyBend(thisWithMixin.getLeftArm());
            mutatedRightSleeve.copyBend(thisWithMixin.getRightArm());
        }
        else {
            emoteSupplier.set(null);
        }
    }
}
