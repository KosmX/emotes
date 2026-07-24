package io.github.kosmx.emotes.hytale.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.kosmx.emotes.hytale.EmotePlayback;
import io.github.kosmx.emotes.hytale.HytaleEmoteRegistry;
import io.github.kosmx.emotes.hytale.asset.EmoteDelivery;
import io.github.kosmx.emotes.hytale.library.EmoteLibrary;
import io.github.kosmx.emotes.hytale.ui.EmoteListPage;
import org.jetbrains.annotations.NotNull;

/** Opens the emote library browser. */
public final class EmotecraftCommand extends AbstractPlayerCommand {
    private final EmoteLibrary library;
    private final HytaleEmoteRegistry registry;
    private final EmoteDelivery delivery;
    private final EmotePlayback playback;

    public EmotecraftCommand(EmoteLibrary library, HytaleEmoteRegistry registry,
                             EmoteDelivery delivery, EmotePlayback playback) {
        super("emotecraft", "emotecraft.commands.emotecraft.desc");
        this.library = library;
        this.registry = registry;
        this.delivery = delivery;
        this.playback = playback;
        this.requirePermission("emotecraft.command.emotecraft");
        this.addAliases("emotes");
    }

    @Override
    protected void execute(@NotNull CommandContext context, @NotNull Store<EntityStore> store,
                           @NotNull Ref<EntityStore> ref, @NotNull PlayerRef playerRef, @NotNull World world) {
        // Already on the player's world thread, which is where PageManager expects to be driven from.
        Player player = store.getComponent(ref, Player.getComponentType());
        player.getPageManager().openCustomPage(ref, store,
                new EmoteListPage(playerRef, this.library, this.registry, this.delivery, this.playback));
    }
}
