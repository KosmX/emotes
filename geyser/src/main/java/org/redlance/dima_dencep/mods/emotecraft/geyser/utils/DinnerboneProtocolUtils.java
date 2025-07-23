/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package org.redlance.dima_dencep.mods.emotecraft.geyser.utils;

import io.github.kosmx.emotes.common.CommonData;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.key.Key;

import java.util.HashSet;
import java.util.Set;

/**
 * Protocol utilities for communicating over Dinnerbone's protocol.
 */
public class DinnerboneProtocolUtils {

    /**
     * Reads a set of channels from the buffer.
     * Each channel is a null-terminated string.
     * If a string is not a valid channel, it is ignored.
     *
     * @param buf the buffer
     * @return the channels
     */
    public static Set<Key> readChannels(ByteBuf buf) {
        final StringBuilder builder = new StringBuilder();
        final Set<Key> channels = new HashSet<>();

        while (buf.isReadable()) {
            final char c = (char) buf.readByte();
            if (c == '\0') {
                parseAndAddChannel(builder, channels);
            } else {
                builder.append(c);
            }
        }

        parseAndAddChannel(builder, channels);

        return channels;
    }

    public static void parseAndAddChannel(StringBuilder builder, Set<Key> channels) {
        if (builder.isEmpty()) {
            return;
        }

        final String channel = builder.toString();
        try {
            channels.add(Key.key(channel));
        } catch (Exception e) {
            CommonData.LOGGER.error("Invalid channel: '{}'!", channel, e);
        } finally {
            builder.setLength(0);
        }
    }

    /**
     * Writes a set of channels to the buffer.
     * Each channel is a null-terminated string.
     *
     * @param buf      the buffer
     * @param channels the channels
     */
    public static void writeChannels(ByteBuf buf, Set<Key> channels) {
        for (Key channel : channels) {
            for (char c : channel.asString().toCharArray()) {
                buf.writeByte(c);
            }
            buf.writeByte('\0');
        }
    }
}
