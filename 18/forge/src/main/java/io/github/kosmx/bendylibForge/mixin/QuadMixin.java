package io.github.kosmx.bendylibForge.mixin;

import com.mojang.math.Vector3f;
import io.github.kosmx.bendylibForge.impl.accessors.DirectionMutator;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelPart.Polygon.class)
public class QuadMixin implements DirectionMutator {
    @Mutable
    @Shadow @Final public Vector3f normal;

    @Override
    public void setDirection(Vector3f vec3f) {
        this.normal = vec3f;
    }
}
