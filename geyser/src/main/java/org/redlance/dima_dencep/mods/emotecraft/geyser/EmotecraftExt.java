package org.redlance.dima_dencep.mods.emotecraft.geyser;

import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.SerializableConfig;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.config.ConfigSerializer;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import org.cloudburstmc.protocol.bedrock.packet.EmoteListPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket;
import org.geysermc.event.PostOrder;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.ClientEmoteEvent;
import org.geysermc.geyser.api.event.bedrock.SessionDisconnectEvent;
import org.geysermc.geyser.api.event.bedrock.SessionInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.pack.GeyserResourcePackManifest;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.session.PendingMicrosoftAuthentication;
import org.geysermc.geyser.util.MinecraftKey;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;
import org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.GayserHacks;
import org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.GeyserSessionPatch;
import org.redlance.dima_dencep.mods.emotecraft.geyser.fuckery.ProtocolProvider;
import org.redlance.dima_dencep.mods.emotecraft.geyser.handler.ConnectionType;
import org.redlance.dima_dencep.mods.emotecraft.geyser.handler.GeyserNetworkInstance;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.BedrockEmoteLoader;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.DinnerboneProtocolUtils;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.resourcepack.EmoteResourcePack;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Some cool stuff:
 * - <a href="https://letsgoaway.dev/MarketplaceEmoteList/">MarketplaceEmoteList</a>
 */
@SuppressWarnings("unused")
public class EmotecraftExt implements Extension {
    static {
        try {
            GeyserSessionPatch.patchClass(PendingMicrosoftAuthentication.class, "org/geysermc/geyser/session/GeyserSession.class", GeyserSessionPatch::patch);
        } catch (ReflectiveOperationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<GeyserConnection, GeyserNetworkInstance> INSTANCES = new ConcurrentHashMap<>();

    public static final Key MINECRAFT_REGISTER_TYPE = MinecraftKey.key("register");

    public static final Key EMOTECRAFT_EMOTE_TYPE = Key.key(CommonData.MOD_ID, CommonData.playEmoteID);
    public static final Key EMOTECRAFT_STREAM_TYPE = Key.key(CommonData.MOD_ID, CommonData.emoteStreamID);
    private static final Set<Key> EMOTECRAFT_CHANNELS = Set.of(EMOTECRAFT_EMOTE_TYPE, EMOTECRAFT_STREAM_TYPE);

    private final EmoteResourcePack resourcePack = new EmoteResourcePack(
            new GeyserResourcePackManifest.Version(1, 0, 0), CommonData.MOD_NAME, CommonData.MOD_NAME
    );

    private static EmotecraftExt instance;

    public EmotecraftExt() {
        EmotecraftExt.instance = this;
    }

    @Subscribe
    public void onPreInitialize(GeyserPreInitializeEvent event) {
        eventBus().register(this.resourcePack);
    }

    @Subscribe(postOrder = PostOrder.LAST)
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        CommonData.LOGGER.info("Loading emotecraft on geyser...");
        CommonData.LOGGER.warn("Note that this extension does some horrible hacks on geyser.");
        CommonData.LOGGER.warn("Until custom packet event is added, workarounds cannot be avoided.");

        Serializer.INSTANCE = new Serializer<>(new ConfigSerializer<>(SerializableConfig::new), SerializableConfig.class);
        UniversalEmoteSerializer.loadEmotes();

        GayserHacks.addCustomJavaTranslator(ClientboundCustomPayloadPacket.class, (session, packet) -> {
            Key type = packet.getChannel();
            if (CommonData.MOD_ID.equals(type.namespace())) { // Any emotecraft payload
                onEmotecraftPayload(session, packet.getChannel(), packet.getData());
                return false; // Discard

            } else if (MINECRAFT_REGISTER_TYPE.equals(type)) {
                onMinecraftRegisterPayload(session, packet.getChannel(), packet.getData());
            }
            return true; // Pass
        });
        GayserHacks.addCustomBedrockTranslator(PlayerAuthInputPacket.class, (session, packet) -> {
            GeyserNetworkInstance networkInstance = EmotecraftExt.INSTANCES.get(session);
            if (networkInstance != null && networkInstance.isPlaying() && session.isSneaking()) {
                CommonData.LOGGER.debug("Stopping animation {}", session.name());
                networkInstance.stopEmote(session.getPlayerEntity());
            }
            return true;
        });
        GayserHacks.addCustomBedrockTranslator(EmoteListPacket.class, (session, packet) -> {
            BedrockEmoteLoader.preloadEmotes(packet.getPieceIds()); // Preload emotes
            return true;
        });
    }

    private void onMinecraftRegisterPayload(GeyserSession session, Key type, byte[] bytes) {
        ByteBuf inputByteBuf = Unpooled.wrappedBuffer(bytes);
        Set<Key> channels = DinnerboneProtocolUtils.readChannels(inputByteBuf);
        inputByteBuf.release();

        CommonData.LOGGER.debug("Server listening channels: {}", channels);
        if (channels.contains(EmotecraftExt.EMOTECRAFT_EMOTE_TYPE)) {
            CommonData.LOGGER.debug("Has emotecraft!");

            ByteBuf byteBuf = Unpooled.buffer();
            DinnerboneProtocolUtils.writeChannels(byteBuf, EMOTECRAFT_CHANNELS);
            session.sendDownstreamPacket(new ServerboundCustomPayloadPacket(type, byteBuf.array()));
            byteBuf.release();

            if (((ProtocolProvider) session).ec$state() == ProtocolState.GAME) {
                GeyserNetworkInstance networkInstance = EmotecraftExt.INSTANCES.get(session);

                if (networkInstance.getConnectionType() == ConnectionType.NONE) {
                    CommonData.LOGGER.warn("The server failed to configure the client, attempting to configure...");
                    networkInstance.sendC2SConfig();
                }
            }
        } /*else {
            // Online-emotes integration?
        }*/
    }

    private void onEmotecraftPayload(GeyserConnection session, Key channel, byte[] bytes) {
        GeyserNetworkInstance networkInstance = EmotecraftExt.INSTANCES.computeIfAbsent(session, GeyserNetworkInstance::new);
        if (networkInstance.getConnectionType() == ConnectionType.NONE) {
            if (((ProtocolProvider) session).ec$state() == ProtocolState.CONFIGURATION) {
                CommonData.LOGGER.debug("Configuring emotecraft...");
                networkInstance.sendC2SConfig();
            }
            networkInstance.setConnectionType(ConnectionType.BACKEND);
        }
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        networkInstance.receiveMessage(new EmotePacket(byteBuf));
        byteBuf.release();
    }

    @Subscribe
    public void onSessionInitialize(SessionInitializeEvent event) {
        GeyserConnection session = event.connection();
        EmotecraftExt.INSTANCES.put(session, new GeyserNetworkInstance(session));
    }

    @Subscribe
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        GeyserNetworkInstance instance = EmotecraftExt.INSTANCES.remove(event.connection());
        if (instance != null) instance.disconnect();
    }

