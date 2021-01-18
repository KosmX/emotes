package com.kosmx.emotecraft.mixinInterface;

public interface IEmotecraftPresence {
    /**
     * @return true, if the other side has emotecraft installed
     */
    boolean hasEmotecraftInstalled();
    void setEmotecraftInstalled(boolean bl);
}
