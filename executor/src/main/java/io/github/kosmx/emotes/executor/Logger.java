package io.github.kosmx.emotes.executor;

import java.util.logging.Level;

public interface Logger {
     default void log(Level level, String msg){
         log(level, msg, false);
     }

     default void log(Level level, String msg, Throwable throwable){
         log(level, msg, throwable, false);
     }

     default void log(Level level, String msg, boolean bl){
         if(bl || EmoteInstance.config != null && EmoteInstance.config.showDebug.get()){
            writeLog(level, msg);
         }
     }

     default void log(Level level, String msg, Throwable throwable, boolean bl){
         if(bl || EmoteInstance.config != null && EmoteInstance.config.showDebug.get()){
             writeLog(level, msg, throwable);
         }
     }

     void writeLog(Level level, String msg, Throwable throwable);

     void writeLog(Level level, String msg);

}
