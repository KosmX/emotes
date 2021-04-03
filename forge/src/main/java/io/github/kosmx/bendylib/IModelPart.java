package io.github.kosmx.bendylib;

import javax.annotation.Nullable;

public interface IModelPart {

    /**
     * Swap the Model part with a MutableModelPart
     * @param part This will be rendered
     * @return is the change success
     */
    boolean mutate(MutableModelPart part);

    /**
     * @param part remove, if this is the active mutated.
     * @return action success
     * use {@link IModelPart#getActiveMutatedPart()} to remove the active
     */
    boolean removeMutate(MutableModelPart part);

    /**
     * @return The active mutated part
     */
    @Nullable
    MutableModelPart getActiveMutatedPart();

    float getTextureWidth();
    float getTextureHeight();
    int getU();
    int getV();
}
