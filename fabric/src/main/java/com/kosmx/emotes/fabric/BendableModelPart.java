package com.kosmx.emotes.fabric;

import com.kosmx.bendylib.IModelPart;
import com.kosmx.bendylib.MutableModelPart;
import com.kosmx.bendylib.objects.BendableCuboid;
import com.kosmx.emotes.common.CommonData;
import com.kosmx.emotes.common.tools.Pair;
import com.kosmx.emotes.common.tools.SetableSupplier;
import com.kosmx.emotes.executor.emotePlayer.IUpperPartHelper;
import com.kosmx.emotes.fabric.emote.EmotePlayImpl;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Matrix4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//Until I don't have to modify bendy-lib, this will work properly
public class BendableModelPart extends MutableModelPart {
    @Nullable
    protected SetableSupplier<EmotePlayImpl> emote;
    protected float axis = 0;
    protected float angl = 0;

    //Render after or before bending the torso...
    protected boolean isUpperPart = false;

    public BendableModelPart(ModelPart modelPart, boolean isUpperPart, @Nullable SetableSupplier<EmotePlayImpl> emote){
        super(modelPart);
        this.emote = emote;
        this.isUpperPart = isUpperPart;
        ((IModelPart) modelPart).mutate(this);
        ((IUpperPartHelper) modelPart).setUpperPart(isUpperPart);
    }

    public BendableModelPart(ModelPart modelPart, @Nullable SetableSupplier<EmotePlayImpl> emote){
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
     * This mod has always max priority, but not always active.
     *
     * @return 0
     */
    @Override
    public int getPriority(){
        return 0;
    }

    public Matrix4f getMatrix4f(){
        return ((BendableCuboid) this.iCuboids.get(0)).getLastPosMatrix();
    }

    public BendableCuboid getCuboid(){
        return (BendableCuboid) this.iCuboids.get(0);
    }

    @Override
    public boolean isActive(){
        return this.emote != null && EmotePlayImpl.isRunningEmote(this.emote.get());
    }

    public void setEmote(@Nullable SetableSupplier<EmotePlayImpl> emote){
        this.emote = emote;
    }

    @Nullable
    public SetableSupplier<EmotePlayImpl> getEmote(){
        return emote;
    }

    public void bend(float a, float b){
        this.axis = a;
        this.angl = b;
        ((BendableCuboid) this.iCuboids.get(0)).setRotationRad(a, b);
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


    public static void roteteMatrixStack(MatrixStack matrices, Pair<Float, Float> pair){
        float offset = 0.375f;
        matrices.translate(0, offset, 0);
        float bend = pair.getRight();
        float axisf = - pair.getLeft();
        Vector3f axis = new Vector3f((float) Math.cos(axisf), 0, (float) Math.sin(axisf));
        //return this.setRotation(axis.getRadialQuaternion(bend));
        matrices.multiply(axis.getRadialQuaternion(bend));
        matrices.translate(0, - offset, 0);
    }
}
