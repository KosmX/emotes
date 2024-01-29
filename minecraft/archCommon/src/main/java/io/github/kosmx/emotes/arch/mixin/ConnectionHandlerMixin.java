package io.github.kosmx.emotes.arch.mixin;

import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

@Mixin(Connection.class)
public class ConnectionHandlerMixin implements io.github.kosmx.emotes.arch.network.EmotesMixinConnection {

    @Unique
    @NotNull
    private final HashMap<Byte, Byte> versions = new HashMap<>();

    @Override
    public @NotNull HashMap<Byte, Byte> emotecraft$getRemoteVersions() {
        return versions;
    }

    @Override
    public void emotecraft$setVersions(@Nullable HashMap<Byte, Byte> map) {
        versions.clear();
        if (map != null) {
            versions.putAll(map);
        }
    }
}
