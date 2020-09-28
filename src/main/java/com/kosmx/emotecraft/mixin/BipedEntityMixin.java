package com.kosmx.emotecraft.mixin;

import com.kosmx.bendylib.IModelPart;
import com.kosmx.emotecraft.BendableModelPart;
import com.kosmx.emotecraft.Emote;
import com.kosmx.emotecraft.math.Helper;
import com.kosmx.emotecraft.mixinInterface.IUpperPartHelper;
import com.kosmx.emotecraft.mixinInterface.IMutatedBipedModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(BipedEntityModel.class)
public abstract class BipedEntityMixin<T extends LivingEntity> extends AnimalModel<T> implements IMutatedBipedModel {

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
    protected BendableModelPart.EmoteSupplier emote;

    @Inject(method = "<init>(Ljava/util/function/Function;FFII)V", at = @At("RETURN"))
    private void InitInject(Function<Identifier, RenderLayer> texturedLayerFactory, float scale, float pivotY, int textureWidth, int textureHeight, CallbackInfo ci){
        mutatedLeftArm = new BendableModelPart(this.leftArm, true);
        mutatedLeftLeg = new BendableModelPart(this.leftLeg, false);
        mutatedRightArm = new BendableModelPart(this.rightArm, true);
        mutatedRightLeg = new BendableModelPart(this.rightLeg, false);
        mutatedTorso = new BendableModelPart(this.torso, false);
        ((IUpperPartHelper)this.head).setUpperPart(true);
        ((IUpperPartHelper)this.helmet).setUpperPart(true);

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
        this.emote = emoteSupplier;
    }

    @Inject(method = "setAttributes", at = @At("RETURN"))
    private void copyMutatedAttributes(BipedEntityModel<T> bipedEntityModel, CallbackInfo ci){
        if(emote != null){
            if(((IMutatedBipedModel)bipedEntityModel).getEmoteSupplier() != emote)((IMutatedBipedModel)bipedEntityModel).setEmoteSupplier(emote);
            if(Emote.isRunningEmote(this.emote.get())){
                IMutatedBipedModel thisWithMixin = (IMutatedBipedModel) bipedEntityModel;
                Emote playedEmote = emote.get();
                thisWithMixin.getTorso().bend(playedEmote.torso.getBend());
                thisWithMixin.getLeftArm().bend(playedEmote.leftArm.getBend());
                thisWithMixin.getLeftLeg().bend(playedEmote.leftLeg.getBend());
                thisWithMixin.getRightArm().bend(playedEmote.rightArm.getBend());
                thisWithMixin.getRightLeg().bend(playedEmote.rightLeg.getBend());
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        if(((IModelPart)this.torso).getActiveMutatedPart()==this.mutatedTorso && mutatedTorso.getEmote()!= null && Emote.isRunningEmote(mutatedTorso.getEmote().get())){
            this.getHeadParts().forEach((part)->{
                if(!((IUpperPartHelper)part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });
            this.getBodyParts().forEach((part)->{
                if(!((IUpperPartHelper)part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });

            BendableModelPart.EmoteSupplier emoteSupplier = this.mutatedTorso.getEmote();
            matrices.push();
            Helper.roteteMatrixStack(matrices, emoteSupplier.get().torso.getBend());
            this.getHeadParts().forEach((part)->{
                if(((IUpperPartHelper)part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });
            this.getBodyParts().forEach((part)->{
                if(((IUpperPartHelper)part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });
            matrices.pop();
        }
        else super.render(matrices, vertices, light, overlay, red, green, blue, alpha);
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


    @Shadow protected abstract Iterable<ModelPart> getHeadParts();

    @Shadow protected abstract Iterable<ModelPart> getBodyParts();

    @Shadow public ModelPart head;

    @Shadow public ModelPart helmet;

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

    @Override
    public BendableModelPart.EmoteSupplier getEmoteSupplier() {
        return emote;
    }
}
