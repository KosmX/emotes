package io.github.kosmx.emotes.common.network;


public enum PacketTask {
    UNKNOWN(0, false, false, false),
    STREAM(1, true, false, true),
    CONFIG(8, false, false, false),
    STOP(10, true, false, true),
    FILE(0x10, true, true, false);

    public final byte id;

    /**
     * True if task is player emote play related
     */
    public final boolean isEmoteStream;
    /**
     * Exchange name author and desc data
     */
    public final boolean exchangeHeader;

    /**
     * It has to do something with a specific player
     */
    public final boolean playerBound;

    PacketTask(byte id, boolean isEmoteStream, boolean exchangeHeader, boolean playerBound) {
        this.id = id;
        this.isEmoteStream = isEmoteStream;
        this.exchangeHeader = exchangeHeader;
        this.playerBound = playerBound;
    }
    public static PacketTask getTaskFromID(byte b){
        for(PacketTask task:PacketTask.values()){
            if(task.id == b)return task;
        }
        return UNKNOWN;
    }

    PacketTask(int i, boolean isEmoteStream, boolean exchangeHeader, boolean playerBound) {
        this((byte) i, isEmoteStream, exchangeHeader, playerBound);
    }
}
