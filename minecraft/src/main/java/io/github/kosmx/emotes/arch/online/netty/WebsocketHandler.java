package io.github.kosmx.emotes.arch.online.netty;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.online.OnlineEmotes;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketBound;
import io.github.kosmx.emotes.main.network.BaseClientNetwork;
import io.github.kosmx.emotes.mc.McUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/** Takes what the relay sends: emote packets as binary frames, messages for the player as JSON text frames. */
public class WebsocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private static final Component DISCONNECTED = Component.translatable("emotecraft.online.disconnected");
    private static final Component CONNECTED = Component.translatable("emotecraft.online.connected");

    private final BaseClientNetwork network;

    public WebsocketHandler(BaseClientNetwork network) {
        this.network = network;
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        if (PlatformTools.getConfig().onlineDebug.get()) OnlineEmotes.toast(WebsocketHandler.DISCONNECTED);
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        if (PlatformTools.getConfig().onlineDebug.get()) OnlineEmotes.toast(WebsocketHandler.CONNECTED);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {
        switch (msg) {
            case BinaryWebSocketFrame frame -> {
                EmotePacket packet;
                try {
                    // Read here, as the frame is released once this returns; played on the client thread
                    packet = new EmotePacket(frame.content(), PacketBound.CLIENT, true);
                } catch (Exception e) {
                    CommonData.LOGGER.warn("Dropping undecodable Online Emotes packet", e);
                    return;
                }
                Minecraft.getInstance().execute(() -> this.network.receiveMessage(packet));
            }

            case TextWebSocketFrame frame -> {
                try {
                    JsonElement element = JsonParser.parseString(frame.text());
                    if (!element.isJsonObject()) {
                        OnlineEmotes.toast(McUtils.fromJson(element));
                        break;
                    }

                    JsonObject object = element.getAsJsonObject();
                    if (object.has("message")) {
                        OnlineEmotes.toast(McUtils.fromJson(object.get("message")));
                    }
                } catch (Exception e) {
                    CommonData.LOGGER.error("Failed to parse Online Emotes text frame: {}", frame.text(), e);
                }
            }

            case PingWebSocketFrame frame -> {
                frame.content().retain();
                ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content()), ctx.channel().voidPromise());
            }

            case CloseWebSocketFrame ignored -> ctx.channel().close();

            default -> CommonData.LOGGER.error("Unsupported Online Emotes frame type: {}!", msg.getClass().getName());
        }
    }
}
