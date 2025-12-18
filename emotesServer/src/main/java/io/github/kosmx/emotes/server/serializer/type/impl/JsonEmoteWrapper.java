package io.github.kosmx.emotes.server.serializer.type.impl;

import com.google.gson.JsonParseException;
import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import io.github.kosmx.emotes.server.serializer.type.EmoteSerializerException;
import io.github.kosmx.emotes.server.serializer.type.IReader;

import java.io.*;
import java.util.Collections;
import java.util.Map;

public class JsonEmoteWrapper implements IReader {
    @Override
    public Map<String, Animation> read(InputStream inputStream, String filename) throws EmoteSerializerException {
        try {
            Map<String, Animation> deserialized = UniversalAnimLoader.loadAnimations(inputStream);
            if (deserialized == null) throw new IOException("Can't load emote, " + filename + " is empty.");
            return Collections.unmodifiableMap(deserialized);
        } catch (JsonParseException | IOException e) {
            throw new EmoteSerializerException("Exception has occurred", getExtension(), e);
        }
    }

    /*@Override
    public void write(Animation emote, OutputStream outputStream, String filename) throws EmoteSerializerException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            AnimationSerializing.writeAnimation(emote, bufferedWriter);
        } catch (Exception e) {
            throw new EmoteSerializerException("Exception has occurred", getExtension(), e);
        }
    }

    @Override
    public boolean onlyEmoteFile() {
        return true;
    }*/

    @Override
    public String getExtension() {
        return "json";
    }

    /*private List<KeyframeAnimation> fixStopTick(List<KeyframeAnimation> deserializeAnimation) {
        if (!Serializer.getConfig().autoFixEmoteStop.get()) return deserializeAnimation;
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
        return part.getKeyFrames().getLast().tick;
    }*/
}
