package io.github.kosmx.emotes.server.serializer.type;

import com.google.gson.JsonParseException;
import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import io.github.kosmx.emotes.executor.EmoteInstance;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class JsonEmoteWrapper implements ISerializer {

    @Override
    public List<KeyframeAnimation> read(InputStream inputStream, String filename) throws EmoteSerializerException {
        try{
            List<KeyframeAnimation> deserialized = AnimationSerializing.deserializeAnimation(inputStream);
            if (deserialized == null) throw new IOException("Can't load emote, " + filename + " is empty.");
            return fixStopTick(deserialized);
        }catch (JsonParseException | IOException e){
            throw new EmoteSerializerException("Exception has occurred", this.getFormatExtension(), e);
        }
    }


    @Override
    public void write(KeyframeAnimation emote, OutputStream outputStream) throws EmoteSerializerException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            AnimationSerializing.writeAnimation(emote, bufferedWriter);

        }catch (Exception e){
            throw new EmoteSerializerException("Exception has occurred", this.getFormatExtension(), e);
        }
    }

    @Override
    public String getFormatExtension() {
        return "json";
    }

    @Override
    public AnimationFormat getFormatType() {
        return AnimationFormat.JSON_EMOTECRAFT;
    }


    private List<KeyframeAnimation> fixStopTick(List<KeyframeAnimation> deserializeAnimation) {
        if (!EmoteInstance.config.autoFixEmoteStop.get()) return deserializeAnimation;
        List<KeyframeAnimation> fixed = new LinkedList<>();
        for (KeyframeAnimation emote: deserializeAnimation) {
            if (emote.endTick + 1 == emote.stopTick && !emote.isInfinite()) {
                KeyframeAnimation.AnimationBuilder builder = emote.mutableCopy();
                int last = 0;
                last = Math.max(last, lastKeyPos(builder.body));
                last = Math.max(last, lastKeyPos(builder.head));
                last = Math.max(last, lastKeyPos(builder.leftArm));
                last = Math.max(last, lastKeyPos(builder.rightArm));
                last = Math.max(last, lastKeyPos(builder.leftLeg));
                last = Math.max(last, lastKeyPos(builder.rightLeg));
                last = Math.max(last, lastKeyPos(builder.torso));
                builder.endTick = last;
                fixed.add(builder.build());
            } else {
                fixed.add(emote);
            }
        }
        return fixed;
    }
    private static int lastKeyPos(KeyframeAnimation.StateCollection part) {
        int last = 0;
        last = Math.max(last, lastKeyPos(part.x));
        last = Math.max(last, lastKeyPos(part.y));
        last = Math.max(last, lastKeyPos(part.z));
        last = Math.max(last, lastKeyPos(part.pitch));
        last = Math.max(last, lastKeyPos(part.yaw));
        last = Math.max(last, lastKeyPos(part.roll));
        if (part.bend != null) last = Math.max(last, lastKeyPos(part.bend));
        if (part.bendDirection != null) last = Math.max(last, lastKeyPos(part.bendDirection));
        return last;
    }
    private static int lastKeyPos(KeyframeAnimation.StateCollection.State part) {
        if (part.getKeyFrames().isEmpty()) return 0;
        return part.getKeyFrames().get(part.getKeyFrames().size() - 1).tick;
    }
}
