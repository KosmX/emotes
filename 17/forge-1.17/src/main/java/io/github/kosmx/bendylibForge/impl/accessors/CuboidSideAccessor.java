package io.github.kosmx.bendylibForge.impl.accessors;

import io.github.kosmx.bendylibForge.ModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;

/**
 * For a shader fix. see {@link ModelPartAccessor.Workaround}
 */
public interface CuboidSideAccessor {
    ModelPart.Polygon[] getSides();

    void setSides(ModelPart.Polygon[] polygons);

    void resetSides();

    void doSideSwapping();
}
