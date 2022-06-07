package io.github.kosmx.emotes.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import io.github.kosmx.bendylib.IModelPart;
import io.github.kosmx.bendylib.MutableModelPart;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import io.github.kosmx.emotes.api.Pair;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.tools.SetableSupplier;
import io.github.kosmx.emotes.executor.emotePlayer.IUpperPartHelper;
import io.github.kosmx.playerAnim.impl.AnimationPlayer;
import net.minecraft.client.model.geom.ModelPart;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//Until I don't have to modify bendy-lib, this will work properly
public class BendableModelPart extends MutableModelPart {
    @Nullable
    protected SetableSupplier<AnimationPlayer> emote;
    protected float axis = 0;
    protected float angl = 0;

    //Render after or before bending the torso...
    protected boolean isUpperPart = false;

    public BendableModelPart(ModelPart modelPart, boolean isUpperPart, @Nullable SetableSupplier<AnimationPlayer> emote){
        super(modelPart);
        this.emote = emote;
        this.isUpperPart = isUpperPart;
        ((IModelPart) modelPart).mutate(this);
        ((IUpperPartHelper) modelPart).setUpperPart(isUpperPart);
    }

    public BendableModelPart(ModelPart modelPart, @Nullable SetableSupplier<AnimationPlayer> emote){
        this(modelPart, false, emote);
    }

    public BendableModelPart(ModelPart modelPart){
        this(modelPart, null);
    }

    public BendableModelPart(ModelPart modelPart, boolean isUpperPart){
        this(modelPart, isUpperPart, null);
    }

    /*public void remove(ModelPart modelPart){
        ((IModelPart)modelPart).removeMutate(this);
    }
     */

    @Override
    public String modId(){
        return CommonData.MOD_NAME;
    }


    /**
     * This mod has always 4 priority, but not always active.
     *
     * @return 0
     */
    @Override
    public int getPriority(){
        return 4;
    }

    public Matrix4f getMatrix4f(){
        return ((BendableCuboid) this.iCuboids.get(0)).getLastPosMatrix();
    }

    public BendableCuboid getCuboid(){
        return (BendableCuboid) this.iCuboids.get(0);
    }

    @Override
    public boolean isActive(){
        return this.emote != null && this.emote.get() != null && this.emote.get().isActive() && angl != 0;
    }

    public void setEmote(@Nullable SetableSupplier<AnimationPlayer> emote){
        this.emote = emote;
    }

    @Nullable
    public SetableSupplier<AnimationPlayer> getEmote(){
        return emote;
    }

    public void bend(float a, float b){
        this.axis = a;
        this.angl = b;
        ((BendableCuboid) this.iCuboids.get(0)).applyBend(a, b);
    }

    public void bend(Pair<Float, Float> pair){
        this.bend(pair.getLeft(), pair.getRight());
    }

    public void copyBend(@Nonnull BendableModelPart mutableModelPart){
        this.bend(mutableModelPart.axis, mutableModelPart.angl);
    }

    public boolean isUpperPart(){
        return isUpperPart;
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
