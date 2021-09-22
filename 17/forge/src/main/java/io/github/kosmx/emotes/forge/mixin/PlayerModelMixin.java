package io.github.kosmx.emotes.forge.mixin;

import io.github.kosmx.bendylibForge.ModelPartAccessor;
import io.github.kosmx.bendylibForge.impl.BendableCuboid;
import io.github.kosmx.emotes.common.tools.SetableSupplier;
import io.github.kosmx.emotes.executor.emotePlayer.IEmotePlayerEntity;
import io.github.kosmx.emotes.executor.emotePlayer.IMutatedBipedModel;
import io.github.kosmx.emotes.executor.emotePlayer.IUpperPartHelper;
import io.github.kosmx.emotes.forge.BendableModelPart;
import io.github.kosmx.emotes.arch.emote.EmotePlayImpl;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

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

    private IMutatedBipedModel<BendableModelPart, EmotePlayImpl> thisWithMixin;

    public PlayerModelMixin(ModelPart modelPart, Function<ResourceLocation, RenderType> function) {
        super(modelPart, function);
    }


    @Inject(method = "<init>", at = @At("RETURN"))
    private void initBendableStuff(ModelPart p_170821_, boolean p_170822_, CallbackInfo ci){
        thisWithMixin = (IMutatedBipedModel<BendableModelPart, EmotePlayImpl>) this;
        emoteSupplier.set(null);

        thisWithMixin.setEmoteSupplier(emoteSupplier);

        addBendMutator(this.jacket, Direction.DOWN);
        addBendMutator(this.rightPants, Direction.UP);
        addBendMutator(this.rightSleeve, Direction.UP);
        addBendMutator(this.leftPants, Direction.UP);
        addBendMutator(this.leftSleeve, Direction.UP);

        ((IUpperPartHelper)rightSleeve).setUpperPart(true);
        ((IUpperPartHelper)leftSleeve).setUpperPart(true);

    }

    private void addBendMutator(ModelPart part, Direction d){
        ModelPartAccessor.optionalGetCuboid(part, 0).ifPresent(mutableCuboid ->mutableCuboid.registerMutator("bend", data -> new BendableCuboid.Builder().setDirection(d).build(data)));
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
        this.body.x = 0f;
        this.body.z = 0f;
        this.body.yRot = 0;
        this.body.zRot = 0;
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
            emote.getPart("torso").updateBodyPart(this.body);

            thisWithMixin.getTorso().bend(emote.torso.getBend());
            thisWithMixin.getLeftArm().bend(emote.leftArm.getBend());
            thisWithMixin.getLeftLeg().bend(emote.leftLeg.getBend());
            thisWithMixin.getRightArm().bend(emote.rightArm.getBend());
            thisWithMixin.getRightLeg().bend(emote.rightLeg.getBend());

        }
        else {
            emoteSupplier.set(null);
            thisWithMixin.getTorso().bend(null);
            thisWithMixin.getLeftArm().bend(null);
            thisWithMixin.getRightArm().bend(null);
            thisWithMixin.getLeftLeg().bend(null);
            thisWithMixin.getRightLeg().bend(null);
        }
    }
}
