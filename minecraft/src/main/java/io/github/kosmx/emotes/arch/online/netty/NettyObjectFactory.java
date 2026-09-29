package io.github.kosmx.emotes.arch.online.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueIoHandler;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class NettyObjectFactory {
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private static final ThreadFactory THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName(String.format("Online Emotes Thread #%d", COUNTER.incrementAndGet()));
        thread.setDaemon(true);
        return thread;
    };

    private NettyObjectFactory() {}

    /** One thread: the relay connection keeps its state on it, so none of it needs locking. */
    public static EventLoopGroup newEventLoopGroup() {
        return new MultiThreadIoEventLoopGroup(1, THREAD_FACTORY, ioHandlerFactory());
    }

    private static IoHandlerFactory ioHandlerFactory() {
        if (KQueue.isAvailable()) return KQueueIoHandler.newFactory();
        if (Epoll.isAvailable()) return EpollIoHandler.newFactory();
        return NioIoHandler.newFactory();
    }

    public static Class<? extends SocketChannel> getSocketChannel() {
        if (KQueue.isAvailable()) return KQueueSocketChannel.class;
        if (Epoll.isAvailable()) return EpollSocketChannel.class;
        return NioSocketChannel.class;
    }
}
