package io.github.kosmx.emotes.arch.network;

import io.github.kosmx.emotes.common.network.EmotePacket;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Wrapper class for avatars
 */
public final class AvatarServerPlayNetwork extends AbstractServerNetwork {
    @NotNull
    private final Avatar avatar;

    public AvatarServerPlayNetwork(@NotNull Avatar avatar) {
        super();

        if (avatar instanceof Player) throw new UnsupportedOperationException("For players, use ModdedServerPlayNetwork!");
        this.avatar = avatar;
    }

    @Override
    protected @NotNull EmotesMixinConnection getServerConnection() {
        throw new UnsupportedOperationException("Only players can have a connection!");
    }

    @Override
    protected @NotNull Avatar getAvatar() {
        return this.avatar;
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
