package io.github.kosmx.emotes.main;

import io.github.kosmx.emotes.main.network.ClientPacketManager;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;

/**
 * Initializing client and other load stuff...
 *
 */
public class MainClientInit {

    public static void init(){
        loadEmotes();//:D

        ClientPacketManager.init(); //initialize proxy service
    }

    public static void loadEmotes() {
        UniversalEmoteSerializer.loadEmotes();

        EmoteHolder.clearEmotes();

        EmoteHolder.addEmoteToList(UniversalEmoteSerializer.getLoadedEmotes());

    }
}
