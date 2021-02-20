package com.kosmx.emotes.executor.emotePlayer;

import com.kosmx.emotes.common.tools.SetableSupplier;


public interface IMutatedBipedModel<T> {
    T getTorso();

    T getRightArm();

    T getLeftArm();

    T getRightLeg();

    T getLeftLeg();

    void setTorso(T part);

    void setRightArm(T part);

    void setLeftArm(T part);

    void setRightLeg(T part);

    void setLeftLeg(T part);

    void setEmoteSupplier(SetableSupplier<T> emoteSupplier);

    SetableSupplier<T> getEmoteSupplier();
}
