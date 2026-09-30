package io.github.kosmx.emotes.arch.online;

import io.github.kosmx.emotes.PlatformTools;
import io.github.kosmx.emotes.arch.gui.toast.EmotecraftToast;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.mc.McUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Online Emotes: streams emotes through a relay on servers without Emotecraft. Decides when {@link OnlineNetworkInstance} connects. */
public final class OnlineEmotes {
    private static final Component TITLE = Component.translatable("emotecraft.online");
    private static final Identifier ICON = McUtils.newIdentifier("textures/online_emotes.png");
    private static final long DISPLAY_TIME = 1500L;

    private OnlineEmotes() {}

    /** Connects on joining a world someone else can join too, or tells an open connection about the new one. */
    public static void connect() {
        if (isWorldShared()) OnlineNetworkInstance.INSTANCE.connect();
    }

    /** Whether someone else can join the world: a singleplayer one only once it is opened to LAN. */
    public static boolean isWorldShared() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        return server == null || server.isPublished();
    }

    /** Shows a message about the connection, if debugging is on. Any thread. */
    public static void debugToast(Component message) {
        if (PlatformTools.getConfig().onlineDebug.get()) toast(message);
    }

    /** Shows a message from or about the relay. Any thread. */
    public static void toast(Component message) {
        CommonData.LOGGER.info("[Online Emotes] {}", message.getString());

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            ToastManager manager = minecraft.gui.toastManager();
            manager.addToast(new EmotecraftToast(manager, ICON, DISPLAY_TIME, TITLE, message));
        });
    }
}
