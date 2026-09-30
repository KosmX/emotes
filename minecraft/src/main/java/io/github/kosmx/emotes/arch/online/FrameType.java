package io.github.kosmx.emotes.arch.online;

/** What a frame to the Online Emotes relay holds, written right after the protocol version. */
enum FrameType {
    /** Game profile, nullable world UUID, nullable server address (for protocol 1 clients), every tracked player. */
    STATE(0),
    /** The players tracked since the last frame, then the ones no longer tracked. */
    TRACK(1),
    /** An emote packet, from whoever the last STATE was about. */
    EMOTE(2);

    final byte id;

    FrameType(int id) {
        this.id = (byte) id;
    }
}
