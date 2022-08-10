package io.github.kosmx.emotes.arch.emote;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.opennbs.format.Layer;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;

import java.util.function.Consumer;

public class EmotePlayImpl extends EmotePlayer<ModelPart>{
    public EmotePlayImpl(KeyframeAnimation emote, Consumer<Layer.Note> noteConsumer, int t) {
        super(emote, noteConsumer, t);
    }

    protected void updateBodyPart(BodyPart bodyPart, ModelPart modelPart) {

        modelPart.x = bodyPart.x.getValueAtCurrentTick(modelPart.x);
        modelPart.y = bodyPart.y.getValueAtCurrentTick(modelPart.y);
        modelPart.z = bodyPart.z.getValueAtCurrentTick(modelPart.z);
        modelPart.xRot = bodyPart.pitch.getValueAtCurrentTick(modelPart.xRot);
        modelPart.yRot = bodyPart.yaw.getValueAtCurrentTick(modelPart.yRot);
        modelPart.zRot = bodyPart.roll.getValueAtCurrentTick(modelPart.zRot);

    }

    @Override
    public void stop() {
        super.stop();
        if(this.perspective == 1){
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        }
    }
}
