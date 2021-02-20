package com.kosmx.emotes.testing.common;

import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.tools.Ease;
import com.kosmx.emotes.common.tools.Pair;

import java.util.Random;

public class RandomEmoteData {
    /**
     * Creates two identical random emote.
     * @return Pair
     */
    public static Pair<EmoteData.EmoteBuilder, EmoteData.EmoteBuilder> generateEmotes(){
        Random random = new Random();
        int length = random.nextInt()%1000 + 2000; //make some useable values

        EmoteData.EmoteBuilder builder1 = new EmoteData.EmoteBuilder();
        EmoteData.EmoteBuilder builder2 = new EmoteData.EmoteBuilder();
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
