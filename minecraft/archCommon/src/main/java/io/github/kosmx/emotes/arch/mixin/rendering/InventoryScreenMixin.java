package io.github.kosmx.emotes.arch.mixin.rendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kosmx.emotes.arch.screen.utils.UnsafeRemotePlayer;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @WrapOperation(
            method = "renderEntityInInventory",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
            )
    )
    private static EntityRenderState emotecraft$previews(EntityRenderer<Entity, EntityRenderState> instance, Entity entity, float partialTick, Operation<EntityRenderState> original) {
        if (entity instanceof UnsafeRemotePlayer player) {
            instance.extractRenderState(player, player.reusedState, partialTick);
            return player.reusedState;
        } else {
            return original.call(instance, entity, partialTick);
        }
    }
}
