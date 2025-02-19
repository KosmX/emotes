package io.github.kosmx.emotes.arch.mixin;

import io.github.kosmx.emotes.arch.network.EmotesMixinConnection;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

@Mixin(Connection.class)
public class ConnectionHandlerMixin implements EmotesMixinConnection {
    @Unique
    @NotNull
    private final HashMap<Byte, Byte> emotecraft$versions = new HashMap<>();

    @Override
    public @NotNull HashMap<Byte, Byte> emotecraft$getRemoteVersions() {
        return emotecraft$versions;
    }

    @Override
    public void emotecraft$setVersions(@Nullable HashMap<Byte, Byte> map) {
        emotecraft$versions.clear();
        if (map != null) {
            emotecraft$versions.putAll(map);
        }
    }
}
