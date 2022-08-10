package io.github.kosmx.emotes.testing.common;

import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.common.tools.BiMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class BiMapTest {
    @Test
    @DisplayName("BiMap function verification")
    public void test(){
        BiMap<String, Integer> biMap = new BiMap<>(), biMap1 = new BiMap<>();

        Random random = new Random();

        //basic operations

        Pair<String, Integer> element;

        Pair<String, Integer> nullPair = new Pair<>(null, null);

        element = biMap.put("a", 1);
        Assertions.assertEquals(element, nullPair, "there were no element like that in the map");

        element = biMap.put("b", 2);
        Assertions.assertEquals(element, nullPair, "there were no element like that in the map");

        element = biMap.put("a", 2);
        Assertions.assertEquals(element, new Pair<>("b", 1));


        Assertions.assertFalse(biMap.contains(new Pair<>("a", 1)));
        Assertions.assertFalse(biMap.contains(new Pair<>("b", 2)));
        Assertions.assertTrue(biMap.contains(new Pair<>("a", 2)));
        Assertions.assertEquals(1, biMap.size());

        Assertions.assertEquals(biMap.removeL("a"), 2);


        int i = random.nextInt(1000) + 500; //500-1500 elements. that will be enough
        for(int n = 0; n < i; n++){
            String str = Double.toString(random.nextDouble());
            i = random.nextInt();
            biMap.add(new Pair<>(str, i));
            biMap1.put(str, i);
        }
        Assertions.assertEquals(biMap, biMap1);
        Assertions.assertEquals(biMap.hashCode(), biMap1.hashCode());
    }
}
