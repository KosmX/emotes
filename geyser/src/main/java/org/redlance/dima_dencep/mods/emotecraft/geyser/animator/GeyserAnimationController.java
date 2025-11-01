package org.redlance.dima_dencep.mods.emotecraft.geyser.animator;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.enums.Axis;
import com.zigythebird.playeranimcore.enums.PlayState;
import com.zigythebird.playeranimcore.enums.TransformType;
import com.zigythebird.playeranimcore.math.Vec3f;
import com.zigythebird.playeranimcore.molang.MolangLoader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityProperty;
import org.cloudburstmc.protocol.bedrock.packet.AnimateEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket;
import org.geysermc.geyser.api.entity.property.GeyserEntityProperty;
import org.geysermc.geyser.api.entity.property.type.GeyserIntEntityProperty;
import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.entity.properties.GeyserEntityPropertyManager;
import org.geysermc.geyser.entity.properties.type.PropertyType;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.redlance.dima_dencep.mods.emotecraft.geyser.EmotecraftExt;
import org.redlance.dima_dencep.mods.emotecraft.geyser.utils.EmoteResourcePack;

import java.time.Duration;
import java.util.*;

/**
 * Bends in the bedrock are not supported, so this feature is not implemented here.
 */
public class GeyserAnimationController extends AnimationController implements Runnable {
    // Bone pivot point positions used to apply custom pivot point translations.
    public static final Map<String, Vec3f> BONE_POSITIONS = Map.of(
            "right_arm", new Vec3f(5, 22, 0),
            "left_arm", new Vec3f(-5, 22, 0),
            "left_leg", new Vec3f(-2f, 12, 0f),
            "right_leg", new Vec3f(2f, 12, 0f),
            "torso", new Vec3f(0, 24, 0),
            "head", new Vec3f(0, 24, 0),
            "body", new Vec3f(0, 12, 0),
            "cape", new Vec3f(0, 24, 2),
            "elytra", new Vec3f(0, 24, 2)
    );

    private final Set<Identifier> lastUsedProperties = new HashSet<>(1);
    private final PlayerEntity playerEntity;

    public GeyserAnimationController(PlayerEntity playerEntity) {
        super((controller, state, animationSetter) -> PlayState.STOP, MolangLoader::createNewEngine);
        this.playerEntity = playerEntity;
    }

    @Override
    public void registerBones() {
        this.registerPlayerAnimBone("body");
        this.registerPlayerAnimBone("right_arm");
        this.registerPlayerAnimBone("left_arm");
        this.registerPlayerAnimBone("right_leg");
        this.registerPlayerAnimBone("left_leg");
        this.registerPlayerAnimBone("head");
        this.registerPlayerAnimBone("torso");
        this.registerPlayerAnimBone("right_item");
        this.registerPlayerAnimBone("left_item");
        this.registerPlayerAnimBone("cape");
        this.registerPlayerAnimBone("elytra");
    }

    @Override
    public void run() {
        // Check propertyManager
        GeyserEntityPropertyManager propertyManager = this.playerEntity.getPropertyManager();
        if (propertyManager == null) return;

        // Start animation
        AnimateEntityPacket animatePacket = new AnimateEntityPacket();
        animatePacket.setAnimation(EmoteResourcePack.ANIMATION_NAME);
        animatePacket.setNextState("default");
        animatePacket.setBlendOutTime(0.0f);
        animatePacket.setStopExpression("query.any_animation_finished");
        animatePacket.setController("__runtime_controller");
        animatePacket.getRuntimeEntityIds().add(this.playerEntity.getGeyserId());
        this.playerEntity.getSession().sendUpstreamPacket(animatePacket);

        AnimationData data = new AnimationData(0, 0.0F);
        setupAnim(data);
        tick(data);

        // Animate via properties
        for (String partKey : BONE_POSITIONS.keySet()) {
            PlayerAnimBone bone = get3DTransform(new PlayerAnimBone(partKey));

            updateAxis(propertyManager, partKey, TransformType.POSITION, bone.getPosX(), bone.getPosY(), bone.getPosZ());
            updateAxis(propertyManager, partKey, TransformType.ROTATION,
                    (float) Math.toDegrees(bone.getRotX()), (float) Math.toDegrees(bone.getRotY()), (float) Math.toDegrees(bone.getRotZ())
            );
        }

        // Flush
        flushPropertiesImmediately();
    }

    private void updateAxis(GeyserEntityPropertyManager propertyManager, String partKey, TransformType type, float x, float y, float z) {
        Map<Axis, Integer> ids = EmotecraftExt.getInstance().getResourcePack().getAxisIds(partKey, type);
        int packedX = pack(ids.get(Axis.X), x);
        int packedY = pack(ids.get(Axis.Y), y);
        int packedZ = pack(ids.get(Axis.Z), z);

        System.out.printf("%s %s: X(id=%d, val=%.2f, packed=%d), Y(id=%d, val=%.2f, packed=%d), Z(id=%d, val=%.2f, packed=%d)%n",
                partKey, type, ids.get(Axis.X), x, packedX, ids.get(Axis.Y), y, packedY, ids.get(Axis.Z), z, packedZ
        );

        updateProperty(propertyManager, getAvailableProperty(), packedX);
        updateProperty(propertyManager, getAvailableProperty(), packedY);
        updateProperty(propertyManager, getAvailableProperty(), packedZ);
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
        GeyserEntityPropertyManager propertyManager = this.playerEntity.getPropertyManager();
        if (propertyManager == null || !propertyManager.hasProperties()) return;

        SetEntityDataPacket packet = new SetEntityDataPacket();
        packet.setRuntimeEntityId(this.playerEntity.getGeyserId());
        propertyManager.applyFloatProperties(packet.getProperties().getFloatProperties());
        propertyManager.applyIntProperties(packet.getProperties().getIntProperties());
        this.playerEntity.getSession().sendUpstreamPacketImmediately(packet);

        try {
            Thread.sleep(Duration.ofMillis(10)); // IDK
        } catch (InterruptedException ignored) {}
        this.lastUsedProperties.clear();
    }

    @Override
    public Vec3f getBonePosition(String name) {
        if (BONE_POSITIONS.containsKey(name)) return BONE_POSITIONS.get(name);
        if (pivotBones.containsKey(name)) return pivotBones.get(name).getPivot();
        return Vec3f.ZERO;
    }

    public static int pack(int id, float value) {
        id = Math.max(0, Math.min(99, id));
        value = Math.max(-9999.99f, Math.min(9999.99f, value));

        int intValue = Math.round(value * 100f);
        intValue = intValue + 1000000;

        return id * 10000000 + intValue;
    }
}
