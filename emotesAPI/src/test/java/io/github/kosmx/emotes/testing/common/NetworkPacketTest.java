package io.github.kosmx.emotes.testing.common;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Pair;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.UUID;

/**
 * Test network data sending and receiving
 */
public class NetworkPacketTest {
    @Test
    @DisplayName("Network protocol test")
    public void netTest() throws IOException {
        Random random = new Random();

        EmotePacket.Builder builder = new EmotePacket.Builder();
        Pair<KeyframeAnimation.AnimationBuilder, KeyframeAnimation.AnimationBuilder> pair = RandomEmoteData.generateEmotes();
        builder.configureToStreamEmote(pair.getLeft().build());
        ByteBuffer byteBuffer = builder.build().write();
        byte[] bytes = byteBuffer.array();

        //The array has been sent, hope, it will arrive correctly.
        //Assume it has happened, create a new ByteBuffer and read it.

        NetData data = new EmotePacket.Builder().build().read(ByteBuffer.wrap(bytes)); //That read expression is kinda funny
        Assertions.assertNotNull(data, "Data should be not null");
        Assertions.assertEquals(pair.getLeft().build(), data.emoteData, "The received data should contain the same emote");
        Assertions.assertEquals(pair.getLeft().build().hashCode(), data.emoteData.hashCode(), "The received data should contain the same emote");


        UUID randID = UUID.randomUUID();
        builder = new EmotePacket.Builder();
        builder.configureToSendStop(randID);
        byteBuffer = builder.build().write();
        bytes = byteBuffer.array();

        //The array has been sent, hope, it will arrive correctly.
        //Assume it has happened, create a new ByteBuffer and read it.

        data = new EmotePacket.Builder().build().read(ByteBuffer.wrap(bytes));
        Assertions.assertEquals(randID, data.stopEmoteID);

        boolean shouldRemainFalse = false;
        try {
            builder = new EmotePacket.Builder();
            builder.configureToSendStop(randID);
            builder.configureToStreamEmote(pair.getLeft().build());
            byteBuffer = builder.build().write();
            bytes = byteBuffer.array();

            //The array has been sent, hope, it will arrive correctly.
            //Assume it has happened, create a new ByteBuffer and read it.

            data = new EmotePacket.Builder().build().read(ByteBuffer.wrap(bytes));
            shouldRemainFalse = true; //That line should not bu used
        }catch (Exception ignored){

        }
        Assertions.assertFalse(shouldRemainFalse, "Writer didn't thrown exception");

    }
}
