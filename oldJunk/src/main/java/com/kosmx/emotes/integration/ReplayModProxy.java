package com.kosmx.emotes.integration;

import com.replaymod.recording.ReplayModRecording;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.Packet;

public class ReplayModProxy {
    public static void registerPacket(Packet packet){
        if(FabricLoader.getInstance().isModLoaded("replaymod")
                && ReplayModRecording.instance != null
                && ReplayModRecording.instance.getConnectionEventHandler() != null
                && ReplayModRecording.instance.getConnectionEventHandler().getPacketListener() != null){
            ReplayModRecording.instance.getConnectionEventHandler().getPacketListener().save(packet);
        }
    }
}
