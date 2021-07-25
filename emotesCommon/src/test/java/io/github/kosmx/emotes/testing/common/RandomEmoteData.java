package io.github.kosmx.emotes.testing.common;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.emote.EmoteFormat;
import io.github.kosmx.emotes.common.tools.Ease;
import io.github.kosmx.emotes.api.Pair;

import java.util.Random;

public class RandomEmoteData {
    /**
     * Creates two identical random emote.
     * @return Pair
     */
    public static Pair<EmoteData.EmoteBuilder, EmoteData.EmoteBuilder> generateEmotes(){
        Random random = new Random();
        int length = random.nextInt()%1000 + 2000; //make some useable values

        EmoteData.EmoteBuilder builder1 = new EmoteData.EmoteBuilder(EmoteFormat.UNKNOWN);
        EmoteData.EmoteBuilder builder2 = new EmoteData.EmoteBuilder(EmoteFormat.UNKNOWN);
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
