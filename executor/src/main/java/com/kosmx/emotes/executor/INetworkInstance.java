package com.kosmx.emotes.executor;

import java.util.HashMap;

/**
 * To hold information about network
 */
public interface INetworkInstance {
    public HashMap<Byte, Byte> getVersions();
    public boolean sendPlayerData();
    public void sendByteArray(byte[] bytes);
}
