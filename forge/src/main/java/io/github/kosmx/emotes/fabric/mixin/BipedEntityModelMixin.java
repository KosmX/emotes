package io.github.kosmx.emotes.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.kosmx.bendylib.IModelPart;
import io.github.kosmx.emotes.common.tools.SetableSupplier;
import io.github.kosmx.emotes.executor.emotePlayer.IMutatedBipedModel;
import io.github.kosmx.emotes.executor.emotePlayer.IUpperPartHelper;
import io.github.kosmx.emotes.fabric.BendableModelPart;
import io.github.kosmx.emotes.fabric.emote.EmotePlayImpl;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@SuppressWarnings("unchecked")
@Mixin(HumanoidModel.class)
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements IMutatedBipedModel<BendableModelPart, EmotePlayImpl> {

    @Shadow
    public ModelPart rightLeg;
    @Shadow
    public ModelPart rightArm;
    @Shadow
    public ModelPart leftLeg;
    @Shadow
    public ModelPart leftArm;
    protected BendableModelPart mutatedTorso;
    protected BendableModelPart mutatedRightArm;
    protected BendableModelPart mutatedLeftArm;
    protected BendableModelPart mutatedLeftLeg;
    protected BendableModelPart mutatedRightLeg;
    protected SetableSupplier<EmotePlayImpl> emote;

    @Inject(method = "<init>(Ljava/util/function/Function;FFII)V", at = @At("RETURN"))
    private void InitInject(Function<ResourceLocation, RenderType> texturedLayerFactory, float scale, float pivotY, int textureWidth, int textureHeight, CallbackInfo ci){
        mutatedLeftArm = new BendableModelPart(this.leftArm, true);
        mutatedLeftLeg = new BendableModelPart(this.leftLeg, false);
        mutatedRightArm = new BendableModelPart(this.rightArm, true);
        mutatedRightLeg = new BendableModelPart(this.rightLeg, false);
        mutatedTorso = new BendableModelPart(this.body, false);
        ((IUpperPartHelper) this.head).setUpperPart(true);
        ((IUpperPartHelper) this.hat).setUpperPart(true);

        mutatedTorso.addCuboid(- 4, 0, - 2, 8, 12, 4, scale, Direction.DOWN);
        mutatedRightLeg.addCuboid(- 2, 0, - 2, 4, 12, 4, scale, Direction.UP);
        mutatedLeftLeg.addCuboid(- 2, 0, - 2, 4, 12, 4, scale, Direction.UP);

        mutatedLeftArm.addCuboid(- 1, - 2, - 2, 4, 12, 4, scale, Direction.UP);
        mutatedRightArm.addCuboid(- 3, - 2, - 2, 4, 12, 4, scale, Direction.UP);
    }

    @Override
    public void setEmoteSupplier(SetableSupplier<EmotePlayImpl> emoteSupplier){
        this.mutatedLeftLeg.setEmote(emoteSupplier);
        this.mutatedRightLeg.setEmote(emoteSupplier);
        this.mutatedLeftArm.setEmote(emoteSupplier);
        this.mutatedRightArm.setEmote(emoteSupplier);
        this.mutatedTorso.setEmote(emoteSupplier);
        this.emote = emoteSupplier;
    }

    @Inject(method = "copyPropertiesTo", at = @At("RETURN"))
    private void copyMutatedAttributes(HumanoidModel<T> bipedEntityModel, CallbackInfo ci){
        if(emote != null){
            if(((IMutatedBipedModel) bipedEntityModel).getEmoteSupplier() != emote)
                ((IMutatedBipedModel) bipedEntityModel).setEmoteSupplier(emote);
            if(EmotePlayImpl.isRunningEmote(this.emote.get())){
                IMutatedBipedModel<BendableModelPart, EmotePlayImpl> thisWithMixin = (IMutatedBipedModel) bipedEntityModel;
                EmotePlayImpl playedEmote = emote.get();
                thisWithMixin.getTorso().bend(playedEmote.torso.getBend());
                thisWithMixin.getLeftArm().bend(playedEmote.leftArm.getBend());
                thisWithMixin.getLeftLeg().bend(playedEmote.leftLeg.getBend());
                thisWithMixin.getRightArm().bend(playedEmote.rightArm.getBend());
                thisWithMixin.getRightLeg().bend(playedEmote.rightLeg.getBend());
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha){
        if(((IModelPart) this.body).getActiveMutatedPart() == this.mutatedTorso && mutatedTorso.getEmote() != null && EmotePlayImpl.isRunningEmote(mutatedTorso.getEmote().get())){
            this.headParts().forEach((part)->{
                if(! ((IUpperPartHelper) part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });
            this.bodyParts().forEach((part)->{
                if(! ((IUpperPartHelper) part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });

            SetableSupplier<EmotePlayImpl> emoteSupplier = this.mutatedTorso.getEmote();
            matrices.pushPose();
            BendableModelPart.roteteMatrixStack(matrices, emoteSupplier.get().torso.getBend());
            this.headParts().forEach((part)->{
                if(((IUpperPartHelper) part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });
            this.bodyParts().forEach((part)->{
                if(((IUpperPartHelper) part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });
            matrices.popPose();
        }else super.renderToBuffer(matrices, vertices, light, overlay, red, green, blue, alpha);
    }

    @Shadow
    protected abstract Iterable<ModelPart> headParts();

    @Shadow
    protected abstract Iterable<ModelPart> bodyParts();

    @Shadow
    public ModelPart head;

    @Shadow public ModelPart body;

    @Shadow public ModelPart hat;

    @Override
    public BendableModelPart getTorso(){
        return mutatedTorso;
    }

    @Override
    public BendableModelPart getRightArm(){
        return mutatedRightArm;
    }

    @Override
    public BendableModelPart getLeftArm(){
        return mutatedLeftArm;
    }

    @Override
    public BendableModelPart getRightLeg(){
        return mutatedRightLeg;
    }

    @Override
    public BendableModelPart getLeftLeg(){
        return mutatedLeftLeg;
    }

    @Override
    public void setTorso(BendableModelPart part){
        mutatedTorso = part;
    }

    @Override
    public void setRightArm(BendableModelPart part){
        mutatedRightArm = part;
    }

    @Override
    public void setLeftArm(BendableModelPart part){
        mutatedLeftArm = part;
    }

    @Override
    public void setRightLeg(BendableModelPart part){
        mutatedRightLeg = part;
    }

    @Override
    public void setLeftLeg(BendableModelPart part){
        mutatedLeftLeg = part;
    }

    @Override
    public SetableSupplier<EmotePlayImpl> getEmoteSupplier(){
        return emote;
    }
}
