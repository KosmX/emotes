package com.kosmx.emotecraftCommon;

import com.kosmx.emoteBukkit.BukkitMain;
import net.fabricmc.loader.api.FabricLoader;
import org.bukkit.Bukkit;

/**
 * This should be called ONLY if both Fabric and Bukkit is present!
 */
public class Proxy {
    public static boolean isLoadedAsFabricMod(){
        return FabricLoader.getInstance().isModLoaded("emotecraft");
    }

    public static void disableItself(){
        Bukkit.getPluginManager().disablePlugin(BukkitMain.getPlugin(BukkitMain.class));
    }
}
