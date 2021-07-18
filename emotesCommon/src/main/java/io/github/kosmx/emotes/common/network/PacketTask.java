package io.github.kosmx.emotes.common.network;


public enum PacketTask {
    UNKNOWN(0, false, false),
    STREAM(1, true, false),
    CONFIG(8, false, false),
    STOP(10, true, false),
    FILE(0x10, true, true);

    public final byte id;

    /**
     * True if task is player emote play related
     */
    public final boolean isEmoteStream;
    /**
     * Exchange name author and desc data
     */
    public final boolean exchangeHeader;

    PacketTask(byte id, boolean isEmoteStream, boolean exchangeHeader) {
        this.id = id;
        this.isEmoteStream = isEmoteStream;
        this.exchangeHeader = exchangeHeader;
    }
    public static PacketTask getTaskFromID(byte b){
        for(PacketTask task:PacketTask.values()){
            if(task.id == b)return task;
        }
        return UNKNOWN;
    }

    PacketTask(int i, boolean isEmoteStream, boolean exchangeHeader) {
        this((byte) i, isEmoteStream, exchangeHeader);
    }
}
