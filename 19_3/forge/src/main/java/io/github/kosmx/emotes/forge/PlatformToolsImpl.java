package io.github.kosmx.emotes.forge;

public class PlatformToolsImpl {
    public static boolean isPlayerAnimLoaded() {
        try {
            Class.forName("dev.kosmx.playerAnim.api.layered.IAnimation").getName();
            return true;
        } catch(ClassNotFoundException e) {
            return false;
        }
    }
}
