package io.github.kosmx.bendylibForge.impl;

import io.github.kosmx.bendylibForge.ICuboidBuilder;
import io.github.kosmx.bendylibForge.MutableCuboid;
import net.minecraft.util.Tuple;

import javax.annotation.Nullable;

@Deprecated
public class DummyCuboid implements MutableCuboid {

    private static boolean bl = true;

    public DummyCuboid(){
        if(bl) {
            System.out.println("A Dummy cuboid was created. that is not good, but can be worse");
            bl = false;
        }
    }
    @Override
    public boolean registerMutator(String name, ICuboidBuilder<ICuboid> builder) {
        return false;
    }

    @Override
    public boolean unregisterMutator(String name) {
        return true;
    }

    @Nullable
    @Override
    public Tuple<String, ICuboid> getActiveMutator() {
        return null;
    }

    @Nullable
    @Override
    public ICuboid getMutator(String name) {
        return null;
    }

    @Nullable
    @Override
    public ICuboid getAndActivateMutator(@Nullable String name) {
        return null;
    }

    @Override
    public void copyStateFrom(MutableCuboid other) {

    }
}
