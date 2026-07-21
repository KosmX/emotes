package io.github.kosmx.emotes.hytale.entity;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.SystemGroup;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.kosmx.emotes.hytale.asset.EmoteDelivery;
import org.jetbrains.annotations.NotNull;

/**
 * Keeps an emote in sync with who can see it.
 * <p>
 * The core replays an entity's active animation to anyone who comes into range, but only the animation id — the clip
 * behind it has to be there first, and nothing in the core knows that. So this runs in the same group, against the same
 * newly-visible set, and hands the clip to each arrival. Writing straight to their connection puts it ahead of the
 * core's own update, which is only queued here and flushed a group later.
 * <p>
 * It is also what ends an emote: without the clip's own length nothing would ever clear the active animation, and every
 * newcomer would be shown a performance that finished long ago.
 */
public final class EmoteTrackerSystem extends EntityTickingSystem<EntityStore> {
    private final ComponentType<EntityStore, EntityTrackerSystems.Visible> visibleType = EntityTrackerSystems.Visible.getComponentType();
    private final ComponentType<EntityStore, PlayerRef> playerType = PlayerRef.getComponentType();
    private final ComponentType<EntityStore, Emoting> emotingType = Emoting.getComponentType();

    /**
     * Deliberately not narrowed to entities that are visible to somebody: an emote nobody watched still has to end.
     * The visible component is absent whenever a performer is alone, and querying for it would leave that emote hanging
     * until the next person walked past — who would then be shown it.
     */
    private final Query<EntityStore> query = this.emotingType;

    private final EmoteDelivery delivery;

    public EmoteTrackerSystem(EmoteDelivery delivery) {
        this.delivery = delivery;
    }

    @NotNull
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return EntityTrackerSystems.QUEUE_UPDATE_GROUP;
    }

    @NotNull
    @Override
    public Query<EntityStore> getQuery() {
        return this.query;
    }

    @Override
    public void tick(float dt, int index, @NotNull ArchetypeChunk<EntityStore> archetypeChunk,
                     @NotNull Store<EntityStore> store, @NotNull CommandBuffer<EntityStore> commandBuffer) {
        Emoting emoting = archetypeChunk.getComponent(index, this.emotingType);
        if (emoting == null || emoting.getId() == null) {
            return;
        }

        if (emoting.hasEnded(System.currentTimeMillis())) {
            emoting.stop();

            ActiveAnimationComponent active = archetypeChunk.getComponent(index, ActiveAnimationComponent.getComponentType());
            if (active != null) {
                active.setPlayingAnimation(AnimationSlot.Emote, null);
            }
            return;
        }

        EntityTrackerSystems.Visible visible = archetypeChunk.getComponent(index, this.visibleType);
        if (visible == null || visible.newlyVisibleTo.isEmpty()) {
            return;
        }

        for (Ref<EntityStore> viewerRef : visible.newlyVisibleTo.keySet()) {
            PlayerRef viewer = store.getComponent(viewerRef, this.playerType);
            if (viewer != null) {
                this.delivery.send(viewer.getPacketHandler(), emoting.getBlobs());
            }
        }
    }
}
