package org.redlance.dima_dencep.mods.emotecraft.geyser.animator;

import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.HumanoidAnimationController;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.enums.Axis;
import com.zigythebird.playeranimcore.enums.PlayState;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import io.github.kosmx.emotes.common.CommonData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityProperty;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket;
import org.geysermc.geyser.api.entity.property.GeyserEntityProperty;
import org.geysermc.geyser.api.entity.property.type.GeyserIntEntityProperty;
import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.entity.properties.GeyserEntityPropertyManager;
import org.geysermc.geyser.entity.properties.type.PropertyType;
import org.geysermc.geyser.entity.type.player.AvatarEntity;
import org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.BedrockPacketsUtils;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.resourcepack.EmoteResourcePack;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

public class GeyserAnimationController extends HumanoidAnimationController implements Runnable, Closeable {
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(100, Thread.ofVirtual()
            .name("emotecraft-animating-")
            .uncaughtExceptionHandler((t, e) -> CommonData.LOGGER.warn("Failed to animate!", e))
            .factory()
    );

    private final Set<Identifier> lastUsedProperties = new HashSet<>(1);
    protected final AvatarEntity avatarEntity;
    protected final Future<?> ticker;

    private final Set<String> dirtyBones = new HashSet<>();

    public GeyserAnimationController(AvatarEntity avatarEntity) {
        this(avatarEntity, EXECUTOR);
    }

