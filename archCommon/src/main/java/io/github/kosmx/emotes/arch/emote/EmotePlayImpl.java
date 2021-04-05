package io.github.kosmx.emotes.arch.emote;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.opennbs.format.Layer;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import java.util.function.Consumer;
import net.minecraft.client.model.geom.ModelPart;

public class EmotePlayImpl extends EmotePlayer<ModelPart>{
    public EmotePlayImpl(EmoteData emote, Consumer<Layer.Note> noteConsumer, int t) {
        super(emote, noteConsumer, t);
    }

    @Override
    protected void updateBodyPart(BodyPart bodyPart, ModelPart modelPart) {

        modelPart.x = bodyPart.x.getValueAtCurrentTick(modelPart.x);
        modelPart.y = bodyPart.y.getValueAtCurrentTick(modelPart.y);
        modelPart.z = bodyPart.z.getValueAtCurrentTick(modelPart.z);
        modelPart.xRot = bodyPart.pitch.getValueAtCurrentTick(modelPart.xRot);
        modelPart.yRot = bodyPart.yaw.getValueAtCurrentTick(modelPart.yRot);
        modelPart.zRot = bodyPart.roll.getValueAtCurrentTick(modelPart.zRot);

    }
}
