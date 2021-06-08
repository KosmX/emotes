package io.github.kosmx.emotes.executor.emotePlayer;

import io.github.kosmx.emotes.common.tools.SetableSupplier;


public interface IMutatedBipedModel<T, E extends IEmotePlayer> {
    T getTorso();

    T getRightArm();

    T getLeftArm();

    T getRightLeg();

    T getLeftLeg();

    void setEmoteSupplier(SetableSupplier<E> emoteSupplier);

    SetableSupplier<E> getEmoteSupplier();
}
