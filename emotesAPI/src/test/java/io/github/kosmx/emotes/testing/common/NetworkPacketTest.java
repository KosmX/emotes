package io.github.kosmx.emotes.testing.common;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import it.unimi.dsi.fastutil.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Test network data sending and receiving
 */
public class NetworkPacketTest {
    @Test
    @DisplayName("Network protocol test")
    public void netTest() throws IOException {
        EmotePacket.Builder builder = new EmotePacket.Builder();
        Pair<Animation, Animation> pair = RandomEmoteData.generateEmotes();
        builder.configureToStreamEmote(pair.left());
        ByteBuffer byteBuffer = builder.build().write();
        byte[] bytes = byteBuffer.array();

        //The array has been sent, hope, it will arrive correctly.
        //Assume it has happened, create a new ByteBuffer and read it.

        NetData data = new EmotePacket.Builder().build().read(ByteBuffer.wrap(bytes)); //That read expression is kinda funny
        Assertions.assertNotNull(data, "Data should be not null");
        Assertions.assertEquals(pair.left().boneAnimations(), data.emoteData.boneAnimations(), "The received data should contain the same emote");
        Assertions.assertEquals(pair.left().boneAnimations().hashCode(), data.emoteData.boneAnimations().hashCode(), "The received data should contain the same emote");


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
            builder.configureToStreamEmote(pair.left());
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
