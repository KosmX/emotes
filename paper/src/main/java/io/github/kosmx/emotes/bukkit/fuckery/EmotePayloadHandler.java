package io.github.kosmx.emotes.bukkit.fuckery;

import io.github.kosmx.emotes.bukkit.BukkitWrapper;
import io.github.kosmx.emotes.bukkit.network.ServerSideEmotePlay;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.mc.McUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.bukkit.entity.Player;

import java.util.List;

@ChannelHandler.Sharable
public class EmotePayloadHandler extends MessageToMessageDecoder<ByteBuf> {
    public static final EmotePayloadHandler INSTANCE = new EmotePayloadHandler();

    public static final Identifier PLAY_PAYLOAD = McUtils.newIdentifier(CommonData.playEmoteID);
    private static final int PAYLOAD_ID = EmotePayloadHandler.hackPayloadId();

    private EmotePayloadHandler() {
        // no-op
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (in.readableBytes() == 0 || !ctx.channel().isActive()) {
            out.add(in.retain());
            return;
        }

        Connection connection = (Connection) ctx.pipeline().get("packet_handler");

        int readerIndex = in.readerIndex();
        FriendlyByteBuf buf = new FriendlyByteBuf(in);

        if (buf.readVarInt() == PAYLOAD_ID) {
            if (PLAY_PAYLOAD.equals(buf.readIdentifier())) {

                int i = buf.readableBytes();
                if (i <= 0 || i > CommonData.MAX_PACKET_SIZE) {
                    throw new IllegalArgumentException("Payload may not be larger than " + CommonData.MAX_PACKET_SIZE + " bytes");
                }

                byte[] data = new byte[i];
                buf.readBytes(data);

                Player player = connection.getPlayer().getBukkitEntity();
                ServerSideEmotePlay.getInstance().registerPlayer(player); // Force register
                ServerSideEmotePlay.getInstance().onPluginMessageReceived(BukkitWrapper.EMOTE_PACKET, player, data);
                return;
            }
        }

        in.readerIndex(readerIndex);
        out.add(in.retain());
    }

    /**
     * Credits to AxiomPaperPlugin
     */
    private static int hackPayloadId() {  // Hack to figure out the id of the CustomPayload packet
        ProtocolInfo<ServerGamePacketListener> protocol = GameProtocols.SERVERBOUND_TEMPLATE.bind(
                k -> new RegistryFriendlyByteBuf(k, MinecraftServer.getServer().registryAccess()), () -> false
        );

        RegistryFriendlyByteBuf friendlyByteBuf = new RegistryFriendlyByteBuf(Unpooled.buffer(), MinecraftServer.getServer().registryAccess());
        try {
            protocol.codec().encode(friendlyByteBuf, new ServerboundCustomPayloadPacket(new DiscardedPayload(PLAY_PAYLOAD, new byte[0])));
            return friendlyByteBuf.readVarInt();
        } finally {
            friendlyByteBuf.release();
        }
    }
}
