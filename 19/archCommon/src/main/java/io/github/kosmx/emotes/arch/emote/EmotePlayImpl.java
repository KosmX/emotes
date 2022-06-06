package io.github.kosmx.emotes.arch.emote;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.opennbs.format.Layer;
import io.github.kosmx.emotes.main.emotePlay.EmotePlayer;
import java.util.function.Consumer;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;

public class EmotePlayImpl extends EmotePlayer<ModelPart>{
    public EmotePlayImpl(EmoteData emote, Consumer<Layer.Note> noteConsumer, int t) {
        super(emote, noteConsumer, t);
    }

    @Override
    public void stop() {
        super.stop();
        if(this.perspective == 1){
            Minecraft.getInstance().options.setCameraType(CameraType.FIRST_PERSON);
        }
    }
}
