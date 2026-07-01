package io.github.kosmx.emotes.common.network;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum PacketBound {
    SERVER,
    CLIENT;

    public static final Set<PacketBound> BOTH = Collections.unmodifiableSet(EnumSet.allOf(PacketBound.class));
    public static final Set<PacketBound> TO_CLIENT = Collections.unmodifiableSet(EnumSet.of(PacketBound.CLIENT));
    public static final Set<PacketBound> TO_SERVER = Collections.unmodifiableSet(EnumSet.of(PacketBound.SERVER));
}
