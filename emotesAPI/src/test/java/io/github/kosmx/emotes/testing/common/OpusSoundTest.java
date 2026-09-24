package io.github.kosmx.emotes.testing.common;

import io.github.kosmx.emotes.common.opus.OpusSound;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;

class OpusSoundTest {
    @Test
    void cancelledRequestDoesNotPoisonReplay() throws Exception {
        OpusSound sound = silence();
        assertInstanceOf(CancellationException.class, failure(sound.decoded(() -> true)));
        assertFalse(sound.failed());
        assertEquals(960, sound.decoded().get(5, SECONDS).samples().length);
    }

    @Test
    void stopsBetweenPacketsWithoutMarkingTheTrackCorrupt() throws Exception {
        // First packet is valid; the second passes TOC validation but cannot be decoded.
        OpusSound sound = new OpusSound(0, 0, 0, null, null,
                new byte[]{(byte) 0xf8, (byte) 0xff, (byte) 0xfe, (byte) 0xfb, 0x41}, new int[]{0, 3, 5});
        AtomicInteger checks = new AtomicInteger();
        // Request, allocation, first packet, then cancellation before the corrupt second packet.
        assertInstanceOf(CancellationException.class, failure(sound.decoded(() -> checks.incrementAndGet() >= 4)));
        assertEquals(4, checks.get());
        assertFalse(sound.failed());

        assertFalse(failure(sound.decoded()) instanceof CancellationException);
        assertTrue(sound.failed());
    }

    @Test
    void onePlayerStoppingDoesNotCancelAnotherPlayersDecode() throws Exception {
        ThreadPoolExecutor decoder = decoder();
        CountDownLatch release = blockDecoder(decoder);
        CompletableFuture<OpusSound.DecodedSound> first;
        CompletableFuture<OpusSound.DecodedSound> second;
        AtomicBoolean stopped = new AtomicBoolean();
        OpusSound sound = silence();
        try {
            first = sound.decoded(stopped::get);
            second = sound.decoded(() -> false);
            assertSame(first, second);
            stopped.set(true);
        } finally {
            release.countDown();
        }
        assertSame(first.get(5, SECONDS), second.get(5, SECONDS));
        assertFalse(sound.failed());
    }

    @Test
    void queueIsBoundedAndOverloadCanBeRetried() throws Exception {
        ThreadPoolExecutor decoder = decoder();
        assertTrue(decoder.getMaximumPoolSize() <= 2);
        CountDownLatch release = blockDecoder(decoder);
        List<CompletableFuture<OpusSound.DecodedSound>> queued = new ArrayList<>();
        AtomicBoolean stopped = new AtomicBoolean();
        OpusSound overflow = silence();
        OpusSound cancelled = new OpusSound(0, 0, 0, null, null,
                new byte[]{(byte) 0xfb, 0x41}, new int[]{0, 2});
        try {
            int capacity = decoder.getQueue().remainingCapacity();
            assertTrue(capacity > 0 && capacity <= 16);
            queued.add(cancelled.decoded(stopped::get));
            for (int i = 1; i < capacity; i++) queued.add(silence().decoded(stopped::get));
            assertEquals(capacity, decoder.getQueue().size());
            assertInstanceOf(RejectedExecutionException.class, failure(overflow.decoded()));
            assertFalse(overflow.failed());
            stopped.set(true);
        } finally {
            release.countDown();
        }
        for (var future : queued) assertInstanceOf(CancellationException.class, failure(future));
        // Even the malformed queued track was skipped, rather than decoded after its emote stopped.
        assertFalse(cancelled.failed());
        assertEquals(960, overflow.decoded().get(5, SECONDS).samples().length);
    }

    private static Throwable failure(CompletableFuture<?> future) throws Exception {
        Throwable error = future.handle((value, failure) -> failure).get(5, SECONDS);
        assertNotNull(error);
        return error instanceof CompletionException ? error.getCause() : error;
    }

    private static OpusSound silence() throws Exception {
        return new OpusSound(0, 0, 0, null, null,
                new byte[]{(byte) 0xf8, (byte) 0xff, (byte) 0xfe}, new int[]{0, 3});
    }

    private static ThreadPoolExecutor decoder() throws Exception {
        var field = OpusSound.class.getDeclaredField("DECODER");
        field.setAccessible(true);
        return (ThreadPoolExecutor) field.get(null);
    }

    private static CountDownLatch blockDecoder(ThreadPoolExecutor decoder) throws Exception {
        CountDownLatch started = new CountDownLatch(decoder.getMaximumPoolSize());
        CountDownLatch release = new CountDownLatch(1);
        for (int i = 0; i < decoder.getMaximumPoolSize(); i++) {
            decoder.execute(() -> {
                started.countDown();
                try {
                    if (!release.await(10, SECONDS)) throw new AssertionError("Decoder test timed out");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
            });
        }
        if (!started.await(5, SECONDS)) {
            release.countDown();
            fail("Decoder workers did not start");
        }
        return release;
    }
}
