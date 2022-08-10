package io.github.kosmx.emotes.testing.common;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import java.util.Random;

public class EmoteDataHashingTest {

    @RepeatedTest(10)
    @DisplayName("emoteData hashing test")
    public void hashAndEqualsTest(){
        Random random = new Random();

        Pair<KeyframeAnimation.AnimationBuilder, KeyframeAnimation.AnimationBuilder> pair = RandomEmoteData.generateEmotes();

        KeyframeAnimation.AnimationBuilder emote1 = pair.getLeft();
        KeyframeAnimation.AnimationBuilder emote2 = pair.getRight();

        Assertions.assertEquals(emote1.build(), emote2.build(), "EmoteData should equal with the a perfect copy"); //Object are not the same, but should be equal
        Assertions.assertEquals(emote1.build().hashCode(), emote2.build().hashCode(), "The hash should be same");

        emote1.getOrCreatePart("head").y.addKeyFrame(random.nextInt()&emote1.endTick, random.nextFloat(), Ease.getEase((byte) (random.nextInt() % 48)));

        Assertions.assertNotEquals(emote1.build(), emote2.build(), "After any change these should be NOT equals");
        Assertions.assertNotEquals(emote1.build().hashCode(), emote2.build().hashCode(), "After any change these should have different hash");

    }
}
