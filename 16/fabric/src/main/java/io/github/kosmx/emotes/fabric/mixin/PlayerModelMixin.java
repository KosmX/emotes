package io.github.kosmx.emotes.fabric.mixin;

import io.github.kosmx.emotes.common.tools.SetableSupplier;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.executor.emotePlayer.IMutatedBipedModel;
import io.github.kosmx.emotes.fabric.BendableModelPart;
import io.github.kosmx.emotes.arch.emote.EmotePlayImpl;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unchecked")
@Mixin(value = PlayerModel.class, priority = 2000)//Apply after NotEnoughAnimation's inject
public class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {
    @Shadow
    @Final
    public ModelPart jacket;
    @Shadow
    @Final
    public ModelPart rightSleeve;
    @Shadow
    @Final
    public ModelPart leftSleeve;
    @Shadow @Final public ModelPart rightPants;
    @Shadow @Final public ModelPart leftPants;
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
        this.mutatedRightPantLeg = new BendableModelPart(this.rightPants, emoteSupplier);
        this.mutatedLeftPantLeg = new BendableModelPart(this.leftPants, emoteSupplier);

        thisWithMixin.setLeftArm(new BendableModelPart(this.leftArm, true));
        thisWithMixin.setRightArm(new BendableModelPart(this.rightArm, true));

        thisWithMixin.setEmoteSupplier(emoteSupplier);

        thisWithMixin.setLeftLeg(new BendableModelPart(this.leftLeg, false, emoteSupplier));
        thisWithMixin.getLeftLeg().addCuboid(- 2, 0, - 2, 4, 12, 4, scale, Direction.UP);

        mutatedJacket.addCuboid(- 4, 0, - 2, 8, 12, 4, scale + 0.25f, Direction.DOWN);
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
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.rightLeg.setPos(- 1.9F, 12.0F, 0.0F);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.rightArm.z = 0.0F;
        this.rightArm.x = - 5.0F;
        this.leftArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        this.body.xRot = 0.0F;
        this.rightLeg.z = 0.1F;
        this.leftLeg.z = 0.1F;
        this.rightLeg.y = 12.0F;
        this.leftLeg.y = 12.0F;
        this.head.y = 0.0F;
        this.head.zRot = 0f;
        this.body.y = 0.0F;
    }

    @Inject(method = "setupAnim", at = @At(value = "HEAD"))
    private void setDefaultBeforeRender(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci){
        setDefaultPivot(); //to not make everything wrong
    }

    @Inject(method = "setupAnim", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;copyFrom(Lnet/minecraft/client/model/geom/ModelPart;)V", ordinal = 0))
    private void setEmote(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci){
        if(livingEntity instanceof AbstractClientPlayer && ((IEmotePlayerEntity<EmotePlayImpl>)livingEntity).isPlayingEmote()){
            EmotePlayImpl emote = ((IEmotePlayerEntity<EmotePlayImpl>) livingEntity).getEmote();
            emoteSupplier.set(emote);
            emote.head.updateBodyPart(this.head);
            this.hat.copyFrom(this.head);
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
