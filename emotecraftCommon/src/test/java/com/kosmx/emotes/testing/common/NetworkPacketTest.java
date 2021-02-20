package com.kosmx.emotes.testing.common;

import com.kosmx.emotes.common.emote.EmoteData;
import com.kosmx.emotes.common.network.EmotePacket;
import com.kosmx.emotes.common.network.objects.NetData;
import com.kosmx.emotes.common.tools.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Test network data sending and receiving
 */
public class NetworkPacketTest {
    @Test
    @DisplayName("Network protocol test")
    public void netTest() throws IOException {
        EmotePacket.Builder builder = new EmotePacket.Builder();
        Pair<EmoteData.EmoteBuilder, EmoteData.EmoteBuilder> pair = RandomEmoteData.generateEmotes();
        builder.configureToSendEmote(pair.getLeft().build());
        ByteBuffer byteBuffer = builder.build().write();
        byte[] bytes = byteBuffer.array();

        //The array has been sent, hope, it will arrive correctly.
        //Assume it has happened, create a new ByteBuffer and read it.

        NetData data = new EmotePacket.Builder().build().read(ByteBuffer.wrap(bytes)); //That read expression is kinda funny
        Assertions.assertNotNull(data, "Data should be not null");
        Assertions.assertEquals(pair.getLeft().build(), data.emoteData, "The received data should contain the same emote");
        Assertions.assertEquals(pair.getLeft().build().hashCode(), data.emoteData.hashCode(), "The received data should contain the same emote");

    }
}
