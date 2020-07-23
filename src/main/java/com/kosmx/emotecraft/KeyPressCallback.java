package com.kosmx.emotecraft;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.Level;

public interface KeyPressCallback {

    /**
     * ActionResults:
     * SUCCESS - stop listener, we can play the emote..or do whatever we want
     * CONSUME - stop the listener, we don't have to check, but not success
     * PASS - Continue searching, somewhy don't play idk...(not that key was set)
     * FAIL --> ERROR
     */
    Event<KeyPressCallback> EVENT = EventFactory.createArrayBacked(KeyPressCallback.class,
            (listeners) -> (key) -> {
                for(KeyPressCallback listener :listeners){
                    ActionResult result = listener.onKeyPress(key);
                    if(result == ActionResult.SUCCESS || result == ActionResult.CONSUME){
                        return result;
                    }
                    else if(result != ActionResult.PASS){
                        Main.log(Level.WARN, "problem while KeyEVENT!");
                        return result;
                    }
                }
                return ActionResult.PASS;
            }
    );

    ActionResult onKeyPress(InputUtil.Key key);
}
