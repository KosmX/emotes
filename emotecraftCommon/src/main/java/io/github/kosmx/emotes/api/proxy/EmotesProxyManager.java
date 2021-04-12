package io.github.kosmx.emotes.api.proxy;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.logging.Level;

/**
 * some static methods to register received message and register proxy module
 */
public abstract class EmotesProxyManager {
    private static EmotesProxyManager manager = null;

    /**
     * Log a message through EmoteX logger.
     * If possible use your own logger instead of this
     *
     * @param level Log level
     * @param msg Log message
     */
    public static void log(Level level, String msg){
        if(manager != null)manager.logMSG(level, msg);
    }

    /**
     * Network instance has received a message, it will send it to EmoteX to execute
     * @param buffer received buffer
     * @param player player who plays the emote, Can be NULL but only
     * @param networkInstance the network instance to send back information or to store other version
     */
    static void receiveMessage(ByteBuffer buffer, UUID player, INetworkInstance networkInstance){
        if(manager != null)manager.dispatchReceive(buffer, player, networkInstance);
    }


    //---------------- These are EmoteX's own stuff. you shouldn't touch these ----------------


    protected static void setManager(EmotesProxyManager newManager){
        if(manager != null){
            throw new IllegalArgumentException("You can't set manager twice");
        }
        manager = newManager;
    }

    protected abstract void logMSG(Level level, String msg);

    protected abstract void dispatchReceive(ByteBuffer buffer, UUID player, INetworkInstance networkInstance);
}
