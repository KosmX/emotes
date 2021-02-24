package com.kosmx.emotes.fabric.network;

import com.kosmx.emotes.executor.INetworkInstance;

import java.util.HashMap;

//TODO
public class ClientNetworkManager implements INetworkInstance {
    @Override
    public HashMap<Byte, Byte> getVersions() {
        return new HashMap<>();
    }

    @Override
    public boolean sendPlayerData() {
        return true;
    }

    @Override
    public void sendByteArray(byte[] bytes) {
        //pass
    }
}
