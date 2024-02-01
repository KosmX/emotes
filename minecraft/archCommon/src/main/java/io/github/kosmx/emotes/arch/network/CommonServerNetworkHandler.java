package io.github.kosmx.emotes.arch.network;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import io.github.kosmx.emotes.api.proxy.INetworkInstance;
import io.github.kosmx.emotes.arch.mixin.ServerChunkCacheAccessor;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.GeyserEmotePacket;
import io.github.kosmx.emotes.common.network.objects.NetData;
import io.github.kosmx.emotes.common.tools.BiMap;
import io.github.kosmx.emotes.executor.EmoteInstance;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.geyser.EmoteMappings;
import io.github.kosmx.emotes.server.network.AbstractServerEmotePlay;
import io.github.kosmx.emotes.server.network.IServerNetworkInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CommonServerNetworkHandler extends AbstractServerEmotePlay<ServerPlayer> {
    public static CommonServerNetworkHandler instance = new CommonServerNetworkHandler();

    private static MinecraftServer server = null;

    public static void setServer(@NotNull MinecraftServer server) {
        CommonServerNetworkHandler.server = server;
    }

    @NotNull
    public static MinecraftServer getServer() {
        return server;
    }

    public void init() {

    }

    private byte[] unwrapBuffer(FriendlyByteBuf buf) {
        if(buf.isDirect()){
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            return bytes;
        }
        else {
            return buf.array();
        }
    }

    public void receiveMessage(ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf) {
        try
        {
            receiveMessage(unwrapBuffer(buf), player, (INetworkInstance) handler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveStreamMessage(ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf) {
        try
        {
            if (handler.emotecraft$getServerNetworkInstance().allowEmoteStreamC2S()) {
                var packet = ((AbstractServerNetwork)handler.emotecraft$getServerNetworkInstance()).receiveStreamChunk(ByteBuffer.wrap(unwrapBuffer(buf)));
                if (packet != null) {
                    receiveMessage(packet.array(), player, (INetworkInstance) handler);
                }
            } else {
                handler.disconnect(Component.literal("Emote stream is disabled on this server"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveGeyserMessage(ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf) {
        receiveGeyserMessage(player, unwrapBuffer(buf));
    }

    public void initMappings(Path configPath) throws IOException{
        Path filePath = configPath.resolveSibling("emotecraft_emote_map.json");
        if(filePath.toFile().isFile()){
            BufferedReader reader = Files.newBufferedReader(filePath);
            try {
                this.bedrockEmoteMap = new EmoteMappings(Serializer.serializer.fromJson(reader, new TypeToken<BiMap<UUID, UUID>>() {}.getType()));
            }catch (JsonParseException e){
                e.printStackTrace();
            }
            reader.close();
        }
        else {
            BiMap<UUID, UUID> example = new BiMap<>();
            example.put(new UUID(0x0011223344556677L, 0x8899aabbccddeeffL), new UUID(0xffeeddccbbaa9988L, 0x7766554433221100L));
            BufferedWriter writer = Files.newBufferedWriter(filePath);
            Serializer.serializer.toJson(example, new TypeToken<BiMap<UUID, UUID>>() {}.getType(), writer);
            writer.close();
        }
    }

    @Override
    protected boolean doValidate() {
        return EmoteInstance.config.validateEmote.get();
    }

    @Override
    protected UUID getUUIDFromPlayer(ServerPlayer player) {
        return player.getUUID();
    }

    @Override
    protected ServerPlayer getPlayerFromUUID(UUID player) {
        return server.getPlayerList().getPlayer(player);
    }

    @Override
    protected long getRuntimePlayerID(ServerPlayer player) {
        return player.getId();
    }

    @Override
    protected IServerNetworkInstance getPlayerNetworkInstance(ServerPlayer player) {
        return player.connection.emotecraft$getServerNetworkInstance();
    }

    @Override
    protected void sendForEveryoneElse(GeyserEmotePacket packet, ServerPlayer player) {
        sendForEveryoneElse(null, packet, player); // don't make things complicated
    }

    @Override
    protected void sendForEveryoneElse(@Nullable NetData data, @Nullable GeyserEmotePacket geyserPacket, ServerPlayer player) {
        getTrackedPlayers(player).forEach(target -> {
            if (target != player) {
                try {
                    if (data != null && NetworkPlatformTools.canSendPlay(target, NetworkPlatformTools.EMOTE_CHANNEL_ID)) {
                        IServerNetworkInstance playerNetwork = getPlayerNetworkInstance(target);
                        playerNetwork.sendMessage(new EmotePacket.Builder(data), null);
                    } else if (geyserPacket != null && NetworkPlatformTools.canSendPlay(target, NetworkPlatformTools.GEYSER_CHANNEL_ID)) {
                        IServerNetworkInstance playerNetwork = getPlayerNetworkInstance(target);
                        playerNetwork.sendGeyserPacket(ByteBuffer.wrap(geyserPacket.write()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void sendForPlayerInRange(NetData data, ServerPlayer sourcePlayer, UUID target) {
        try {
            var targetPlayer = sourcePlayer.server.getPlayerList().getPlayer(target);
            if (targetPlayer != null && targetPlayer.getChunkTrackingView().contains(sourcePlayer.chunkPosition())) {
                getPlayerNetworkInstance(targetPlayer).sendMessage(new EmotePacket.Builder(data), null);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void sendForPlayer(NetData data, ServerPlayer ignore, UUID target) {
        try {
            ServerPlayer player = getPlayerFromUUID(target);
            IServerNetworkInstance playerNetwork = getPlayerNetworkInstance(player);

            EmotePacket.Builder packetBuilder = new EmotePacket.Builder(data);
            playerNetwork.sendMessage(packetBuilder, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Collection<ServerPlayer> getTrackedPlayers(Entity entity) {
        var level = entity.level().getChunkSource();
        if (level instanceof ServerChunkCache chunkCache) {
            ServerChunkCacheAccessor storage = (ServerChunkCacheAccessor) chunkCache.chunkMap;

            var tracker = storage.getTrackedEntity().get(entity.getId());
            if (tracker != null) {
                return tracker.getPlayersTracking()
                        .stream().map(ServerPlayerConnection::getPlayer).collect(Collectors.toUnmodifiableSet());
            }
            return Collections.emptyList();
        }
        throw new IllegalArgumentException("server function called on logical client");
    }
}
