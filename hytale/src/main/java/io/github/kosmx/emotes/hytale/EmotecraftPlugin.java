package io.github.kosmx.emotes.hytale;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.zigythebird.playeranimcore.animation.Animation;
import io.github.kosmx.emotes.common.CommonData;
import io.github.kosmx.emotes.hytale.asset.EmoteCache;
import io.github.kosmx.emotes.hytale.asset.EmoteDelivery;
import io.github.kosmx.emotes.hytale.commands.EmotecraftCommand;
import io.github.kosmx.emotes.hytale.entity.EmoteTrackerSystem;
import io.github.kosmx.emotes.hytale.entity.Emoting;
import io.github.kosmx.emotes.hytale.library.EmoteLibrary;
import io.github.kosmx.emotes.hytale.services.HytaleInstanceService;
import io.github.kosmx.emotes.server.config.CommonConfig;
import io.github.kosmx.emotes.server.config.ConfigSerializer;
import io.github.kosmx.emotes.server.config.Serializer;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import io.github.kosmx.emotes.server.services.InstanceService;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class EmotecraftPlugin extends JavaPlugin {
    private final EmoteDelivery delivery = new EmoteDelivery();

    private HytaleEmoteRegistry registry;
    private EmotePlayback playback;
    private EmoteLibrary library;

    public EmotecraftPlugin(@NotNull JavaPluginInit init) {
        super(init);

        // Before the config or the emote cache resolve any path, so both land in the plugin's own directory.
        HytaleInstanceService.setDataDirectory(getDataDirectory());

        // UniversalEmoteSerializer reads the config while running its static initialiser, so the config has to exist
        // before that class is ever touched - otherwise loading the readers NPEs on Serializer.INSTANCE.
        Serializer.INSTANCE = new Serializer<>(
                new ConfigSerializer<>(CommonConfig::new, CommonConfig.staticConfigVersion), CommonConfig.class
        );
    }

    @Override
    protected void setup() {
        UniversalEmoteSerializer.loadEmotes();

        this.registry = new HytaleEmoteRegistry(new EmoteCache(InstanceService.INSTANCE.getCacheDirectory().resolve("emotes")));
        this.playback = new EmotePlayback(this.registry, this.delivery);

        // What is playing on whom, and the system that carries it to players who arrive part-way through.
        Emoting.setComponentType(getEntityStoreRegistry().registerComponent(Emoting.class, Emoting::new));
        getEntityStoreRegistry().registerSystem(new EmoteTrackerSystem(this.delivery));

        // Registered here so the Connect watcher is in place before the first player finishes connecting - the identity
        // token it needs is only ever visible on that packet.
        this.library = new EmoteLibrary(getManifest().getVersion().toString(), getName());
        getCommandRegistry().registerCommand(new EmotecraftCommand(this.library, this.registry, this.delivery, this.playback));

        // The emote wheel lists every published emote for every player, so every player needs every icon. The clips
        // behind them are the expensive half, and those go out only to the people who actually watch an emote.
        // Waiting for ready rather than connect keeps these out of the client's own asset phase; a second firing costs
        // nothing, since a connection is never sent the same blob twice.
        // Global, because the ready event is keyed and we want it whatever the key is.
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, (PlayerReadyEvent event) -> {
            Ref<EntityStore> ref = event.getPlayerRef();
            PlayerRef player = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
            if (player != null) {
                this.delivery.send(player, this.registry.icons());
            }
        });
    }

    @Override
    protected void shutdown() {
        if (this.playback != null) {
            this.playback.close();
        }
        if (this.library != null) {
            this.library.close();
        }
    }

    @Override
    protected void start() {
        // Baking resolves common assets, so it has to wait until the asset stores are up - which start() guarantees.
        // Nobody is online yet either, which is what makes this the right moment to bring the cache back: the blobs go
        // out with the rest of the required assets at login instead of being pushed at whoever happens to be connected.
        int published = this.registry.publishCached();

        for (Animation emote : UniversalEmoteSerializer.getLoadedEmotes().values()) {
            if (this.registry.register(emote) != null) {
                published++;
            }
        }

        getLogger().at(Level.INFO).log("%s published %d emotes, playable with /emote", CommonData.MOD_NAME, published);
    }

    public HytaleEmoteRegistry getRegistry() {
        return this.registry;
    }

    public EmoteLibrary getLibrary() {
        return this.library;
    }
}
