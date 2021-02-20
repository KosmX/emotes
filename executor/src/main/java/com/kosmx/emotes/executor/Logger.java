package com.kosmx.emotes.executor;

import java.util.logging.Level;

public interface Logger {
     default void log(Level level, String msg){
         log(level, msg, false);
     }

     default void log(Level level, String msg, boolean bl){
         if(bl && EmoteInstance.config != null && EmoteInstance.config.showDebug){
            writeLog(level, msg);
         }
     }

     void writeLog(Level level, String msg);

}
