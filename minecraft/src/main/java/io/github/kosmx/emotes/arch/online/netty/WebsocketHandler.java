package io.github.kosmx.emotes.arch.online.netty;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.kosmx.emotes.arch.online.OnlineEmotes;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.common.network.PacketBound;
import io.github.kosmx.emotes.main.network.BaseClientNetwork;
import io.github.kosmx.emotes.mc.McUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import net.minecraft.client.Minecraft;

/** Takes what the relay sends: emote packets as binary frames, messages for the player as JSON text frames. */
public class WebsocketHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    private final BaseClientNetwork network;

    public WebsocketHandler(BaseClientNetwork network) {
        this.network = network;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent idle)) {
            super.userEventTriggered(ctx, evt);
        } else if (idle.state() == IdleState.READER_IDLE) {
            ctx.close(); // not even a pong: the socket is half-open
        } else {
            ctx.writeAndFlush(new PingWebSocketFrame(), ctx.voidPromise());
        }
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
                try { // a component, or an object holding one as its message
                    JsonElement element = JsonParser.parseString(frame.text());
                    JsonElement message = element.isJsonObject() ? element.getAsJsonObject().get("message") : element;
                    if (message != null) OnlineEmotes.toast(McUtils.fromJson(message));
                } catch (Exception e) {
                    CommonData.LOGGER.error("Failed to parse Online Emotes text frame: {}", frame.text(), e);
                }
            }

            case PingWebSocketFrame frame -> {
                frame.content().retain();
                ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content()), ctx.channel().voidPromise());
            }

            case PongWebSocketFrame ignored -> {} // the answer to our ping

            case CloseWebSocketFrame frame -> {
                if (frame.statusCode() == WebSocketCloseStatus.POLICY_VIOLATION.code()) this.network.disconnect(); // until the next join
                ctx.channel().close();
            }

            default -> CommonData.LOGGER.error("Unsupported Online Emotes frame type: {}!", msg.getClass().getName());
        }
    }
}
