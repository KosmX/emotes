package com.kosmx.emotecraft.mixin;

import com.kosmx.emotecraft.BendableModelPart;
import com.kosmx.emotecraft.mixinInterface.IMutatedBipedModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityMixin extends AnimalModel implements IMutatedBipedModel {

    @Shadow public ModelPart torso;
    @Shadow public ModelPart rightLeg;
    @Shadow public ModelPart rightArm;
    @Shadow public ModelPart leftLeg;
    @Shadow public ModelPart leftArm;
    protected BendableModelPart mutatedTorso;
    protected BendableModelPart mutatedRightArm;
    protected BendableModelPart mutatedLeftArm;
    protected BendableModelPart mutatedLeftLeg;
    protected BendableModelPart mutatedRightLeg;

    @Inject(method = "<init>(Ljava/util/function/Function;FFII)V", at = @At("RETURN"))
    private void InitInject(Function<Identifier, RenderLayer> texturedLayerFactory, float scale, float pivotY, int textureWidth, int textureHeight, CallbackInfo ci){
        mutatedLeftArm = new BendableModelPart(this.leftArm, true);
        mutatedLeftLeg = new BendableModelPart(this.leftLeg, false);
        mutatedRightArm = new BendableModelPart(this.rightArm, true);
        mutatedRightLeg = new BendableModelPart(this.rightLeg, false);
        mutatedTorso = new BendableModelPart(this.torso, false);

        mutatedTorso.addCuboid(-4.0F, 0.0F, -2.0F, 8, 12, 4, scale, Direction.DOWN);
        mutatedRightLeg.addCuboid(-2, 0, -2, 4, 12, 4, scale, Direction.UP);
        mutatedLeftLeg.addCuboid(-2, 0, -2, 4, 12, 4, scale, Direction.UP);

        mutatedLeftArm.addCuboid(-1, -2, -2, 4, 12, 4, scale, Direction.UP);
        mutatedRightArm.addCuboid(-3, -2, -2, 4, 12, 4, scale, Direction.UP);
    }

    @Override
    public void setEmoteSupplier(BendableModelPart.EmoteSupplier emoteSupplier) {
        this.mutatedLeftLeg.setEmote(emoteSupplier);
        this.mutatedRightLeg.setEmote(emoteSupplier);
        this.mutatedLeftArm.setEmote(emoteSupplier);
        this.mutatedRightArm.setEmote(emoteSupplier);
        this.mutatedTorso.setEmote(emoteSupplier);
    }

    @Shadow protected abstract ModelPart getArm(Arm arm);


    /*
    @Inject(method = "setArmAngle", at = @At("HEAD"), cancellable = true)
    private void setArmInject(Arm arm, MatrixStack matrices, CallbackInfo ci){
        ModelPart armModel = this.getArm(arm);
        if(((IModelPart)armModel).getActiveMutatedPart())
    }
    TODO
     */



    @Override
    public BendableModelPart getTorso() {
        return mutatedTorso;
    }

    @Override
    public BendableModelPart getRightArm() {
        return mutatedRightArm;
    }

    @Override
    public BendableModelPart getLeftArm() {
        return mutatedLeftArm;
    }

    @Override
    public BendableModelPart getRightLeg() {
        return mutatedRightLeg;
    }

    @Override
    public BendableModelPart getLeftLeg() {
        return mutatedLeftLeg;
    }

    @Override
    public void setTorso(BendableModelPart part) {
        mutatedTorso = part;
    }

    @Override
    public void setRightArm(BendableModelPart part) {
        mutatedRightArm = part;
    }

    @Override
    public void setLeftArm(BendableModelPart part) {
        mutatedLeftArm = part;
    }

    @Override
    public void setRightLeg(BendableModelPart part) {
        mutatedRightLeg = part;
    }

    @Override
    public void setLeftLeg(BendableModelPart part) {
        mutatedLeftLeg = part;
    }
}
