package io.github.kosmx.bendylibForge.impl.accessors;

import io.github.kosmx.bendylibForge.ModelPartAccessor;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Basic operation to access cuboid in ModelPart
 */
public interface IModelPartAccessor {

    List<ModelPart.Cube> getCuboids();

    Map<String, ModelPart> getChildren(); //easy to search in it :D

    void setWorkaround(ModelPartAccessor.Workaround workaround);

}
