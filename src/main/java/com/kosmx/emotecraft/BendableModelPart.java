package com.kosmx.emotecraft;

import com.kosmx.bendylib.IModelPart;
import com.kosmx.bendylib.MutableModelPart;
import com.kosmx.bendylib.objects.BendableCuboid;
import net.minecraft.client.model.ModelPart;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Matrix4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BendableModelPart extends MutableModelPart {

    @Nullable
    protected EmoteSupplier emote;
    protected float axis = 0;
    protected float angl = 0;

    //Render after or before bending the torso...
    protected boolean isUpperPart = false;

    public BendableModelPart(ModelPart modelPart, boolean isUpperPart, @Nullable EmoteSupplier emote) {
        super(modelPart);
        this.emote = emote;
        this.isUpperPart = isUpperPart;
        ((IModelPart)modelPart).mutate(this);
    }

    public BendableModelPart(ModelPart modelPart, @Nullable EmoteSupplier emote){
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
    public String modId() {
        return Main.MOD_NAME;
    }


    /**
     * This mod has always max priority, but not always active.
     * @return 0
     */
    @Override
    public int getPriority() {
        return 0;
    }

    public Matrix4f getMatrix4f(){
        return ((BendableCuboid)this.iCuboids.get(0)).getLastPosMatrix();
    }

    public BendableCuboid getCuboid(){
        return (BendableCuboid)this.iCuboids.get(0);
    }

    @Override
    public boolean isActive() {
        return this.emote != null && Emote.isRunningEmote(this.emote.get());
    }

    public void setEmote(@Nullable EmoteSupplier emote){
        this.emote = emote;
    }

    public void bend(float a, float b){
        this.axis = a;
        this.angl = b;
        ((BendableCuboid)this.iCuboids.get(0)).setRotationRad(a, b);
    }

    public void bend(Pair<Float, Float> pair){
        this.bend(pair.getLeft(), pair.getRight());
    }

    public void copyBend(@Nonnull BendableModelPart mutableModelPart){
        this.bend(mutableModelPart.axis, mutableModelPart.angl);
    }

    public boolean isUpperPart() {
        return isUpperPart;
    }

    public static class EmoteSupplier implements Supplier<Emote>{
        @Nullable
        Emote emote;

        @Override
        @Nullable
        public Emote get() {
            return this.emote;
        }

        public void set(@Nullable Emote emote){
            this.emote = emote;
        }
    }
}
