package io.github.kosmx.emotes.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.MutableCuboid;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import io.github.kosmx.emotes.common.tools.Pair;
import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nullable;
import java.util.function.Consumer;


//Until I don't have to modify bendy-lib, this will work properly
public class BendableModelPart {

    ModelPart modelPart;

    public BendableModelPart(ModelPart modelPart){
        this.modelPart = modelPart;
    }

    public void bend(float a, float b){
        ModelPartAccessor.optionalGetCuboid(modelPart, 0).ifPresent(mutableCuboid -> ((BendableCuboid)mutableCuboid.getAndActivateMutator("bend")).applyBend(a, b));
    }

    public void bend(@Nullable Pair<Float, Float> pair){
        if(pair != null) {
            this.bend(pair.getLeft(), pair.getRight());
        }
        else {
            //ModelPartAccessor.getCuboid(modelPart, 0).getAndActivateMutator(null);
            ModelPartAccessor.optionalGetCuboid(modelPart, 0).ifPresent(mutableCuboid -> mutableCuboid.getAndActivateMutator(null));
        }
    }


    public static void roteteMatrixStack(PoseStack matrices, Pair<Float, Float> pair){
        float offset = 0.375f;
        matrices.translate(0, offset, 0);
        float bend = pair.getRight();
        float axisf = - pair.getLeft();
        Vector3f axis = new Vector3f((float) Math.cos(axisf), 0, (float) Math.sin(axisf));
        //return this.setRotation(axis.getRadialQuaternion(bend));
        matrices.mulPose(axis.rotation(bend));
        matrices.translate(0, - offset, 0);
    }
}
