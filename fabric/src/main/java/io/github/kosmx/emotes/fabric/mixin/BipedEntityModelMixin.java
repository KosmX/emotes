package io.github.kosmx.emotes.fabric.mixin;

import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import io.github.kosmx.emotes.common.tools.SetableSupplier;
import io.github.kosmx.emotes.executor.emotePlayer.IMutatedBipedModel;
import io.github.kosmx.emotes.executor.emotePlayer.IUpperPartHelper;
import io.github.kosmx.emotes.fabric.BendableModelPart;
import io.github.kosmx.emotes.arch.emote.EmotePlayImpl;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.function.Function;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

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
    protected SetableSupplier<EmotePlayImpl> emote = new SetableSupplier<>();

    @Inject(method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;Ljava/util/function/Function;)V", at = @At("RETURN"))
    private void initBend(ModelPart modelPart, Function<ResourceLocation, RenderType> function, CallbackInfo ci){
        ModelPartAccessor.getCuboid(modelPart.getChild("body"), 0).registerMutator("bend", data -> new BendableCuboid.Builder().setDirection(Direction.DOWN).build(data));
        ModelPartAccessor.getCuboid(modelPart.getChild("right_arm"), 0).registerMutator("bend", data -> new BendableCuboid.Builder().setDirection(Direction.UP).build(data));
        ModelPartAccessor.getCuboid(modelPart.getChild("left_arm"), 0).registerMutator("bend", data -> new BendableCuboid.Builder().setDirection(Direction.UP).build(data));
        ModelPartAccessor.getCuboid(modelPart.getChild("right_leg"), 0).registerMutator("bend", data -> new BendableCuboid.Builder().setDirection(Direction.UP).build(data));
        ModelPartAccessor.getCuboid(modelPart.getChild("left_leg"), 0).registerMutator("bend", data -> new BendableCuboid.Builder().setDirection(Direction.UP).build(data));
    }

    @Override
    public void setEmoteSupplier(SetableSupplier<EmotePlayImpl> emoteSupplier){
        this.emote = emoteSupplier;
    }

    @Inject(method = "copyPropertiesTo", at = @At("RETURN"))
    private void copyMutatedAttributes(HumanoidModel<T> bipedEntityModel, CallbackInfo ci){
        if(emote != null) {
            ((IMutatedBipedModel) bipedEntityModel).setEmoteSupplier(emote);
        }
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha){
        if(EmotePlayImpl.isRunningEmote(this.emote.get())){
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

            SetableSupplier<EmotePlayImpl> emoteSupplier = this.emote;
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
    public ModelPart head;

    @Shadow public ModelPart body;

    @Shadow public ModelPart hat;

    @Override
    public BendableModelPart getTorso(){
        return new BendableModelPart(body);
    }

    @Override
    public BendableModelPart getRightArm(){
        return new BendableModelPart(rightArm);
    }

    @Override
    public BendableModelPart getLeftArm(){
        return new BendableModelPart(leftArm);
    }

    @Override
    public BendableModelPart getRightLeg(){
        return new BendableModelPart(rightLeg);
    }

    @Override
    public BendableModelPart getLeftLeg(){
        return new BendableModelPart(leftLeg);
    }

    @Override
    public SetableSupplier<EmotePlayImpl> getEmoteSupplier(){
        return emote;
    }
}
