package io.github.kosmx.emotes.hytale.ui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.kosmx.emotes.hytale.EmotePlayback;
import io.github.kosmx.emotes.hytale.HytaleEmoteRegistry;
import io.github.kosmx.emotes.hytale.asset.EmoteDelivery;
import io.github.kosmx.emotes.hytale.library.EmoteLibrary;
import org.jetbrains.annotations.NotNull;
import org.redlance.emotecraftlibrary.sdk.GameEmoteInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Browses the player's cloud emote library: a page of ten, a search box, and prev/next below.
 * <p>
 * Nothing is downloaded to browse — a page costs one metadata call, and an emote's body is only fetched the first time
 * somebody actually picks it (see {@code HytaleEmoteRegistry#publish}). That matters because the library enforces a
 * download quota, and pushing every liked emote at login would burn it for nothing.
 */
public final class EmoteListPage extends InteractiveCustomUIPage<EmoteListPage.Data> {
    private static final int PER_PAGE = 10;

    private static final String ROOT = "Pages/Emotecraft/EmoteList.ui";
    private static final String ROW = "Pages/Emotecraft/EmoteRow.ui";

    private final EmoteLibrary library;
    private final HytaleEmoteRegistry registry;
    private final EmoteDelivery delivery;
    private final EmotePlayback playback;

    private List<GameEmoteInfo> entries = List.of();
    private String query = "";
    private int page;
    private boolean loading;
    private String status = "";

    public EmoteListPage(@NotNull PlayerRef playerRef, EmoteLibrary library, HytaleEmoteRegistry registry,
                         EmoteDelivery delivery, EmotePlayback playback) {
        super(playerRef, CustomPageLifetime.CanDismiss, Data.CODEC);
        this.library = library;
        this.registry = registry;
        this.delivery = delivery;
        this.playback = playback;
    }

    @Override
    public void build(@NotNull Ref<EntityStore> ref, @NotNull UICommandBuilder cmd,
                      @NotNull UIEventBuilder ev, @NotNull Store<EntityStore> store) {
        cmd.append(ROOT);

        // The query is read off the field at the moment the button fires, so nothing has to be tracked per keystroke -
        // which also sidesteps PageManager dropping Data events while an update is still unacknowledged.
        EventData search = EventData.of(Data.KEY_ACTION, Action.SEARCH.name()).append(Data.KEY_QUERY, "#SearchInput.Value");
        ev.addEventBinding(CustomUIEventBindingType.Activating, "#Search", search);
        ev.addEventBinding(CustomUIEventBindingType.Validating, "#SearchInput", search); // Enter in the field
        ev.addEventBinding(CustomUIEventBindingType.Activating, "#Prev", EventData.of(Data.KEY_ACTION, Action.PREV.name()));
        ev.addEventBinding(CustomUIEventBindingType.Activating, "#Next", EventData.of(Data.KEY_ACTION, Action.NEXT.name()));

        render(cmd, ev);
        if (this.entries.isEmpty() && !this.loading) {
            fetch();
        }
    }

    @Override
    public void handleDataEvent(@NotNull Ref<EntityStore> ref, @NotNull Store<EntityStore> store, @NotNull Data data) {
        if (data.emote != null) {
            play(UUID.fromString(data.emote));
            return;
        }

        if (data.action == null) {
            return;
        }

        switch (Action.valueOf(data.action)) {
            case SEARCH -> {
                this.query = data.query == null ? "" : data.query.trim();
                this.page = 0;
                fetch();
            }
            case PREV -> {
                if (this.page > 0) {
                    this.page--;
                    fetch();
                }
            }
            case NEXT -> {
                this.page++;
                fetch();
            }
        }

        refresh();
    }

    /** Pulls one page of metadata off the library. Blocking SDK work never touches the world thread. */
    private void fetch() {
        this.loading = true;
        this.status = "...";

        int offset = this.page * PER_PAGE;
        var request = this.query.isEmpty()
                ? this.library.listLiked(this.playerRef, offset, PER_PAGE)
                : this.library.execute(this.playerRef, client -> client.search(this.query, offset, PER_PAGE).getData());

        request.whenComplete((result, throwable) -> {
            this.loading = false;
            if (throwable != null) {
                this.status = throwable.getMessage();
            } else {
                this.entries = result;
                this.status = "";
            }
            refresh(); // InteractiveCustomUIPage#sendUpdate hops to the world thread itself
        });
    }

    private void refresh() {
        UICommandBuilder cmd = new UICommandBuilder();
        UIEventBuilder ev = new UIEventBuilder();
        render(cmd, ev);
        sendUpdate(cmd, ev, false); // patch in place - a full rebuild would drop the search box's contents
    }

    private void render(UICommandBuilder cmd, UIEventBuilder ev) {
        cmd.clear("#List");

        List<CommonAsset> icons = new ArrayList<>();
        for (int i = 0; i < this.entries.size(); i++) {
            GameEmoteInfo info = this.entries.get(i);
            String row = "#List[" + i + "]";

            cmd.append("#List", ROW);
            // These three arrive as Minecraft component JSON, not plain text - see McComponents.
            String language = this.playerRef.getLanguage();
            cmd.set(row + " #Name.Text", McComponents.toMessage(info.getName(), language));
            cmd.set(row + " #Author.Text", McComponents.toMessage(info.getAuthor(), language));
            cmd.set(row + " #Description.Text", McComponents.toMessage(info.getDescription(), language));

            cmd.set(row + " #Tags.Text", String.join(" · ", info.getTags()));

            // Only a published emote has an icon registered as a common asset; until then the row shows its fallback.
            HytaleEmoteRegistry.Published published = this.registry.publishedFor(info.getId());
            if (published != null) {
                cmd.set(row + " #Icon.AssetPath", published.icon().getName());
                icons.add(published.icon());
            }

            ev.addEventBinding(CustomUIEventBindingType.Activating, row,
                    EventData.of(Data.KEY_EMOTE, info.getId().toString()));
        }

        // Whoever is reading this page is the one client that needs these icons right now.
        this.delivery.send(this.playerRef, icons);

        cmd.set("#Status.Text", this.status);
        cmd.set("#Prev.Disabled", this.page == 0 || this.loading);
        // The library reports no total, so "next" stays open until a short page proves this was the last one.
        cmd.set("#Next.Disabled", this.entries.size() < PER_PAGE || this.loading);
    }

    /**
     * Publishes the emote if this is its first use, then plays it.
     * <p>
     * Publishing also puts the emote in the client's own emote menu, since that menu is built from the built-in list
     * plus everything in the {@code EmoteAsset} store — so picking an emote here is enough to make it reachable from
     * the wheel afterwards, without any per-player call (there is none).
     */
    private void play(UUID emoteId) {
        this.registry.publish(emoteId, () -> this.library.download(this.playerRef, emoteId))
                .whenComplete((id, throwable) -> {
                    if (throwable != null || id == null) {
                        this.status = throwable == null ? "" : throwable.getMessage();
                        refresh();
                        return;
                    }

                    this.playback.play(this.playerRef, id);

                    this.status = "";
                    refresh(); // the row can now show its icon, which only exists once the emote is published
                });
    }

    private enum Action {
        SEARCH, PREV, NEXT
    }

    public static final class Data {
        static final String KEY_ACTION = "Action";
        static final String KEY_EMOTE = "Emote";
        /** The leading '@' makes the client resolve the value as a selector when the event fires. */
        static final String KEY_QUERY = "@Query";

        static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .addField(new KeyedCodec<>(KEY_ACTION, Codec.STRING), (d, v) -> d.action = v, d -> d.action)
                .addField(new KeyedCodec<>(KEY_EMOTE, Codec.STRING), (d, v) -> d.emote = v, d -> d.emote)
                .addField(new KeyedCodec<>(KEY_QUERY, Codec.STRING), (d, v) -> d.query = v, d -> d.query)
                .build();

        private String action;
        private String emote;
        private String query;
    }
}
