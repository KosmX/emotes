package com.kosmx.emotes.testing.common;

import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.tools.Ease;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Assertions;
import java.util.Random;

public class EmoteDataHashingTest {

    @RepeatedTest(10)
    @DisplayName("emoteData hashing test")
    public void hashAndEqualsTest(){
        Random random = new Random();
        int length = random.nextInt()%1000 + 2000; //make some useable values

        EmoteData.EmoteBuilder builder1 = new EmoteData.EmoteBuilder();
        EmoteData.EmoteBuilder builder2 = new EmoteData.EmoteBuilder();
        builder1.endTick = length;
        builder2.endTick = length;

        for(int i = 0; i < 10; i++) {
            int pos = Math.abs(random.nextInt() % length);
            float val = Math.abs(random.nextInt() % length);
            Ease ease = Ease.getEase((byte) (random.nextInt() % 48));
            builder1.rightArm.x.addKeyFrame(pos, val, ease);
            builder2.rightArm.x.addKeyFrame(pos, val, ease);
        }

        EmoteData emote1 = builder1.build();
        EmoteData emote2 = builder2.build();

        Assertions.assertEquals(emote1, emote2, "EmoteData should equal with the a perfect copy"); //Object are not the same, but should be equal
        Assertions.assertEquals(emote1.hashCode(), emote2.hashCode(), "The hash should be same");

        emote1.head.y.addKeyFrame(random.nextInt()&length, random.nextFloat(), Ease.getEase((byte) (random.nextInt() % 48)));

        Assertions.assertNotEquals(emote1, emote2, "After any change these should be NOT equals");
        Assertions.assertNotEquals(emote1.hashCode(), emote2.hashCode(), "After any change these should have different hash");

    }
}
