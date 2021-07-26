package io.github.kosmx.emotes.testing.common;

import io.github.kosmx.emotes.common.emote.EmoteData;
import io.github.kosmx.emotes.common.tools.Ease;
import io.github.kosmx.emotes.api.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Assertions;
import java.util.Random;

public class EmoteDataHashingTest {

    @RepeatedTest(10)
    @DisplayName("emoteData hashing test")
    public void hashAndEqualsTest(){
        Random random = new Random();

        Pair<EmoteData.EmoteBuilder, EmoteData.EmoteBuilder> pair = RandomEmoteData.generateEmotes();

        EmoteData emote1 = pair.getLeft().build();
        EmoteData emote2 = pair.getRight().build();

        Assertions.assertEquals(emote1, emote2, "EmoteData should equal with the a perfect copy"); //Object are not the same, but should be equal
        Assertions.assertEquals(emote1.hashCode(), emote2.hashCode(), "The hash should be same");

        emote1.head.y.addKeyFrame(random.nextInt()&emote1.endTick, random.nextFloat(), Ease.getEase((byte) (random.nextInt() % 48)));

        Assertions.assertNotEquals(emote1, emote2, "After any change these should be NOT equals");
        Assertions.assertNotEquals(emote1.hashCode(), emote2.hashCode(), "After any change these should have different hash");

    }
}
