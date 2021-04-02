package io.github.kosmx.emotes.common.network;


public enum PacketTask {
    UNKNOWN(0), STREAM(1), CONFIG(8), STOP(10)
    ;

    public final byte id;

    PacketTask(byte id) {
        this.id = id;
    }
    public static PacketTask getTaskFromID(byte b){
        for(PacketTask task:PacketTask.values()){
            if(task.id == b)return task;
        }
        return UNKNOWN;
    }

    PacketTask(int i) {
        this((byte) i);
    }
}
