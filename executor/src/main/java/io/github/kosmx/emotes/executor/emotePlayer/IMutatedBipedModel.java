package io.github.kosmx.emotes.executor.emotePlayer;

import io.github.kosmx.emotes.common.tools.SetableSupplier;
import io.github.kosmx.playerAnim.impl.AnimationPlayer;


public interface IMutatedBipedModel<T> {
    T getTorso();

    T getRightArm();

    T getLeftArm();

    T getRightLeg();

    T getLeftLeg();

    void setEmoteSupplier(SetableSupplier<AnimationPlayer> emoteSupplier);

    SetableSupplier<AnimationPlayer> getEmoteSupplier();

    default void setTorso(T v){}
    default void setRightArm(T v){}
    default void setLeftArm(T v){}
    default void setRightLeg(T v){}
    default void setLeftLeg(T v){}
}
