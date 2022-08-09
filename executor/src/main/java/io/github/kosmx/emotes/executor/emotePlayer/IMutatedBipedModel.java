package io.github.kosmx.emotes.executor.emotePlayer;


import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.SetableSupplier;

public interface IMutatedBipedModel<T> {
    T getTorso();

    T getRightArm();

    T getLeftArm();

    T getRightLeg();

    T getLeftLeg();

    void setEmoteSupplier(SetableSupplier<AnimationProcessor> emoteSupplier);

    SetableSupplier<AnimationProcessor> getEmoteSupplier();

    default void setTorso(T v){}
    default void setRightArm(T v){}
    default void setLeftArm(T v){}
    default void setRightLeg(T v){}
    default void setLeftLeg(T v){}
}
