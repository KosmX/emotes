package com.kosmx.emotes.proxy;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.Perspective;
import pm.c7.perspective.PerspectiveMod;

public class PerspectiveReduxProxy {

    public static void setPerspective(boolean bl){
        PerspectiveMod.INSTANCE.perspectiveEnabled = bl;
        PerspectiveMod.INSTANCE.cameraPitch = MinecraftClient.getInstance().player.pitch;
        PerspectiveMod.INSTANCE.cameraYaw = MinecraftClient.getInstance().player.yaw;
        MinecraftClient.getInstance().options.setPerspective(bl ? Perspective.THIRD_PERSON_BACK : Perspective.FIRST_PERSON);
    }

    public static boolean getPerspective(){
        return PerspectiveMod.INSTANCE.perspectiveEnabled;
    }
}
