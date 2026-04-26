package io.github.kosmx.emotes.main.network;

import com.zigythebird.playeranim.util.ClientUtil;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.api.events.client.ClientEmoteAPI;
import io.github.kosmx.emotes.api.events.client.ClientEmoteEvents;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.main.EmoteHolder;
import net.minecraft.client.player.LocalPlayer;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unused") // API
public class ClientEmotePlay extends ClientEmoteAPI {
    public static void clientStartLocalEmote(EmoteHolder emoteHolder) {
        clientStartLocalEmote(emoteHolder.getEmote());
    }

    public static boolean clientStartLocalEmote(Animation emote) {
        return clientStartLocalEmote(emote, 0);
    }

    public static boolean clientStartLocalEmote(Animation emote, float tick) {
        LocalPlayer player = ClientUtil.getClientPlayer();
        if (player.emotecraft$isForcedEmote()) {
            return false;
        }

        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToStreamEmote(emote, player.getUUID());
        packetBuilder.configureEmoteTick(tick);
        ClientPacketManager.send(packetBuilder, null);
        ClientEmoteEvents.EMOTE_PLAY.invoker().onEmotePlay(emote, tick, player.getUUID());
        player.emotecraft$playEmote(emote, tick, false);
        return true;
    }

    public static void clientRepeatLocalEmote(Animation emote, float tick, UUID target) {
        EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
        packetBuilder.configureToStreamEmote(emote, ClientUtil.getClientPlayer().getUUID()).configureEmoteTick(tick);
        ClientPacketManager.send(packetBuilder, target);
    }

    public static boolean clientStopLocalEmote() {
        if (ClientUtil.getClientPlayer().isPlayingEmote()) {
            return clientStopLocalEmote(ClientUtil.getClientPlayer().emotecraft$getEmote().getCurrentAnimationInstance());
        }
        return false;
    }

    public static boolean isForcedEmote() {
        return ClientUtil.getClientPlayer().emotecraft$isForcedEmote();
    }

    public static boolean clientStopLocalEmote(@Nullable Animation emoteData) {
        if (emoteData != null && !ClientUtil.getClientPlayer().emotecraft$isForcedEmote()) {
            EmotePacket.Builder packetBuilder = new EmotePacket.Builder();
            packetBuilder.configureToSendStop(emoteData.uuid(), ClientUtil.getClientPlayer().getUUID());
            ClientPacketManager.send(packetBuilder, null);
            ClientUtil.getClientPlayer().stopEmote();

            ClientEmoteEvents.LOCAL_EMOTE_STOP.invoker().onEmoteStop();
            return true;
        }
        return false;
    }

    @Override
    protected boolean playEmoteImpl(Animation animation, float tick) {
        if (animation != null) {
            return clientStartLocalEmote(animation, tick);
        } else {
            return clientStopLocalEmote();
        }
    }

    @Override
    protected Collection<Animation> clientEmoteListImpl() {
        return EmoteHolder.list.values().stream().map(EmoteHolder::getEmote).collect(Collectors.toList());
    }
}
