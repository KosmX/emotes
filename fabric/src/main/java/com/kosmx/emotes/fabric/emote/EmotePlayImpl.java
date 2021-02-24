package com.kosmx.emotes.fabric.emote;

import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.opennbs.format.Layer;
import com.kosmx.emotes.main.emotePlay.EmotePlayer;
import net.minecraft.client.model.ModelPart;

import java.util.function.Consumer;

public class EmotePlayImpl extends EmotePlayer<ModelPart>{
    public EmotePlayImpl(EmoteData emote, Consumer<Layer.Note> noteConsumer) {
        super(emote, noteConsumer);
    }

    @Override
    protected void updateBodyPart(BodyPart bodyPart, ModelPart modelPart) {

        modelPart.pivotX = bodyPart.x.getValueAtCurrentTick(modelPart.pivotX);
        modelPart.pivotY = bodyPart.y.getValueAtCurrentTick(modelPart.pivotY);
        modelPart.pivotZ = bodyPart.z.getValueAtCurrentTick(modelPart.pivotZ);
        modelPart.pitch = bodyPart.pitch.getValueAtCurrentTick(modelPart.pitch);
        modelPart.yaw = bodyPart.yaw.getValueAtCurrentTick(modelPart.yaw);
        modelPart.roll = bodyPart.roll.getValueAtCurrentTick(modelPart.roll);

    }
}
