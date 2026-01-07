package org.redlance.dima_dencep.mods.emotecraft.geyser.animator;

import com.google.common.collect.Sets;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.HumanoidAnimationController;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.enums.Axis;
import com.zigythebird.playeranimcore.enums.PlayState;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import com.zigythebird.playeranimcore.util.MatrixUtil;
import io.github.kosmx.emotes.common.CommonData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.geyser.api.entity.property.type.GeyserIntEntityProperty;
import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.entity.type.player.AvatarEntity;
import org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt;
import org.redlance.dima_dencep.mods.emotecraft.geyser.animator.geometry.BendingGeometry;
import org.redlance.dima_dencep.mods.emotecraft.geyser.animator.geometry.GeometryChanger;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.BedrockPacketsUtils;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.GeyserEntityUtils;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.resourcepack.EmoteResourcePack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.redlance.dima_dencep.mods.emotecraft.geyser.animator.PackedProperty.pack;

public class GeyserAnimationController extends HumanoidAnimationController {
    private static final long LEASE_MS = 10;

    private final Map<Identifier, Long> lastUsedProperties = new ConcurrentHashMap<>(1);
    protected final Set<AvatarEntity> listeners = Sets.newConcurrentHashSet();
    private final Set<String> dirtyBones = new HashSet<>();

    protected final UUID avatarId;

    protected GeyserAnimationController(UUID avatarId) {
        super((controller, state, animationSetter) -> PlayState.STOP, MolangLoader::createNewEngine);
        this.avatarId = avatarId;
    }

    @Override
    protected void setupNewAnimation() {
        super.setupNewAnimation();
        for (AvatarEntity avatar : this.listeners) subscribe(avatar);
        this.dirtyBones.clear();
    }

    public void subscribe(AvatarEntity avatarEntity) {
        if (this.listeners.add(avatarEntity) && !EmotecraftExt.getNetworkInstance(avatarEntity.getSession()).appliedGeometries.contains(avatarEntity.getGeyserId())) {
            try {
                GeometryChanger.changeGeometryToBending(avatarEntity).join();
            } catch (Throwable th) {
                CommonData.LOGGER.warn("Failed to apply geometry!", th);
            }
        }
        for (String partKey : this.dirtyBones) {
            updateBone(avatarEntity, partKey, new PlayerAnimBone(partKey));
        }
    }

    protected void handleFrameInternal() {
        try {
            AnimationData data = new AnimationData(0, 1.0F, false);
            tick(data);

            if (!isActive()) return;
            setupAnim(data);

            // Animate via properties
            for (String partKey : this.activeBones.keySet()) {
                if (!this.bones.containsKey(partKey)) {
                    CommonData.LOGGER.debug("Unsupported bone: {}!", partKey);
                    continue;
                }

                PlayerAnimBone bone = get3DTransform(new PlayerAnimBone(partKey));

                for (AvatarEntity avatarEntity : this.listeners) {
                    if (GeyserEntityUtils.unsubscribedFromEntity(avatarEntity)) {
                        unsubscribe(avatarEntity);
                        continue;
                    }

                    if (isActive()) BedrockPacketsUtils.sendInstantAnimation(EmoteResourcePack.ANIMATION_NAME, avatarEntity);
                    updateBone(avatarEntity, partKey, bone);
                }
            }

            if (this.dirtyBones.isEmpty()) this.dirtyBones.addAll(this.activeBones.keySet());
        } catch (Throwable th) {
            CommonData.LOGGER.warn("Failed to animate {}!", this.avatarId, th);
        }
    }

    protected boolean handleFrame() {
        handleFrameInternal();
        this.listeners.removeIf(GeyserEntityUtils::unsubscribedFromEntity);
        return this.listeners.isEmpty();
    }

    @Override
    public PlayerAnimBone get3DTransform(@NonNull PlayerAnimBone bone) {
        bone = super.get3DTransform(bone);

        String boneName = bone.getName();
        if ("left_arm".equals(boneName) || "right_arm".equals(boneName) || "head".equals(boneName)) {
            PlayerAnimBone torsoBone = get3DTransform(new PlayerAnimBone("torso"));
            torsoBone.mulPos(-1);
            torsoBone.mulRot(-1);
            torsoBone.setScaleX(1 / bone.getScaleX());
            torsoBone.setScaleY(1 / bone.getScaleY());
            torsoBone.setScaleZ(1 / bone.getScaleZ());

            MatrixUtil.applyParentsToChild(bone, Collections.singleton(torsoBone), this::getBonePosition);
        } else if ("left_leg".equals(boneName) || "right_leg".equals(boneName)) {
            PlayerAnimBone body = get3DTransform(new PlayerAnimBone("body"));
            MatrixUtil.applyParentsToChild(bone, Collections.singleton(body), this::getBonePosition);

        } else if ("cape".equals(boneName)) {
            bone.rotX *= -1;
        }
        return bone;
    }

