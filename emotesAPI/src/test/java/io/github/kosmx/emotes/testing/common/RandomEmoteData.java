package io.github.kosmx.emotes.testing.common;

import com.zigythebird.playeranimcore.animation.Animation;
import com.zigythebird.playeranimcore.animation.ExtraAnimationData;
import com.zigythebird.playeranimcore.animation.keyframe.BoneAnimation;
import com.zigythebird.playeranimcore.animation.keyframe.Keyframe;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.loading.UniversalAnimLoader;
import it.unimi.dsi.fastutil.Pair;
import team.unnamed.mocha.parser.ast.FloatExpression;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class RandomEmoteData {
    /**
     * Creates two identical random emote.
     * @return Pair
     */
    public static Pair<Animation, Animation> generateEmotes() {
        Random random = new Random();
        int length = random.nextInt()%1000 + 2000; //make some useable values

        BoneAnimation builder1Bone = new BoneAnimation();
        BoneAnimation builder2Bone = new BoneAnimation();

        int count = random.nextInt()%118 + 128;
        for(int i = 0; i < count; i++) {
            int pos = Math.abs(random.nextInt() % length);
            FloatExpression val = FloatExpression.of(Math.abs(random.nextInt() % length));
            EasingType ease = EasingType.fromId((byte) (random.nextInt() % 48));
            builder1Bone.positionKeyFrames().xKeyframes().add(new Keyframe(pos, Collections.singletonList(val), Collections.singletonList(val), ease));
            builder2Bone.positionKeyFrames().xKeyframes().add(new Keyframe(pos, Collections.singletonList(val), Collections.singletonList(val), ease));
        }

        Animation builder1 = new Animation(new ExtraAnimationData(), length, Animation.LoopType.PLAY_ONCE,
                Collections.singletonMap("head", builder1Bone), UniversalAnimLoader.NO_KEYFRAMES, new HashMap<>(), new HashMap<>()
        );
        Animation builder2 = new Animation(new ExtraAnimationData(), length, Animation.LoopType.PLAY_ONCE,
                Collections.singletonMap("head", builder2Bone), UniversalAnimLoader.NO_KEYFRAMES, new HashMap<>(), new HashMap<>()
        );
        return Pair.of(builder1, builder2);
    }
}