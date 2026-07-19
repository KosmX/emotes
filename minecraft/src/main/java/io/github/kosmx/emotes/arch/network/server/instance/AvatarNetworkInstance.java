package io.github.kosmx.emotes.arch.network.server.instance;

import io.github.kosmx.emotes.common.network.EmotePacket;
import io.github.kosmx.emotes.server.network.instance.ConfigNetworkInstance;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper class for avatars
 */
public final class AvatarNetworkInstance extends McServerNetworkInstance {
    @NotNull
    private final Avatar avatar;

    public AvatarNetworkInstance(@NotNull Avatar avatar) {
        super(ConfigNetworkInstance.IMMUTABLE);
        if (avatar instanceof Player) throw new UnsupportedOperationException("For players, use ModdedServerPlayNetwork!");
        this.avatar = avatar;
    }

    @Override
    public @NotNull Avatar getAvatar() {
        return this.avatar;
    }

    @Override
    public void disconnect() {
        // no-op
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void sendPlayMessage(EmotePacket bytes) {
        throw new UnsupportedOperationException("Only players can have a connection!");
    }
}
