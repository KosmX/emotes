package io.github.kosmx.emotes.testing.common;

import dev.kosmx.playerAnim.core.data.AnimationFormat;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Pair;

import java.util.Random;

public class RandomEmoteData {
    /**
     * Creates two identical random emote.
     * @return Pair
     */
    public static Pair<KeyframeAnimation.AnimationBuilder, KeyframeAnimation.AnimationBuilder> generateEmotes(){
        Random random = new Random();
        int length = random.nextInt()%1000 + 2000; //make some useable values

        KeyframeAnimation.AnimationBuilder builder1 = new KeyframeAnimation.AnimationBuilder(AnimationFormat.UNKNOWN);
        KeyframeAnimation.AnimationBuilder builder2 = new KeyframeAnimation.AnimationBuilder(AnimationFormat.UNKNOWN);
        builder1.endTick = length;
        builder2.endTick = length;

        int count = random.nextInt()%118 + 128;
        for(int i = 0; i < count; i++) {
            int pos = Math.abs(random.nextInt() % length);
            float val = Math.abs(random.nextInt() % length);
            Ease ease = Ease.getEase((byte) (random.nextInt() % 48));
            builder1.rightArm.x.addKeyFrame(pos, val, ease);
            builder2.rightArm.x.addKeyFrame(pos, val, ease);
        }
        return new Pair<>(builder1, builder2);
    }
}
