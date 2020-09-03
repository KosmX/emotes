package com.kosmx.bendylib;

import javax.annotation.Nullable;

public interface IModelPart {

    /**
     * Swap the Model part with a MutableModelPart
     * @param part This will be renderer
     * @return is the change success
     */
    boolean mutate(MutableModelPart part);

    /**
     * @param part remove, if this is the active mutated.
     * @return action success
     * use {@link IModelPart#getMutatedPart()} to remove the active
     */
    boolean removeMutate(MutableModelPart part);

    /**
     * @return The active mutated part
     */
    @Nullable
    MutableModelPart getMutatedPart();
}