    @Subscribe
    public void onDefineCommands(GeyserDefineCommandsEvent event) {
        event.register(Command.builder(this)
                .name(rootCommand())
                .bedrockOnly(true)
                .source(GeyserConnection.class)
                .aliases(List.of("emotes", "form"))
                .description("Emotecraft command")
                .playerOnly(true)
                .executor((source, cmd, args) ->
                        EmotecraftExt.INSTANCES.get((GeyserConnection) source).showForm()
                )
                .build()
        );
    }

    @Subscribe(postOrder = PostOrder.FIRST, ignoreCancelled = true)
    public void onEmote(ClientEmoteEvent event) {
        GeyserNetworkInstance networkInstance = EmotecraftExt.INSTANCES.get(event.connection());
        if (networkInstance != null && networkInstance.getConnectionType() != ConnectionType.NONE) {
            CompletableFuture<Animation> animation = BedrockEmoteLoader.loadEmote(event.emoteId());

            if (animation.isDone() && !animation.isCompletedExceptionally()) {
                networkInstance.playEmote(animation.join(), false);

            } else {
                networkInstance.stopEmote();

                if (animation.isCompletedExceptionally()) {
                    networkInstance.sendChatMessage("emotecraft.cantconvert");

                } else {
                    animation.thenAccept(emote -> networkInstance.playEmote(
                            emote, true
                    ));
                }
            }
            event.setCancelled(true);
        }
    }

    public EmoteResourcePack getResourcePack() {
        return this.resourcePack;
    }

    public static EmotecraftExt getInstance() {
        return EmotecraftExt.instance;
    }
}
