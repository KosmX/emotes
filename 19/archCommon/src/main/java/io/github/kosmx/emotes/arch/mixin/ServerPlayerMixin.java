package io.github.kosmx.emotes.arch.mixin;

import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class ServerPlayerMixin extends LivingEntity {

    protected ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "updatePlayerPose", at = @At(value = "TAIL"))
    private void updatePlayerPoseEvent(CallbackInfo ci) {
        if (!this.level.isClientSide()) {
            if (this.getPose() == Pose.CROUCHING || getPose() == Pose.DYING || getPose() == Pose.SWIMMING || getPose() == Pose.FALL_FLYING || getPose() == Pose.SLEEPING) {
                AbstractServerEmotePlay.getInstance().playerEntersInvalidPose(this);
            }
        }
    }
}
