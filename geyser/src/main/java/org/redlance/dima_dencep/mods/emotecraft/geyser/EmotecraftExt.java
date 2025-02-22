package org.redlance.dima_dencep.mods.emotecraft.geyser;

import io.github.kosmx.emotes.api.services.LoggerService;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import org.cloudburstmc.protocol.bedrock.packet.EmoteListPacket;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.bedrock.ClientEmoteEvent;
import org.geysermc.geyser.api.event.bedrock.SessionDisconnectEvent;
import org.geysermc.geyser.api.event.bedrock.SessionInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.util.MinecraftKey;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;
import org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.GayserHacks;
import org.redlance.dima_dencep.mods.emotecraft.geyser.handler.GeyserNetworkInstance;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.DinnerboneProtocolUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class EmotecraftExt implements Extension {
    private static final Map<GeyserSession, GeyserNetworkInstance> INSTANCES = new ConcurrentHashMap<>();

    public static final Key MIECRAFT_REGISTER_TYPE = MinecraftKey.key("register");
    public static final Key EMOTECAFT_EMOTE_TYPE = Key.key(CommonData.MOD_ID, CommonData.playEmoteID);

    private static EmotecraftExt instance;

    public EmotecraftExt() {
        EmotecraftExt.instance = this;
    }

    @Subscribe
    public void onPreInitialize(GeyserPreInitializeEvent event) {
        LoggerService.INSTANCE.log(Level.INFO, "Loading emotecraft on geyser...");

        GayserHacks.addCustomJavaTranslator(ClientboundCustomPayloadPacket.class, (session, packet) -> {
            Key type = packet.getChannel();
            if (CommonData.MOD_ID.equals(type.namespace())) { // Any emotecraft payload
                onEmotecraftPayload(session, packet.getChannel(), packet.getData());
                return false; // Discard

            } else if (MIECRAFT_REGISTER_TYPE.equals(type)) {
                onMinecraftRegisterPayload(session, packet.getChannel(), packet.getData());
            }
            return true; // Pass
        });
        GayserHacks.addCustomBedrockTranslator(EmoteListPacket.class, (session, packet) -> {
            LoggerService.INSTANCE.log(Level.INFO, "Player emotes " + packet.getPieceIds());
            return true;
        });
    }

    private void onMinecraftRegisterPayload(GeyserSession session, Key type, byte[] bytes) {
        Set<Key> channels = DinnerboneProtocolUtils.readChannels(Unpooled.wrappedBuffer(bytes));
        LoggerService.INSTANCE.log(Level.INFO, "Server listening channels: " + channels);
        if (channels.contains(EmotecraftExt.EMOTECAFT_EMOTE_TYPE)) {
            LoggerService.INSTANCE.log(Level.INFO, "Has emotecraft!");

            ByteBuf byteBuf = Unpooled.buffer();
            DinnerboneProtocolUtils.writeChannels(byteBuf, Collections.singleton(EmotecraftExt.EMOTECAFT_EMOTE_TYPE));
            session.sendDownstreamPacket(new ServerboundCustomPayloadPacket(type, byteBuf.array()));
        } else {
            // Online-emotes integration?
        }
    }

    private void onEmotecraftPayload(GeyserSession session, Key channel, byte[] bytes) throws IOException {
        NetData data = new EmotePacket.Builder().build().read(ByteBuffer.wrap(bytes));
        if (data == null) {
            throw new IOException("no valid data");
        }

        GeyserNetworkInstance networkInstance = EmotecraftExt.INSTANCES.computeIfAbsent(session, GeyserNetworkInstance::new);
        if (!networkInstance.trustReceivedPlayer()) {
            data.player = null;
        }
        if (data.player == null && data.purpose.playerBound) {
            throw new IOException("Didn't received any player information");
        }

        LoggerService.INSTANCE.log(Level.FINE, "[emotes client] Received message: " + data);

        if (data.purpose == null) {
            LoggerService.INSTANCE.log(Level.INFO, "Packet execution is not possible without a purpose");
            return;
        }

        networkInstance.sendMessage(new EmotePacket.Builder(data.copy())
                .setVersion(networkInstance.getRemoteVersions()), null
        );
    }

    @Subscribe
    public void onSessionInitialize(SessionInitializeEvent event) {
        GeyserSession session = (GeyserSession) event.connection();
        EmotecraftExt.INSTANCES.put(session, new GeyserNetworkInstance(session));
    }

    @Subscribe
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        EmotecraftExt.INSTANCES.remove((GeyserSession) event.connection());
    }

    @Subscribe
    public void onEmote(ClientEmoteEvent event) {
        LoggerService.INSTANCE.log(Level.INFO, "On emote " + event.emoteId());
        GeyserSession session = (GeyserSession) event.connection();
        // TODO translate
    }

    public static EmotecraftExt getInstance() {
        return EmotecraftExt.instance;
    }
}