    public GeyserAnimationController(AvatarEntity avatarEntity, ScheduledExecutorService executor) {
        super((controller, state, animationSetter) -> PlayState.STOP, MolangLoader::createNewEngine);
        this.avatarEntity = avatarEntity;
        this.ticker = executor.scheduleAtFixedRate(this, 0L, 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void setupNewAnimation() {
        super.setupNewAnimation();
        BedrockPacketsUtils.sendInstantAnimation(EmoteResourcePack.ANIMATION_NAME, this.avatarEntity);
        for (String partKey : this.dirtyBones) {
            updateBone(this.avatarEntity.getPropertyManager(), partKey, new PlayerAnimBone(partKey));
        }
        this.dirtyBones.clear();
    }

    @Override
    public void run() {
        // Check propertyManager
        GeyserEntityPropertyManager propertyManager = this.avatarEntity.getPropertyManager();
        if (propertyManager == null) return;

        AnimationData data = new AnimationData(0, 1.0F);
        tick(data);

        if (!isActive()) return;
        setupAnim(data);

        // Animate via properties
        for (String partKey : this.activeBones.keySet()) {
            if (!this.bones.containsKey(partKey)) {
                CommonData.LOGGER.debug("Unsupported bone: {}!", partKey);
                continue;
            }

            updateBone(propertyManager, partKey, get3DTransform(new PlayerAnimBone(partKey)));
        }

        // Flush
        flushPropertiesImmediately();
        if (this.dirtyBones.isEmpty()) this.dirtyBones.addAll(this.activeBones.keySet());
    }

    @Override
    public PlayerAnimBone get3DTransform(@NonNull PlayerAnimBone bone) {
        bone = super.get3DTransform(bone);

        String boneName = bone.getName();
        if ("left_arm".equals(boneName) || "right_arm".equals(boneName) || "head".equals(boneName)) {
            bone.applyOtherBone(get3DTransform(new PlayerAnimBone("torso")).scale(-1));

        } else if ("left_leg".equals(boneName) || "right_leg".equals(boneName)) {
            bone.applyOtherBone(get3DTransform(new PlayerAnimBone("body")));

        } if ("cape".equals(boneName)) {
            bone.rotX *= -1;
        }
        return bone;
    }

    protected void updateBone(GeyserEntityPropertyManager propertyManager, String partKey, PlayerAnimBone bone) {
        if (!this.bones.containsKey(partKey)) return;
        updateAxis(propertyManager, partKey, TransformType.POSITION, bone.getPosX(), bone.getPosY(), bone.getPosZ());
        updateAxis(propertyManager, partKey, TransformType.ROTATION,
                (float) Math.toDegrees(bone.getRotX()), (float) Math.toDegrees(bone.getRotY()), (float) Math.toDegrees(bone.getRotZ())
        );
        // updateAxis(propertyManager, partKey, TransformType.SCALE, bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());

        if (BendingGeometry.BENDABLE_BONES.contains(partKey)) {
            updateBend(propertyManager, partKey + BendingGeometry.BEND_SUFFIX, bone.bend);
        }
    }

    protected void updateAxis(GeyserEntityPropertyManager propertyManager, String partKey, TransformType type, float x, float y, float z) {
        Map<Axis, Integer> ids = EmotecraftExt.getInstance().getResourcePack().getAxisIds(partKey, type);
        updateProperty(propertyManager, getAvailableProperty(), pack(ids.get(Axis.X), x));
        updateProperty(propertyManager, getAvailableProperty(), pack(ids.get(Axis.Y), y));
        updateProperty(propertyManager, getAvailableProperty(), pack(ids.get(Axis.Z), z));
    }

    protected void updateBend(GeyserEntityPropertyManager propertyManager, String partKey, float bend) {
        EmoteResourcePack resourcePack = EmotecraftExt.getInstance().getResourcePack();

        updateProperty(propertyManager, getAvailableProperty(), pack(
                resourcePack.getAxisIds(partKey, TransformType.ROTATION).get(Axis.X),
                (float) Math.toDegrees(bend)
        ));

        float radius = 2.0f;
        float angle = Math.abs(bend);

        updateProperty(propertyManager, getAvailableProperty(), pack(
                resourcePack.getAxisIds(partKey, TransformType.POSITION).get(Axis.Y),
                (float) (radius * (1 - Math.cos(angle)))
        ));
        updateProperty(propertyManager, getAvailableProperty(), pack(
                resourcePack.getAxisIds(partKey, TransformType.POSITION).get(Axis.Z),
                (float) -(radius * Math.sin(angle))
        ));
    }

    private GeyserIntEntityProperty getAvailableProperty() {
        for (GeyserIntEntityProperty property : EmotecraftExt.getInstance().getResourcePack().getRegisteredProperties()) {
            if (this.lastUsedProperties.contains(property.identifier())) continue;
            this.lastUsedProperties.add(property.identifier());
            return property;
        }

        // Try flush
        flushPropertiesImmediately();
        return getAvailableProperty();
    }

    public static <T> void updateProperty(GeyserEntityPropertyManager propertyManager, @NonNull GeyserEntityProperty<T> property, @Nullable T value) {
        Objects.requireNonNull(property, "property must not be null!");
        if (!(property instanceof PropertyType<T, ? extends EntityProperty> propertyType)) {
            throw new IllegalArgumentException("Invalid property implementation! Got: " + property.getClass().getSimpleName());
        }
        propertyType.apply(propertyManager, value);
    }

    private void flushPropertiesImmediately() {
        GeyserEntityPropertyManager propertyManager = this.avatarEntity.getPropertyManager();
        if (propertyManager == null || !propertyManager.hasProperties()) return;

        SetEntityDataPacket packet = new SetEntityDataPacket();
        packet.setRuntimeEntityId(this.avatarEntity.getGeyserId());
        propertyManager.applyFloatProperties(packet.getProperties().getFloatProperties());
        propertyManager.applyIntProperties(packet.getProperties().getIntProperties());
        this.avatarEntity.getSession().sendUpstreamPacketImmediately(packet);

        try {
            Thread.sleep(Duration.ofMillis(10 + this.avatarEntity.getSession().ping())); // IDK
        } catch (InterruptedException ignored) {}
        this.lastUsedProperties.clear();
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
        BedrockPacketsUtils.sendBobAnimation(this.avatarEntity);
    }

    public static int pack(int id, float value) {
        id = Math.max(-99, Math.min(99, id));
        value = Math.max(-9999.99f, Math.min(9999.99f, value));

        int intValue = Math.round(value * 100f);
        intValue = intValue + 1000000;

        return id * 10000000 + intValue;
    }

    @Override
    public void close() throws IOException {
        this.ticker.cancel(true);
    }

    /**
     * A small hack that allows us to get all registered bones.
     */
    public static Collection<String> getRegisteredBones() {
        try (GeyserAnimationController controller = new GeyserAnimationController(null)) {
            Set<String> bones = new HashSet<>(controller.bones.keySet());
            for (String bendable : BendingGeometry.BENDABLE_BONES) bones.add(bendable + BendingGeometry.BEND_SUFFIX);
            return Collections.unmodifiableCollection(bones);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
