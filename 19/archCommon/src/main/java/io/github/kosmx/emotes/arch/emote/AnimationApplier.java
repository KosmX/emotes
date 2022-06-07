package io.github.kosmx.emotes.arch.emote;

import io.github.kosmx.emotes.common.tools.Vec3f;
import io.github.kosmx.playerAnim.TransformType;
import io.github.kosmx.playerAnim.impl.AnimationPlayer;
import io.github.kosmx.playerAnim.layered.IAnimation;
import net.minecraft.client.model.geom.ModelPart;

public class AnimationApplier extends AnimationPlayer {
    public AnimationApplier(IAnimation animation) {
        super(animation);
    }

    public void updatePart(String partName, ModelPart part) {
        Vec3f pos = this.get3DTransform(partName, TransformType.POSITION, new Vec3f(part.x, part.y, part.z));
        part.x = pos.getX();
        part.y = pos.getY();
        part.z = pos.getZ();
        Vec3f rot = this.get3DTransform(partName, TransformType.ROTATION, new Vec3f(part.xRot, part.yRot, part.zRot));
        part.setRotation(rot.getX(), rot.getY(), rot.getZ());
    }

}
