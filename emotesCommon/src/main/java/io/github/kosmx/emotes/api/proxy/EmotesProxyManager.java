package io.github.kosmx.emotes.api.proxy;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

/**
 * some static methods to register received message and register proxy module
 */
public abstract class EmotesProxyManager {
    private static EmotesProxyManager manager = null;
    /**
     * The list of registered instances.
     * To register yours use {@link #registerProxyInstance(INetworkInstance)}
     */
    protected final static ArrayList<INetworkInstance> networkInstances = new ArrayList<>();

    /**
     * Log a message through EmoteX logger.
     * If possible use your own logger instead of this
     *
     * @param level Log level
     * @param msg Log message
     */
    public static void log(Level level, String msg){
        getManager().logMSG(level, msg);
    }

    /**
     * Register your proxy instance
     * use {@link AbstractNetworkInstance} to create a new instance
     * @param instance your instance
     * @return true if registered {@link ArrayList#add(Object)}
     */
    public static boolean registerProxyInstance(INetworkInstance instance){
        if(!networkInstances.contains(instance)){
            return networkInstances.add(instance);
        }
        return false;
    }

    /**
     * Unregister your proxy instance
     * Why were you doing that, you can use {@link INetworkInstance#isActive()} to temporally disable it
     * @param instance instance to unregister
     * @return if it was unregistered {@link ArrayList#remove(Object)}
     */
    public static boolean unregisterProxyInstance(INetworkInstance instance){
        return networkInstances.remove(instance);
    }


    /**
     * Network instance has received a message, it will send it to EmoteX to execute
     * You can invoke it via {@link INetworkInstance#receiveMessage(ByteBuffer, UUID)}
     *
     * @param buffer received buffer
     * @param player player who plays the emote, Can be NULL but only
     * @param networkInstance the network instance to send back information or to store other version
     */
    static void receiveMessage(ByteBuffer buffer, UUID player, INetworkInstance networkInstance){
        getManager().dispatchReceive(buffer, player, networkInstance);
    }

    /**
     * Use this when a network connection disconnects.
     * It's responsible to remove server-side emotes
     * @param networkInstance disconnected network instance
     */
    static void disconnectInstance(INetworkInstance networkInstance){
        getManager().onDisconnectFromServer(networkInstance);
    }

    //---------------- These are EmoteX's own stuff. you shouldn't touch these ----------------

    /**
     * Make sure no-one can use this before EmoteX init
     * @return manager
     */
    protected static EmotesProxyManager getManager(){
        if(manager == null){
            throw new IllegalStateException("Emotecraft proxy is NOT loaded. You can only register instances before loading.");
        }
        return manager;
    }

    /**
     * You can set manager only once. DO NOT DO IT
     * @param newManager set it
     */
    protected static void setManager(EmotesProxyManager newManager){
        if(manager != null){
            throw new IllegalArgumentException("You can't set manager twice");
        }
        manager = newManager;
    }

    protected abstract void logMSG(Level level, String msg);

    protected abstract void dispatchReceive(ByteBuffer buffer, UUID player, INetworkInstance networkInstance);

    public abstract void onDisconnectFromServer(INetworkInstance networkInstance);
}
