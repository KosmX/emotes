package org.redlance.dima_dencep.mods.emotecraft.geyser.pal.molang;

import team.unnamed.mocha.runtime.value.ObjectValue;

public final class MolangQueries {
    public static final String ACTOR_COUNT = "actor_count";
    public static final String BLOCKING = "blocking";
    public static final String BODY_X_ROTATION = "body_x_rotation";
    public static final String BODY_Y_ROTATION = "body_y_rotation";
    public static final String CARDINAL_FACING = "cardinal_facing";
    public static final String CARDINAL_FACING_2D = "cardinal_facing_2d";
    public static final String CARDINAL_PLAYER_FACING = "cardinal_player_facing";
    public static final String DAY = "day";
    public static final String DEATH_TICKS = "death_ticks";
    public static final String DISTANCE_FROM_CAMERA = "distance_from_camera";
    public static final String EQUIPMENT_COUNT = "equipment_count";
    public static final String FRAME_ALPHA = "frame_alpha";
    public static final String GET_ACTOR_INFO_ID = "get_actor_info_id";
    public static final String GROUND_SPEED = "ground_speed";
    public static final String HAS_CAPE = "has_cape";
    public static final String HAS_COLLISION = "has_collision";
    public static final String HAS_GRAVITY = "has_gravity";
    public static final String HAS_HEAD_GEAR = "has_head_gear";
    public static final String HAS_OWNER = "has_owner";
    public static final String HAS_PLAYER_RIDER = "has_player_rider";
    public static final String HAS_RIDER = "has_rider";
    public static final String HEAD_X_ROTATION = "head_x_rotation";
    public static final String HEAD_Y_ROTATION = "head_y_rotation";
    public static final String HEALTH = "health";
    public static final String HURT_TIME = "hurt_time";
    public static final String INVULNERABLE_TICKS = "invulnerable_ticks";
    public static final String IS_ALIVE = "is_alive";
    public static final String IS_ANGRY = "is_angry";
    public static final String IS_BABY = "is_baby";
    public static final String IS_BREATHING = "is_breathing";
    public static final String IS_FIRE_IMMUNE = "is_fire_immune";
    public static final String IS_FIRST_PERSON = "is_first_person";
    public static final String IS_IN_CONTACT_WITH_WATER = "is_in_contact_with_water";
    public static final String IS_IN_LAVA = "is_in_lava";
    public static final String IS_IN_WATER = "is_in_water";
    public static final String IS_IN_WATER_OR_RAIN = "is_in_water_or_rain";
    public static final String IS_INVISIBLE = "is_invisible";
    public static final String IS_LEASHED = "is_leashed";
    public static final String IS_MOVING = "is_moving";
    public static final String IS_ON_FIRE = "is_on_fire";
    public static final String IS_ON_GROUND = "is_on_ground";
    public static final String IS_RIDING = "is_riding";
    public static final String IS_SADDLED = "is_saddled";
    public static final String IS_SILENT = "is_silent";
    public static final String IS_SLEEPING = "is_sleeping";
    public static final String IS_SNEAKING = "is_sneaking";
    public static final String IS_SPRINTING = "is_sprinting";
    public static final String IS_SWIMMING = "is_swimming";
    public static final String IS_USING_ITEM = "is_using_item";
    public static final String IS_WALL_CLIMBING = "is_wall_climbing";
    public static final String LIFE_TIME = "life_time";
    public static final String LIMB_SWING = "limb_swing";
    public static final String LIMB_SWING_AMOUNT = "limb_swing_amount";
    public static final String MAIN_HAND_ITEM_MAX_DURATION = "main_hand_item_max_duration";
    public static final String MAIN_HAND_ITEM_USE_DURATION = "main_hand_item_use_duration";
    public static final String MAX_HEALTH = "max_health";
    public static final String MOON_BRIGHTNESS = "moon_brightness";
    public static final String MOON_PHASE = "moon_phase";
    public static final String MOVEMENT_DIRECTION = "movement_direction";
    public static final String PLAYER_LEVEL = "player_level";
    public static final String RIDER_BODY_X_ROTATION = "rider_body_x_rotation";
    public static final String RIDER_BODY_Y_ROTATION = "rider_body_y_rotation";
    public static final String RIDER_HEAD_X_ROTATION = "rider_head_x_rotation";
    public static final String RIDER_HEAD_Y_ROTATION = "rider_head_y_rotation";
    public static final String SCALE = "scale";
    public static final String SLEEP_ROTATION = "sleep_rotation";
    public static final String TIME_OF_DAY = "time_of_day";
    public static final String TIME_STAMP = "time_stamp";
    public static final String VERTICAL_SPEED = "vertical_speed";
    public static final String YAW_SPEED = "yaw_speed";

