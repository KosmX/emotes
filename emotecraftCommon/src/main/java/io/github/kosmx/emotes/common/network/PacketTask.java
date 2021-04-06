package io.github.kosmx.emotes.common.network;


public enum PacketTask {
    UNKNOWN(0, false),
    STREAM(1, true),
    CONFIG(8, false),
    STOP(10, true);

    public final byte id;

    /**
     * True if task is player emote play related
     */
    public final boolean isEmoteStream;

    PacketTask(byte id, boolean isEmoteStream) {
        this.id = id;
        this.isEmoteStream = isEmoteStream;
    }
    public static PacketTask getTaskFromID(byte b){
        for(PacketTask task:PacketTask.values()){
            if(task.id == b)return task;
        }
        return UNKNOWN;
    }

    PacketTask(int i, boolean isEmoteStream) {
        this((byte) i, isEmoteStream);
    }
}
