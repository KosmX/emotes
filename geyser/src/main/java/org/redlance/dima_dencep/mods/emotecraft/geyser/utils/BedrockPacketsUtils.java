package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import org.cloudburstmc.protocol.bedrock.packet.AnimateEntityPacket;
import org.geysermc.geyser.entity.type.player.AvatarEntity;

public class BedrockPacketsUtils {
    public static final String BOB = "animation.player.bob";

    public static void sendInstantAnimation(String animation, AvatarEntity playerEntity) {
        sendAnimation(animation, "0", playerEntity);
    }

    public static void sendBobAnimation(AvatarEntity playerEntity) {
        sendAnimation(BOB, "1", playerEntity);
    }

    public static void sendAnimation(String animation, String stopExpression, AvatarEntity playerEntity) {
        AnimateEntityPacket animatePacket = new AnimateEntityPacket();
        animatePacket.setAnimation(animation);
        animatePacket.setNextState("default");
        animatePacket.setBlendOutTime(0.0f);
        animatePacket.setStopExpression(stopExpression);
        animatePacket.setController("__runtime_controller");
        animatePacket.getRuntimeEntityIds().add(playerEntity.geyserId());
        playerEntity.getSession().sendUpstreamPacketImmediately(animatePacket);
    }
}