    public static void setDefaultQueryValues(ObjectValue binding) {
//        MolangLoader.setDoubleQuery(binding, ACTOR_COUNT, actor -> Minecraft.getInstance().levelRenderer.renderedEntities);
//        MolangLoader.setDoubleQuery(binding, CARDINAL_PLAYER_FACING, actor -> ((PlayerAnimationController) actor).getPlayer().getDirection().ordinal());
//        MolangLoader.setDoubleQuery(binding, DAY, actor -> ((PlayerAnimationController) actor).getPlayer().level().getGameTime() / 24000d);
//        MolangLoader.setDoubleQuery(binding, FRAME_ALPHA, actor -> actor.getAnimationData().getPartialTick());
//        MolangLoader.setBoolQuery(binding, HAS_CAPE, actor -> ((PlayerAnimationController) actor).getPlayer().getSkin().capeTexture() != null);
//        MolangLoader.setBoolQuery(binding, IS_FIRST_PERSON, actor -> ((PlayerAnimationController) actor).getPlayer().isLocalPlayer() && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON);
//        MolangLoader.setDoubleQuery(binding, LIFE_TIME, actor -> actor.isActive() ? actor.getAnimationTime() : 0);
//        MolangLoader.setDoubleQuery(binding, MOON_BRIGHTNESS, actor -> ((PlayerAnimationController) actor).getPlayer().level().getMoonBrightness());
//        MolangLoader.setDoubleQuery(binding, MOON_PHASE, actor -> ((PlayerAnimationController) actor).getPlayer().level().getMoonPhase());
//        MolangLoader.setDoubleQuery(binding, PLAYER_LEVEL, actor -> ((PlayerAnimationController) actor).getPlayer().experienceLevel);
//        MolangLoader.setDoubleQuery(binding, TIME_OF_DAY, actor -> ((PlayerAnimationController) actor).getPlayer().level().getDayTime() / 24000d);
//        MolangLoader.setDoubleQuery(binding, TIME_STAMP, actor -> ((PlayerAnimationController) actor).getPlayer().level().getGameTime());

//        setDefaultEntityQueryValues(binding);
//        setDefaultLivingEntityQueryValues(binding);
    }

//    private static void setDefaultEntityQueryValues(ObjectValue binding) {
//        MolangLoader.setDoubleQuery(binding, BODY_X_ROTATION, actor -> ((PlayerAnimationController) actor).getPlayer().getViewXRot(actor.getAnimationData().getPartialTick()));
//        MolangLoader.setDoubleQuery(binding, BODY_Y_ROTATION, actor -> ((PlayerAnimationController) actor).getPlayer() instanceof LivingEntity living ? Mth.lerp(actor.getAnimationData().getPartialTick(), living.yBodyRotO, living.yBodyRot) : ((PlayerAnimationController) actor).getPlayer().getViewYRot(actor.getAnimationData().getPartialTick()));
//        MolangLoader.setDoubleQuery(binding, CARDINAL_FACING, actor -> ((PlayerAnimationController) actor).getPlayer().getDirection().get3DDataValue());
//        MolangLoader.setDoubleQuery(binding, CARDINAL_FACING_2D, actor -> {
//            int directionId = ((PlayerAnimationController) actor).getPlayer().getDirection().get3DDataValue();
//
//            return directionId < 2 ? 6 : directionId;
//        });
//        MolangLoader.setDoubleQuery(binding, DISTANCE_FROM_CAMERA, actor -> Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().distanceTo(((PlayerAnimationController) actor).getPlayer().position()));
//        MolangLoader.setDoubleQuery(binding, GET_ACTOR_INFO_ID, actor -> ((PlayerAnimationController) actor).getPlayer().getId());
//        MolangLoader.setDoubleQuery(binding, EQUIPMENT_COUNT, actor -> ((PlayerAnimationController) actor).getPlayer() instanceof EquipmentUser armorable ? Arrays.stream(EquipmentSlot.values()).filter(EquipmentSlot::isArmor).filter(slot -> !armorable.getItemBySlot(slot).isEmpty()).count() : 0);
//        MolangLoader.setBoolQuery(binding, HAS_COLLISION, actor -> !((PlayerAnimationController) actor).getPlayer().noPhysics);
//        MolangLoader.setBoolQuery(binding, HAS_GRAVITY, actor -> !((PlayerAnimationController) actor).getPlayer().isNoGravity());
//        MolangLoader.setBoolQuery(binding, HAS_OWNER, actor -> ((PlayerAnimationController) actor).getPlayer() instanceof OwnableEntity ownable && ownable.getOwner() != null);
//        MolangLoader.setBoolQuery(binding, HAS_PLAYER_RIDER, actor -> ((PlayerAnimationController) actor).getPlayer().hasPassenger(Player.class::isInstance));
//        MolangLoader.setBoolQuery(binding, HAS_RIDER, actor -> ((PlayerAnimationController) actor).getPlayer().isVehicle());
//        MolangLoader.setBoolQuery(binding, IS_ALIVE, actor -> ((PlayerAnimationController) actor).getPlayer().isAlive());
//        MolangLoader.setBoolQuery(binding, IS_ANGRY, actor -> ((PlayerAnimationController) actor).getPlayer() instanceof NeutralMob neutralMob && neutralMob.isAngry());
//        MolangLoader.setBoolQuery(binding, IS_BREATHING, actor -> ((PlayerAnimationController) actor).getPlayer().getAirSupply() >= ((PlayerAnimationController) actor).getPlayer().getMaxAirSupply());
//        MolangLoader.setBoolQuery(binding, IS_FIRE_IMMUNE, actor -> ((PlayerAnimationController) actor).getPlayer().getType().fireImmune());
//        MolangLoader.setBoolQuery(binding, IS_INVISIBLE, actor -> ((PlayerAnimationController) actor).getPlayer().isInvisible());
//        MolangLoader.setBoolQuery(binding, IS_IN_CONTACT_WITH_WATER, actor -> ((PlayerAnimationController) actor).getPlayer().isInWaterOrRain());
//        MolangLoader.setBoolQuery(binding, IS_IN_LAVA, actor -> ((PlayerAnimationController) actor).getPlayer().isInLava());
//        MolangLoader.setBoolQuery(binding, IS_IN_WATER, actor -> ((PlayerAnimationController) actor).getPlayer().isInWater());
//        MolangLoader.setBoolQuery(binding, IS_IN_WATER_OR_RAIN, actor -> ((PlayerAnimationController) actor).getPlayer().isInWaterOrRain());
//        MolangLoader.setBoolQuery(binding, IS_LEASHED, actor -> ((PlayerAnimationController) actor).getPlayer() instanceof Leashable leashable && leashable.isLeashed());
//        MolangLoader.setBoolQuery(binding, IS_MOVING, actor -> actor.getAnimationData().isMoving());
//        MolangLoader.setBoolQuery(binding, IS_ON_FIRE, actor -> ((PlayerAnimationController) actor).getPlayer().isOnFire());
//        MolangLoader.setBoolQuery(binding, IS_ON_GROUND, actor -> ((PlayerAnimationController) actor).getPlayer().onGround());
//        MolangLoader.setBoolQuery(binding, IS_RIDING, actor -> ((PlayerAnimationController) actor).getPlayer().isPassenger());
////        MolangLoader.setBoolQuery(binding, IS_SADDLED, actor -> ((PlayerAnimationController) actor).getPlayer() instanceof EquipmentUser saddleable && !saddleable.getItemBySlot(EquipmentSlot.SADDLE).isEmpty());
//        MolangLoader.setBoolQuery(binding, IS_SILENT, actor -> ((PlayerAnimationController) actor).getPlayer().isSilent());
//        MolangLoader.setBoolQuery(binding, IS_SNEAKING, actor -> ((PlayerAnimationController) actor).getPlayer().isCrouching());
//        MolangLoader.setBoolQuery(binding, IS_SPRINTING, actor -> ((PlayerAnimationController) actor).getPlayer().isSprinting());
//        MolangLoader.setBoolQuery(binding, IS_SWIMMING, actor -> ((PlayerAnimationController) actor).getPlayer().isSwimming());
//        MolangLoader.setDoubleQuery(binding, MOVEMENT_DIRECTION, actor -> actor.getAnimationData().isMoving() ? Direction.getNearest(((PlayerAnimationController) actor).getPlayer().getDeltaMovement()).get3DDataValue() : 6);
//        MolangLoader.setDoubleQuery(binding, RIDER_BODY_X_ROTATION, actor -> ((PlayerAnimationController) actor).getPlayer().isVehicle() ? ((PlayerAnimationController) actor).getPlayer().getFirstPassenger() instanceof LivingEntity ? 0 : ((PlayerAnimationController) actor).getPlayer().getFirstPassenger().getViewXRot(actor.getAnimationData().getPartialTick()) : 0);
//        MolangLoader.setDoubleQuery(binding, RIDER_BODY_Y_ROTATION, actor -> ((PlayerAnimationController) actor).getPlayer().isVehicle() ? ((PlayerAnimationController) actor).getPlayer().getFirstPassenger() instanceof LivingEntity living ? Mth.lerp(actor.getAnimationData().getPartialTick(), living.yBodyRotO, living.yBodyRot) : ((PlayerAnimationController) actor).getPlayer().getFirstPassenger().getViewYRot(actor.getAnimationData().getPartialTick()) : 0);
//        MolangLoader.setDoubleQuery(binding, RIDER_HEAD_X_ROTATION, actor -> ((PlayerAnimationController) actor).getPlayer().getFirstPassenger() instanceof LivingEntity living ? living.getViewXRot(actor.getAnimationData().getPartialTick()) : 0);
//        MolangLoader.setDoubleQuery(binding, RIDER_HEAD_Y_ROTATION, actor -> ((PlayerAnimationController) actor).getPlayer().getFirstPassenger() instanceof LivingEntity living ? living.getViewYRot(actor.getAnimationData().getPartialTick()) : 0);
//        MolangLoader.setDoubleQuery(binding, VERTICAL_SPEED, actor -> ((PlayerAnimationController) actor).getPlayer().getDeltaMovement().y);
//        MolangLoader.setDoubleQuery(binding, YAW_SPEED, actor -> ((PlayerAnimationController) actor).getPlayer().getYRot() - ((PlayerAnimationController) actor).getPlayer().yRotO);
//    }
//
//    private static void setDefaultLivingEntityQueryValues(ObjectValue binding) {
//        MolangLoader.setBoolQuery(binding, BLOCKING, actor -> ((PlayerAnimationController) actor).getPlayer().isBlocking());
//        MolangLoader.setDoubleQuery(binding, DEATH_TICKS, actor -> ((PlayerAnimationController) actor).getPlayer().deathTime == 0 ? 0 : ((PlayerAnimationController) actor).getPlayer().deathTime + actor.getAnimationData().getPartialTick());
//        MolangLoader.setDoubleQuery(binding, GROUND_SPEED, actor -> ((PlayerAnimationController) actor).getPlayer().getDeltaMovement().horizontalDistance());
//        MolangLoader.setBoolQuery(binding, HAS_HEAD_GEAR, actor -> !((PlayerAnimationController) actor).getPlayer().getItemBySlot(EquipmentSlot.HEAD).isEmpty());
//        MolangLoader.setDoubleQuery(binding, HEAD_X_ROTATION, actor -> ((PlayerAnimationController) actor).getPlayer().getViewXRot(actor.getAnimationData().getPartialTick()));
//        MolangLoader.setDoubleQuery(binding, HEAD_Y_ROTATION, actor -> ((PlayerAnimationController) actor).getPlayer().getViewYRot(actor.getAnimationData().getPartialTick()));
//        MolangLoader.setDoubleQuery(binding, HEALTH, actor -> ((PlayerAnimationController) actor).getPlayer().getHealth());
//        MolangLoader.setDoubleQuery(binding, HURT_TIME, actor -> ((PlayerAnimationController) actor).getPlayer().hurtTime == 0 ? 0 : ((PlayerAnimationController) actor).getPlayer().hurtTime - actor.getAnimationData().getPartialTick());
//        MolangLoader.setDoubleQuery(binding, INVULNERABLE_TICKS, actor -> ((PlayerAnimationController) actor).getPlayer().invulnerableTime == 0 ? 0 : ((PlayerAnimationController) actor).getPlayer().invulnerableTime - actor.getAnimationData().getPartialTick());
//        MolangLoader.setBoolQuery(binding, IS_BABY, actor -> ((PlayerAnimationController) actor).getPlayer().isBaby());
//        MolangLoader.setBoolQuery(binding, IS_SLEEPING, actor -> ((PlayerAnimationController) actor).getPlayer().isSleeping());
//        MolangLoader.setBoolQuery(binding, IS_USING_ITEM, actor -> ((PlayerAnimationController) actor).getPlayer().isUsingItem());
//        MolangLoader.setBoolQuery(binding, IS_WALL_CLIMBING, actor -> ((PlayerAnimationController) actor).getPlayer().onClimbable());
//        MolangLoader.setDoubleQuery(binding, LIMB_SWING, actor -> ((PlayerAnimationController) actor).getPlayer().walkAnimation.position());
//        MolangLoader.setDoubleQuery(binding, LIMB_SWING_AMOUNT, actor -> ((PlayerAnimationController) actor).getPlayer().walkAnimation.speed(actor.getAnimationData().getPartialTick()));
//        MolangLoader.setDoubleQuery(binding, MAIN_HAND_ITEM_MAX_DURATION, actor -> ((PlayerAnimationController) actor).getPlayer().getMainHandItem().getUseDuration(((PlayerAnimationController) actor).getPlayer()));
//        MolangLoader.setDoubleQuery(binding, MAIN_HAND_ITEM_USE_DURATION, actor -> ((PlayerAnimationController) actor).getPlayer().getUsedItemHand() == InteractionHand.MAIN_HAND ? ((PlayerAnimationController) actor).getPlayer().getTicksUsingItem() / 20d + actor.getAnimationData().getPartialTick() : 0);
//        MolangLoader.setDoubleQuery(binding, MAX_HEALTH, actor -> ((PlayerAnimationController) actor).getPlayer().getMaxHealth());
//        MolangLoader.setDoubleQuery(binding, SCALE, actor -> ((PlayerAnimationController) actor).getPlayer().getScale());
//        MolangLoader.setDoubleQuery(binding, SLEEP_ROTATION, actor -> Optional.ofNullable(((PlayerAnimationController) actor).getPlayer().getBedOrientation()).map(Direction::toYRot).orElse(0f));
//    }
}
