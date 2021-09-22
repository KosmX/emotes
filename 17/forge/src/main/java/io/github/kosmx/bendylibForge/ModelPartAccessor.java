package io.github.kosmx.bendylibForge;

import io.github.kosmx.bendylibForge.impl.DummyCuboid;
import io.github.kosmx.bendylibForge.impl.accessors.IModelPartAccessor;
import java.util.*;

import net.minecraft.client.model.geom.ModelPart;

/**
 * Access to children and cuboids in {@link ModelPart}
 * Don't have to reinterpret the object...
 */
public final class ModelPartAccessor {

    public static Map<String,ModelPart> getChildren(ModelPart modelPart){
        return ((IModelPartAccessor)modelPart).getChildren();
    }

    /**
     * Get a cuboid, and cast ist to {@link MutableCuboid}
     * Use {@link ModelPartAccessor#optionalGetCuboid(ModelPart, int)}
     * @param modelPart
     * @param index
     * @return
     */
    @Deprecated
    public static MutableCuboid getCuboid(ModelPart modelPart, int index){
        Optional<MutableCuboid> optionalMutableCuboid = optionalGetCuboid(modelPart, index);
        return optionalMutableCuboid.orElseGet(DummyCuboid::new);
    }

    /**
     * Get a cuboid, and cast it to {@link MutableCuboid}
     *
     * @param modelPart
     * @param index
     * @return
     */
    public static Optional<MutableCuboid> optionalGetCuboid(ModelPart modelPart, int index){
        if(modelPart == null || getCuboids(modelPart) == null || getCuboids(modelPart).size() <= index) return Optional.empty();
        return Optional.of((MutableCuboid)getCuboids(modelPart).get(index));
    }

    public static List<ModelPart.Cube> getCuboids(ModelPart modelPart){
        return ((IModelPartAccessor)modelPart).getCuboids();
    }

    /**
     * Different workarounds to fix shared mod incompatibilities
     * If needed, I advice using {@link Workaround#VanillaDraw}. that is the most stable in any modded environment.
     */
    public enum Workaround {
        ExportQuads, VanillaDraw, None;
    }
}