    protected void updateBone(AvatarEntity avatarEntity, String partKey, PlayerAnimBone bone) {
        if (!this.bones.containsKey(partKey)) return;
        updateAxis(avatarEntity, partKey, TransformType.POSITION, bone.getPosX(), bone.getPosY(), bone.getPosZ());
        updateAxis(avatarEntity, partKey, TransformType.ROTATION,
                (float) Math.toDegrees(bone.getRotX()), (float) Math.toDegrees(bone.getRotY()), (float) Math.toDegrees(bone.getRotZ())
        );
        updateAxis(avatarEntity, partKey, TransformType.SCALE, bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());

        if (BendingGeometry.BENDABLE_BONES.contains(partKey)) {
            updateBend(avatarEntity, partKey + BendingGeometry.BEND_SUFFIX, bone.bend);
        }
    }

    protected void updateAxis(AvatarEntity avatarEntity, String partKey, TransformType type, float x, float y, float z) {
        Map<Axis, Integer> ids = EmotecraftExt.getInstance().getResourcePack().getAxisIds(partKey, type);
        updateProperty(avatarEntity, pack(ids.get(Axis.X), x));
        updateProperty(avatarEntity, pack(ids.get(Axis.Y), y));
        updateProperty(avatarEntity, pack(ids.get(Axis.Z), z));
    }

    protected void updateBend(AvatarEntity avatarEntity, String partKey, float bend) {
        EmoteResourcePack resourcePack = EmotecraftExt.getInstance().getResourcePack();

        updateProperty(avatarEntity, pack(
                resourcePack.getAxisIds(partKey, TransformType.ROTATION).get(Axis.X),
                (float) Math.toDegrees(bend)
        ));

        float radius = 2.0f;
        float angle = Math.abs(bend);

        updateProperty(avatarEntity, pack(
                resourcePack.getAxisIds(partKey, TransformType.POSITION).get(Axis.Y),
                (float) (radius * (1 - Math.cos(angle)))
        ));
        updateProperty(avatarEntity, pack(
                resourcePack.getAxisIds(partKey, TransformType.POSITION).get(Axis.Z),
                (float) -(radius * Math.sin(angle))
        ));
    }

    @SuppressWarnings("BusyWait")
    private GeyserIntEntityProperty getAvailableProperty() {
        for (;;) {
            long now = System.currentTimeMillis();

            for (GeyserIntEntityProperty property : EmotecraftExt.getInstance().getResourcePack().getRegisteredProperties()) {
                if (this.lastUsedProperties.putIfAbsent(property.identifier(), now) == null) {
                    return property;
                }
            }

            long sleepMs = Long.MAX_VALUE;
            boolean removedAny = false;

            for (var e : this.lastUsedProperties.entrySet()) {
                long remaining = LEASE_MS - (now - e.getValue());

                if (remaining <= 0) {
                    removedAny |= this.lastUsedProperties.remove(e.getKey(), e.getValue());
                } else if (remaining < sleepMs) {
                    sleepMs = remaining;
                }
            }

            if (removedAny) continue;

            if (sleepMs != Long.MAX_VALUE) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void updateProperty(AvatarEntity avatarEntity, int value) {
        avatarEntity.updatePropertiesBatched(updater -> updater.update(getAvailableProperty(), value), true);
    }

    @Override
    public void process(AnimationData state) {
        super.process(state);
        if (!this.animationState.isActive()) internalStop();
    }

    @Override
    public void stop() {
        super.stop();
        internalStop();
    }

    protected void internalStop() {
        for (AvatarEntity avatarEntity : this.listeners) BedrockPacketsUtils.sendBobAnimation(avatarEntity);
    }

    public void unsubscribe(AvatarEntity avatarEntity) {
        if (this.listeners.remove(avatarEntity)) BedrockPacketsUtils.sendBobAnimation(avatarEntity);
    }

    /**
     * A small hack that allows us to get all registered bones.
     */
    public static Collection<String> getRegisteredBones() {
        GeyserAnimationController controller = new GeyserAnimationController(UUID.randomUUID());
        Set<String> bones = new HashSet<>(controller.bones.keySet());
        for (String bendable : BendingGeometry.BENDABLE_BONES) bones.add(bendable + BendingGeometry.BEND_SUFFIX);
        return Collections.unmodifiableCollection(bones);
    }
}
