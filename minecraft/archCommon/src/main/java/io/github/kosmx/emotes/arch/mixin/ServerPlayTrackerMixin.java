package io.github.kosmx.emotes.arch.mixin;

import io.github.kosmx.emotes.arch.network.CommonServerNetworkHandler;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public class ServerPlayTrackerMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "addPairing", at = @At(value = "TAIL"))
    private void startTrackingCallback(ServerPlayer serverPlayer, CallbackInfo ci) {
        if (this.entity instanceof ServerPlayer player) {
            CommonServerNetworkHandler.getInstance().playerStartTracking(
                    CommonServerNetworkHandler.getInstance().getPlayerNetworkInstance(player),
                    CommonServerNetworkHandler.getInstance().getPlayerNetworkInstance(serverPlayer)
            );
        }
    }
}
