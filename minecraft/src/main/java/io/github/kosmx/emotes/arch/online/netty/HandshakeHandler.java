package io.github.kosmx.emotes.arch.online.netty;

import io.github.kosmx.emotes.common.CommonData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Upgrades one connection to a WebSocket; {@link #handshakeFuture()} tells how that went. */
public class HandshakeHandler extends SimpleChannelInboundHandler<FullHttpResponse> {
    private static final long TIMEOUT = 10L; // seconds

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;

    public HandshakeHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    /** Exists once the handler joined the pipeline, which happens as the channel registers. */
    public ChannelPromise handshakeFuture() {
        return this.handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        this.handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.handshaker.handshake(ctx.channel());

        ctx.executor().schedule(() -> { // as long as Netty's WebSocketClientProtocolHandler waits
            if (this.handshakeFuture.tryFailure(new WebSocketClientHandshakeException("Handshake timed out"))) ctx.close();
        }, TIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) {
        if (!this.handshaker.isHandshakeComplete()) {
            // Throws on a refused upgrade, which exceptionCaught hands to the promise
            this.handshaker.finishHandshake(ctx.channel(), msg);
            this.handshakeFuture.setSuccess();
        }
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        if (!this.handshakeFuture.isDone()) {
            this.handshakeFuture.setFailure(new IOException("Channel closed before handshake completed"));
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!this.handshakeFuture.isDone()) {
            this.handshakeFuture.setFailure(cause);
        } else {
            CommonData.LOGGER.error("Online Emotes WebSocket exception!", cause);
        }
        ctx.close();
    }
}
