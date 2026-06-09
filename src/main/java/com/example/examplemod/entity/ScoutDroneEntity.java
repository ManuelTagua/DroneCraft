package com.example.examplemod.entity;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.item.DroneControllerItem;
import com.example.examplemod.menu.ScoutDroneModuleMenu;
import com.example.examplemod.network.DroneCameraPacket;
import com.example.examplemod.network.ModNetworking;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkHooks;

public class ScoutDroneEntity extends Entity
{
    private static final EntityDataAccessor<Boolean> HAS_RANGE_MODULE = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_ZOOM_MODULE = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_LIGHT_MODULE = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_EXPLOSIVE_MODULE = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_RETURN_MODULE = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_SPEED_MODULE = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_THERMAL_CAMERA_MODULE = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LIGHT_ENABLED = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> THERMAL_CAMERA_ENABLED = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> RANGE_MODULE_LEVEL = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPEED_MODULE_LEVEL = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> REMOTE_CONTROLLED = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LANDING_MODE = SynchedEntityData.defineId(ScoutDroneEntity.class, EntityDataSerializers.BOOLEAN);

    public static final String DRONE_MODULES_TAG = "DroneModules";
    public static final String RANGE_MODULE_TAG = "RangeModule";
    public static final String CAMERA_MODULE_TAG = "CameraModule";
    public static final String UTILITY_MODULE_TAG = "UtilityModule";
    public static final String EXPLOSIVE_MODULE_TAG = "ExplosiveModule";
    public static final String RETURN_MODULE_TAG = "ReturnModule";
    public static final String SPEED_MODULE_TAG = "SpeedModule";
    public static final String THERMAL_CAMERA_MODULE_TAG = "ThermalCameraModule";

    private static final float MAX_HEALTH = 6.0F;
    private static final double BASE_CONTROL_RANGE = 32.0D;
    private static final double BASIC_RANGE_MODULE_CONTROL_RANGE = 64.0D;
    private static final double ADVANCED_RANGE_MODULE_CONTROL_RANGE = 128.0D;
    private static final double ELITE_RANGE_MODULE_CONTROL_RANGE = 256.0D;
    private static final double INFINITE_RANGE_MODULE_CONTROL_RANGE = 1_000_000.0D;
    private static final double MAX_HORIZONTAL_SPEED = 0.28D;
    private static final double MAX_VERTICAL_SPEED = 0.20D;
    private static final double BASIC_SPEED_MODULE_MULTIPLIER = 1.5D;
    private static final double ADVANCED_SPEED_MODULE_MULTIPLIER = 2.0D;
    private static final double ELITE_SPEED_MODULE_MULTIPLIER = 3.0D;
    private static final double THERMAL_CAMERA_RANGE = 32.0D;
    private static final int THERMAL_CAMERA_EFFECT_DURATION = 12;
    private static final int THERMAL_CAMERA_REFRESH_TICKS = 5;
    private static final double ACCELERATION_FACTOR = 0.18D;
    private static final double FRICTION_FACTOR = 0.85D;
    private static final double LANDING_SPEED = 0.10D;
    private static final int MAX_LANDING_TICKS = 512;
    private static final float MAX_ROTOR_SPEED = 2.8F;
    private static final float LANDING_ROTOR_SPEED = 1.35F;
    private static final float ROTOR_SPEED_LERP = 0.08F;
    private static final float ROTOR_STOP_EPSILON = 0.001F;
    private static final float ROTOR_FULL_TURN = (float) (Math.PI * 2.0D);

    private static final Component DRONE_ALREADY_CONTROLLED = Component.literal("Este dron ya está siendo controlado.");
    private static final Component DRONE_MODE_ACTIVATED = Component.literal("Modo dron activado.");
    private static final Component DRONE_MODE_DEACTIVATED = Component.literal("Modo dron desactivado.");
    private static final Component SIGNAL_LOST = Component.literal("Señal perdida: dron fuera de rango.");

    private float health = MAX_HEALTH;
    private UUID controllingPlayerUUID;
    private boolean remoteControlled;
    private boolean landingMode;
    private int landingTicks;
    private Vec3 controllerAnchorPosition;
    private float controllerAnchorYaw;
    private float controllerAnchorPitch;
    private boolean pilotForward;
    private boolean pilotBackward;
    private boolean pilotLeft;
    private boolean pilotRight;
    private boolean pilotUp;
    private boolean pilotDown;
    private int pilotInputTicks;
    private Vec3 droneVelocity = Vec3.ZERO;
    private float targetYaw;
    private float targetPitch;
    private ItemStack rangeModuleStack = ItemStack.EMPTY;
    private ItemStack cameraModuleStack = ItemStack.EMPTY;
    private ItemStack utilityModuleStack = ItemStack.EMPTY;
    private ItemStack explosiveModuleStack = ItemStack.EMPTY;
    private ItemStack returnModuleStack = ItemStack.EMPTY;
    private ItemStack speedModuleStack = ItemStack.EMPTY;
    private ItemStack thermalCameraModuleStack = ItemStack.EMPTY;
    private float rotorSpeed;
    private float rotorSpeedO;
    private float rotorAngle;
    private float rotorAngleO;

    public ScoutDroneEntity(EntityType<? extends ScoutDroneEntity> entityType, Level level)
    {
        super(entityType, level);
        setNoGravity(true);
        targetYaw = getYRot();
        targetPitch = getXRot();
    }

    @Override
    protected void defineSynchedData()
    {
        entityData.define(HAS_RANGE_MODULE, false);
        entityData.define(HAS_ZOOM_MODULE, false);
        entityData.define(HAS_LIGHT_MODULE, false);
        entityData.define(HAS_EXPLOSIVE_MODULE, false);
        entityData.define(HAS_RETURN_MODULE, false);
        entityData.define(HAS_SPEED_MODULE, false);
        entityData.define(HAS_THERMAL_CAMERA_MODULE, false);
        entityData.define(LIGHT_ENABLED, false);
        entityData.define(THERMAL_CAMERA_ENABLED, false);
        entityData.define(RANGE_MODULE_LEVEL, 0);
        entityData.define(SPEED_MODULE_LEVEL, 0);
        entityData.define(REMOTE_CONTROLLED, false);
        entityData.define(LANDING_MODE, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        if (tag.contains("Health")) {
            health = tag.getFloat("Health");
        }
        if (tag.hasUUID("ControllingPlayerUUID")) {
            controllingPlayerUUID = tag.getUUID("ControllingPlayerUUID");
            remoteControlled = tag.getBoolean("RemoteControlled");
        } else {
            controllingPlayerUUID = null;
            remoteControlled = false;
        }
        landingMode = tag.getBoolean("LandingMode");
        landingTicks = tag.getInt("LandingTicks");
        syncControlVisualState();
        setRangeModuleStack(tag.contains(RANGE_MODULE_TAG)
                ? ItemStack.of(tag.getCompound(RANGE_MODULE_TAG))
                : tag.contains("Module") ? ItemStack.of(tag.getCompound("Module")) : ItemStack.EMPTY);
        setCameraModuleStack(tag.contains(CAMERA_MODULE_TAG) ? ItemStack.of(tag.getCompound(CAMERA_MODULE_TAG)) : ItemStack.EMPTY);
        setUtilityModuleStack(tag.contains(UTILITY_MODULE_TAG) ? ItemStack.of(tag.getCompound(UTILITY_MODULE_TAG)) : ItemStack.EMPTY);
        setExplosiveModuleStack(tag.contains(EXPLOSIVE_MODULE_TAG) ? ItemStack.of(tag.getCompound(EXPLOSIVE_MODULE_TAG)) : ItemStack.EMPTY);
        setReturnModuleStack(tag.contains(RETURN_MODULE_TAG) ? ItemStack.of(tag.getCompound(RETURN_MODULE_TAG)) : ItemStack.EMPTY);
        setSpeedModuleStack(tag.contains(SPEED_MODULE_TAG) ? ItemStack.of(tag.getCompound(SPEED_MODULE_TAG)) : ItemStack.EMPTY);
        setThermalCameraModuleStack(tag.contains(THERMAL_CAMERA_MODULE_TAG) ? ItemStack.of(tag.getCompound(THERMAL_CAMERA_MODULE_TAG)) : ItemStack.EMPTY);
        setLightEnabled(tag.getBoolean("LightEnabled") && hasLightModule());
        setThermalCameraEnabled(tag.getBoolean("ThermalCameraEnabled") && hasThermalCameraModule());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        tag.putFloat("Health", health);
        tag.putBoolean("RemoteControlled", remoteControlled);
        if (controllingPlayerUUID != null) {
            tag.putUUID("ControllingPlayerUUID", controllingPlayerUUID);
        }
        tag.putBoolean("LandingMode", landingMode);
        tag.putInt("LandingTicks", landingTicks);
        if (!rangeModuleStack.isEmpty()) {
            tag.put(RANGE_MODULE_TAG, rangeModuleStack.save(new CompoundTag()));
        }
        if (!cameraModuleStack.isEmpty()) {
            tag.put(CAMERA_MODULE_TAG, cameraModuleStack.save(new CompoundTag()));
        }
        if (!utilityModuleStack.isEmpty()) {
            tag.put(UTILITY_MODULE_TAG, utilityModuleStack.save(new CompoundTag()));
        }
        if (!explosiveModuleStack.isEmpty()) {
            tag.put(EXPLOSIVE_MODULE_TAG, explosiveModuleStack.save(new CompoundTag()));
        }
        if (!returnModuleStack.isEmpty()) {
            tag.put(RETURN_MODULE_TAG, returnModuleStack.save(new CompoundTag()));
        }
        if (!speedModuleStack.isEmpty()) {
            tag.put(SPEED_MODULE_TAG, speedModuleStack.save(new CompoundTag()));
        }
        if (!thermalCameraModuleStack.isEmpty()) {
            tag.put(THERMAL_CAMERA_MODULE_TAG, thermalCameraModuleStack.save(new CompoundTag()));
        }
        tag.putBoolean("LightEnabled", isLightEnabled());
        tag.putBoolean("ThermalCameraEnabled", isThermalCameraEnabled());
    }

    @Override
    public boolean isPickable()
    {
        return !isRemoved();
    }

    @Override
    public void tick()
    {
        super.tick();
        setNoGravity(true);

        if (level().isClientSide) {
            tickRotorAnimation();
        }

        if (!level().isClientSide && remoteControlled) {
            ServerPlayer controller = getController();
            if (controller == null) {
                clearController();
                return;
            }
            if (!controller.isAlive() || controller.level() != level()) {
                stopControlling(DRONE_MODE_DEACTIVATED);
                return;
            }

            if (distanceToSqr(controller) > getMaxControlRangeSqr()) {
                stopControlling(SIGNAL_LOST);
                return;
            }

            keepControllerStill(controller);
            applyLightEffect(controller);
            applyThermalCameraEffect(controller);
            updateControlledRotation();
            applyControlledMovement();
        }

        if (!level().isClientSide && landingMode && !remoteControlled) {
            tickLanding();
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand)
    {
        if (player.isShiftKeyDown()) {
            if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                openModuleMenu(serverPlayer);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof DroneControllerItem) {
            return DroneControllerItem.linkToDrone(stack, player, this);
        }

        return super.interact(player, hand);
    }

    public boolean startControlling(ServerPlayer player)
    {
        if (remoteControlled && controllingPlayerUUID != null && !controllingPlayerUUID.equals(player.getUUID())) {
            player.displayClientMessage(DRONE_ALREADY_CONTROLLED, true);
            return false;
        }
        if (distanceToSqr(player) > getMaxControlRangeSqr()) {
            player.displayClientMessage(SIGNAL_LOST, true);
            return false;
        }

        remoteControlled = true;
        landingMode = false;
        syncControlVisualState();
        landingTicks = 0;
        controllingPlayerUUID = player.getUUID();
        controllerAnchorPosition = player.position();
        controllerAnchorYaw = player.getYRot();
        controllerAnchorPitch = player.getXRot();
        player.displayClientMessage(DRONE_MODE_ACTIVATED, true);
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DroneCameraPacket(getId(), true));
        return true;
    }

    public void stopControlling(Component message)
    {
        ServerPlayer controller = getController();
        if (controller != null) {
            controller.displayClientMessage(message, true);
            controller.removeEffect(MobEffects.NIGHT_VISION);
            ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> controller), new DroneCameraPacket(getId(), false));
        }

        clearController();
        startLanding();
    }

    public void clearController()
    {
        remoteControlled = false;
        syncControlVisualState();
        controllingPlayerUUID = null;
        controllerAnchorPosition = null;
        pilotForward = false;
        pilotBackward = false;
        pilotLeft = false;
        pilotRight = false;
        pilotUp = false;
        pilotDown = false;
        pilotInputTicks = 0;
        droneVelocity = Vec3.ZERO;
        setLightEnabled(false);
        setThermalCameraEnabled(false);
    }

    public boolean isControlledBy(Player player)
    {
        return remoteControlled && controllingPlayerUUID != null && controllingPlayerUUID.equals(player.getUUID());
    }

    public boolean isRemoteControlled()
    {
        return level().isClientSide ? entityData.get(REMOTE_CONTROLLED) : remoteControlled;
    }

    public boolean isLandingMode()
    {
        return level().isClientSide ? entityData.get(LANDING_MODE) : landingMode;
    }

    public float getRotorAngle(float partialTick)
    {
        return Mth.lerp(partialTick, rotorAngleO, rotorAngle);
    }

    public float getRotorSpeed(float partialTick)
    {
        return Mth.lerp(partialTick, rotorSpeedO, rotorSpeed);
    }

    public boolean hasRangeModule()
    {
        return entityData.get(HAS_RANGE_MODULE);
    }

    public boolean hasZoomModule()
    {
        return entityData.get(HAS_ZOOM_MODULE);
    }

    public boolean hasLightModule()
    {
        return entityData.get(HAS_LIGHT_MODULE);
    }

    public boolean hasExplosiveModule()
    {
        return entityData.get(HAS_EXPLOSIVE_MODULE);
    }

    public boolean hasReturnModule()
    {
        return entityData.get(HAS_RETURN_MODULE);
    }

    public boolean hasSpeedModule()
    {
        return entityData.get(HAS_SPEED_MODULE);
    }

    public boolean hasThermalCameraModule()
    {
        return entityData.get(HAS_THERMAL_CAMERA_MODULE);
    }

    public double getSpeedMultiplier()
    {
        return switch (entityData.get(SPEED_MODULE_LEVEL)) {
            case 1 -> BASIC_SPEED_MODULE_MULTIPLIER;
            case 2 -> ADVANCED_SPEED_MODULE_MULTIPLIER;
            case 3 -> ELITE_SPEED_MODULE_MULTIPLIER;
            default -> 1.0D;
        };
    }

    public boolean isLightEnabled()
    {
        return entityData.get(LIGHT_ENABLED);
    }

    public void setLightEnabled(boolean enabled)
    {
        entityData.set(LIGHT_ENABLED, enabled && hasLightModule());
    }

    public boolean isThermalCameraEnabled()
    {
        return entityData.get(THERMAL_CAMERA_ENABLED);
    }

    public void setThermalCameraEnabled(boolean enabled)
    {
        entityData.set(THERMAL_CAMERA_ENABLED, enabled && hasThermalCameraModule());
    }

    public double getMaxControlRange()
    {
        return switch (entityData.get(RANGE_MODULE_LEVEL)) {
            case 1 -> BASIC_RANGE_MODULE_CONTROL_RANGE;
            case 2 -> ADVANCED_RANGE_MODULE_CONTROL_RANGE;
            case 3 -> ELITE_RANGE_MODULE_CONTROL_RANGE;
            case 4 -> INFINITE_RANGE_MODULE_CONTROL_RANGE;
            default -> BASE_CONTROL_RANGE;
        };
    }

    public boolean hasInfiniteRangeModule()
    {
        return entityData.get(RANGE_MODULE_LEVEL) == 4;
    }

    private static int getRangeModuleLevel(ItemStack stack)
    {
        if (stack.is(ExampleMod.INFINITE_RANGE_MODULE.get())) {
            return 4;
        }
        if (stack.is(ExampleMod.ELITE_RANGE_MODULE.get())) {
            return 3;
        }
        if (stack.is(ExampleMod.ADVANCED_RANGE_MODULE.get())) {
            return 2;
        }
        if (isBasicRangeModule(stack)) {
            return 1;
        }
        return 0;
    }

    private static int getSpeedModuleLevel(ItemStack stack)
    {
        if (stack.is(ExampleMod.ELITE_SPEED_MODULE.get())) {
            return 3;
        }
        if (stack.is(ExampleMod.ADVANCED_SPEED_MODULE.get())) {
            return 2;
        }
        if (stack.is(ExampleMod.SPEED_MODULE.get())) {
            return 1;
        }
        return 0;
    }

    public ItemStack getModuleStack()
    {
        return getRangeModuleStack();
    }

    public void setModuleStack(ItemStack stack)
    {
        setRangeModuleStack(stack);
    }

    public ItemStack getRangeModuleStack()
    {
        return rangeModuleStack;
    }

    public void setRangeModuleStack(ItemStack stack)
    {
        rangeModuleStack = isRangeModule(stack) ? copySingle(stack) : ItemStack.EMPTY;
        entityData.set(HAS_RANGE_MODULE, !rangeModuleStack.isEmpty());
        entityData.set(RANGE_MODULE_LEVEL, getRangeModuleLevel(rangeModuleStack));
    }

    public ItemStack getCameraModuleStack()
    {
        return cameraModuleStack;
    }

    public void setCameraModuleStack(ItemStack stack)
    {
        cameraModuleStack = sanitizeModuleStack(stack, ExampleMod.ZOOM_MODULE.get());
        entityData.set(HAS_ZOOM_MODULE, !cameraModuleStack.isEmpty());
    }

    public ItemStack getUtilityModuleStack()
    {
        return utilityModuleStack;
    }

    public void setUtilityModuleStack(ItemStack stack)
    {
        utilityModuleStack = sanitizeModuleStack(stack, ExampleMod.LIGHT_MODULE.get());
        entityData.set(HAS_LIGHT_MODULE, !utilityModuleStack.isEmpty());
        if (utilityModuleStack.isEmpty()) {
            setLightEnabled(false);
        }
    }

    public ItemStack getExplosiveModuleStack()
    {
        return explosiveModuleStack;
    }

    public void setExplosiveModuleStack(ItemStack stack)
    {
        explosiveModuleStack = sanitizeModuleStack(stack, ExampleMod.EXPLOSIVE_MODULE.get());
        entityData.set(HAS_EXPLOSIVE_MODULE, !explosiveModuleStack.isEmpty());
    }

    public ItemStack getReturnModuleStack()
    {
        return returnModuleStack;
    }

    public void setReturnModuleStack(ItemStack stack)
    {
        returnModuleStack = sanitizeModuleStack(stack, ExampleMod.RETURN_MODULE.get());
        entityData.set(HAS_RETURN_MODULE, !returnModuleStack.isEmpty());
    }

    public ItemStack getSpeedModuleStack()
    {
        return speedModuleStack;
    }

    public void setSpeedModuleStack(ItemStack stack)
    {
        speedModuleStack = isSpeedModule(stack) ? copySingle(stack) : ItemStack.EMPTY;
        entityData.set(HAS_SPEED_MODULE, !speedModuleStack.isEmpty());
        entityData.set(SPEED_MODULE_LEVEL, getSpeedModuleLevel(speedModuleStack));
    }

    public ItemStack getThermalCameraModuleStack()
    {
        return thermalCameraModuleStack;
    }

    public void setThermalCameraModuleStack(ItemStack stack)
    {
        thermalCameraModuleStack = sanitizeModuleStack(stack, ExampleMod.THERMAL_CAMERA_MODULE.get());
        entityData.set(HAS_THERMAL_CAMERA_MODULE, !thermalCameraModuleStack.isEmpty());
        if (thermalCameraModuleStack.isEmpty()) {
            setThermalCameraEnabled(false);
        }
    }

    private ItemStack sanitizeModuleStack(ItemStack stack, net.minecraft.world.item.Item expectedItem)
    {
        if (stack.isEmpty() || !stack.is(expectedItem)) {
            return ItemStack.EMPTY;
        }

        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static ItemStack copySingle(ItemStack stack)
    {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    public static boolean isRangeModule(ItemStack stack)
    {
        return isBasicRangeModule(stack)
                || stack.is(ExampleMod.ADVANCED_RANGE_MODULE.get())
                || stack.is(ExampleMod.ELITE_RANGE_MODULE.get())
                || stack.is(ExampleMod.INFINITE_RANGE_MODULE.get());
    }

    public static boolean isSpeedModule(ItemStack stack)
    {
        return stack.is(ExampleMod.SPEED_MODULE.get())
                || stack.is(ExampleMod.ADVANCED_SPEED_MODULE.get())
                || stack.is(ExampleMod.ELITE_SPEED_MODULE.get());
    }

    public static boolean isBasicRangeModule(ItemStack stack)
    {
        return stack.is(ExampleMod.RANGE_MODULE.get()) || stack.is(ExampleMod.BASIC_RANGE_MODULE.get());
    }

    public ItemStack createDroneCoreStack()
    {
        ItemStack stack = new ItemStack(ExampleMod.DRONE_CORE.get());
        saveModulesToCoreStack(stack);
        return stack;
    }

    public void saveModulesToCoreStack(ItemStack stack)
    {
        CompoundTag modulesTag = new CompoundTag();
        putModule(modulesTag, RANGE_MODULE_TAG, rangeModuleStack);
        putModule(modulesTag, CAMERA_MODULE_TAG, cameraModuleStack);
        putModule(modulesTag, UTILITY_MODULE_TAG, utilityModuleStack);
        putModule(modulesTag, EXPLOSIVE_MODULE_TAG, explosiveModuleStack);
        putModule(modulesTag, RETURN_MODULE_TAG, returnModuleStack);
        putModule(modulesTag, SPEED_MODULE_TAG, speedModuleStack);
        putModule(modulesTag, THERMAL_CAMERA_MODULE_TAG, thermalCameraModuleStack);
        if (!modulesTag.isEmpty()) {
            stack.getOrCreateTag().put(DRONE_MODULES_TAG, modulesTag);
        }
    }

    public void loadModulesFromCoreStack(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(DRONE_MODULES_TAG)) {
            return;
        }

        CompoundTag modulesTag = tag.getCompound(DRONE_MODULES_TAG);
        setRangeModuleStack(modulesTag.contains(RANGE_MODULE_TAG) ? ItemStack.of(modulesTag.getCompound(RANGE_MODULE_TAG)) : ItemStack.EMPTY);
        setCameraModuleStack(modulesTag.contains(CAMERA_MODULE_TAG) ? ItemStack.of(modulesTag.getCompound(CAMERA_MODULE_TAG)) : ItemStack.EMPTY);
        setUtilityModuleStack(modulesTag.contains(UTILITY_MODULE_TAG) ? ItemStack.of(modulesTag.getCompound(UTILITY_MODULE_TAG)) : ItemStack.EMPTY);
        setExplosiveModuleStack(modulesTag.contains(EXPLOSIVE_MODULE_TAG) ? ItemStack.of(modulesTag.getCompound(EXPLOSIVE_MODULE_TAG)) : ItemStack.EMPTY);
        setReturnModuleStack(modulesTag.contains(RETURN_MODULE_TAG) ? ItemStack.of(modulesTag.getCompound(RETURN_MODULE_TAG)) : ItemStack.EMPTY);
        setSpeedModuleStack(modulesTag.contains(SPEED_MODULE_TAG) ? ItemStack.of(modulesTag.getCompound(SPEED_MODULE_TAG)) : ItemStack.EMPTY);
        setThermalCameraModuleStack(modulesTag.contains(THERMAL_CAMERA_MODULE_TAG) ? ItemStack.of(modulesTag.getCompound(THERMAL_CAMERA_MODULE_TAG)) : ItemStack.EMPTY);
    }

    private static void putModule(CompoundTag tag, String key, ItemStack stack)
    {
        if (!stack.isEmpty()) {
            tag.put(key, stack.save(new CompoundTag()));
        }
    }

    public void applyPilotInput(boolean forward, boolean backward, boolean left, boolean right, boolean up, boolean down, float yaw, float pitch)
    {
        if (level().isClientSide || !remoteControlled) {
            return;
        }

        targetYaw = yaw;
        targetPitch = Mth.clamp(pitch, -89.0F, 89.0F);
        pilotForward = forward;
        pilotBackward = backward;
        pilotLeft = left;
        pilotRight = right;
        pilotUp = up;
        pilotDown = down;
        pilotInputTicks = 3;
    }

    public void recoverToController()
    {
        if (level().isClientSide) {
            return;
        }

        ServerPlayer controller = getController();
        if (controller == null || !hasReturnModule()) {
            return;
        }

        ItemStack coreStack = createDroneCoreStack();
        controller.displayClientMessage(Component.literal("Dron recuperado."), true);
        controller.removeEffect(MobEffects.NIGHT_VISION);
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> controller), new DroneCameraPacket(getId(), false));
        clearController();
        if (!controller.getInventory().add(coreStack)) {
            controller.drop(coreStack, false);
        }
        discard();
    }

    public void explodeFromController()
    {
        if (level().isClientSide) {
            return;
        }

        ServerPlayer controller = getController();
        if (controller == null || !hasExplosiveModule()) {
            return;
        }

        controller.removeEffect(MobEffects.NIGHT_VISION);
        ModNetworking.CHANNEL.send(PacketDistributor.PLAYER.with(() -> controller), new DroneCameraPacket(getId(), false));
        clearController();
        level().explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.TNT);
        discard();
    }

    private void updateControlledRotation()
    {
        float oldYaw = getYRot();
        float oldPitch = getXRot();
        float newYaw = Mth.wrapDegrees(targetYaw);
        float newPitch = Mth.clamp(targetPitch, -89.0F, 89.0F);
        setYRot(newYaw);
        setXRot(newPitch);
        yRotO = oldYaw;
        xRotO = oldPitch;
        setYHeadRot(getYRot());
        setYBodyRot(getYRot());
    }

    private void applyControlledMovement()
    {
        double yawRadians = Math.toRadians(getYRot());
        Vec3 forwardVector = new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
        Vec3 rightVector = new Vec3(-Math.cos(yawRadians), 0.0D, -Math.sin(yawRadians));
        Vec3 movement = Vec3.ZERO;

        if (pilotInputTicks > 0 && pilotForward) {
            movement = movement.add(forwardVector);
        }
        if (pilotInputTicks > 0 && pilotBackward) {
            movement = movement.subtract(forwardVector);
        }
        if (pilotInputTicks > 0 && pilotRight) {
            movement = movement.add(rightVector);
        }
        if (pilotInputTicks > 0 && pilotLeft) {
            movement = movement.subtract(rightVector);
        }
        if (movement.lengthSqr() > 1.0D) {
            movement = movement.normalize();
        }

        double vertical = 0.0D;
        if (pilotInputTicks > 0 && pilotUp) {
            vertical += 1.0D;
        }
        if (pilotInputTicks > 0 && pilotDown) {
            vertical -= 1.0D;
        }

        double speedMultiplier = getSpeedMultiplier();
        Vec3 targetVelocity = new Vec3(
                movement.x * MAX_HORIZONTAL_SPEED * speedMultiplier,
                vertical * MAX_VERTICAL_SPEED * speedMultiplier,
                movement.z * MAX_HORIZONTAL_SPEED * speedMultiplier);
        if (targetVelocity.lengthSqr() > 0.0D) {
            droneVelocity = droneVelocity.scale(1.0D - ACCELERATION_FACTOR).add(targetVelocity.scale(ACCELERATION_FACTOR));
        } else {
            droneVelocity = droneVelocity.scale(FRICTION_FACTOR);
        }

        if (droneVelocity.lengthSqr() < 1.0E-5D) {
            droneVelocity = Vec3.ZERO;
        }

        if (pilotInputTicks > 0) {
            pilotInputTicks--;
        }

        Vec3 appliedVelocity = moveWithCollision(droneVelocity);
        droneVelocity = appliedVelocity;
        setDeltaMovement(appliedVelocity);
    }

    private Vec3 moveWithCollision(Vec3 movement)
    {
        Vec3 appliedMovement = Vec3.ZERO;
        appliedMovement = appliedMovement.add(tryMove(new Vec3(movement.x, 0.0D, 0.0D)));
        appliedMovement = appliedMovement.add(tryMove(new Vec3(0.0D, 0.0D, movement.z)));
        appliedMovement = appliedMovement.add(tryMove(new Vec3(0.0D, movement.y, 0.0D)));
        return appliedMovement;
    }

    private Vec3 tryMove(Vec3 movement)
    {
        if (movement.lengthSqr() <= 1.0E-7D) {
            return Vec3.ZERO;
        }

        AABB movedBox = getBoundingBox().move(movement);
        if (level().noCollision(this, movedBox)) {
            move(MoverType.SELF, movement);
            return movement;
        }

        return Vec3.ZERO;
    }

    private void startLanding()
    {
        landingMode = true;
        syncControlVisualState();
        landingTicks = 0;
    }

    private void tickLanding()
    {
        setDeltaMovement(Vec3.ZERO);

        if (isSupportedBelow() || isUnsafeToDescend()) {
            landingMode = false;
            syncControlVisualState();
            return;
        }

        landingTicks++;
        if (landingTicks > MAX_LANDING_TICKS) {
            landingMode = false;
            syncControlVisualState();
            return;
        }

        Vec3 descent = new Vec3(0.0D, -LANDING_SPEED, 0.0D);
        AABB movedBox = getBoundingBox().move(descent);
        if (level().noCollision(this, movedBox)) {
            move(MoverType.SELF, descent);
        } else {
            while (descent.y < -0.001D && !level().noCollision(this, getBoundingBox().move(descent))) {
                descent = descent.scale(0.5D);
            }

            if (descent.y < -0.001D) {
                move(MoverType.SELF, descent);
            }
            landingMode = false;
            syncControlVisualState();
        }
    }

    private void syncControlVisualState()
    {
        entityData.set(REMOTE_CONTROLLED, remoteControlled);
        entityData.set(LANDING_MODE, landingMode);
    }

    private void tickRotorAnimation()
    {
        rotorSpeedO = rotorSpeed;
        rotorAngleO = rotorAngle;

        float targetRotorSpeed = isRemoteControlled()
                ? MAX_ROTOR_SPEED
                : isLandingMode() ? LANDING_ROTOR_SPEED : 0.0F;
        rotorSpeed += (targetRotorSpeed - rotorSpeed) * ROTOR_SPEED_LERP;
        if (targetRotorSpeed == 0.0F && rotorSpeed < ROTOR_STOP_EPSILON) {
            rotorSpeed = 0.0F;
        }
        if (rotorSpeed > 0.0F) {
            rotorAngle += rotorSpeed;
            if (rotorAngle > ROTOR_FULL_TURN) {
                rotorAngle -= ROTOR_FULL_TURN;
                rotorAngleO -= ROTOR_FULL_TURN;
            }
        }
    }

    private boolean isSupportedBelow()
    {
        return !level().noCollision(this, getBoundingBox().move(0.0D, -0.05D, 0.0D));
    }

    private boolean isUnsafeToDescend()
    {
        if (getY() <= level().getMinBuildHeight() + 1.0D) {
            return true;
        }

        BlockState blockBelow = level().getBlockState(blockPosition().below());
        return !blockBelow.getFluidState().isEmpty();
    }

    private void keepControllerStill(ServerPlayer controller)
    {
        if (controllerAnchorPosition == null) {
            controllerAnchorPosition = controller.position();
            controllerAnchorYaw = controller.getYRot();
            controllerAnchorPitch = controller.getXRot();
        }

        controller.setDeltaMovement(Vec3.ZERO);
        controller.setShiftKeyDown(false);
        controller.moveTo(controllerAnchorPosition.x, controllerAnchorPosition.y, controllerAnchorPosition.z, controllerAnchorYaw, controllerAnchorPitch);
        controller.setYHeadRot(controllerAnchorYaw);
        controller.setYBodyRot(controllerAnchorYaw);
    }

    private void applyLightEffect(ServerPlayer controller)
    {
        if (isLightEnabled() && hasLightModule()) {
            controller.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false, false));
        } else {
            controller.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    private void applyThermalCameraEffect(ServerPlayer controller)
    {
        if (!isThermalCameraEnabled() || !hasThermalCameraModule()) {
            setThermalCameraEnabled(false);
            return;
        }
        if (tickCount % THERMAL_CAMERA_REFRESH_TICKS != 0) {
            return;
        }

        AABB thermalArea = getBoundingBox().inflate(THERMAL_CAMERA_RANGE);
        List<LivingEntity> targets = level().getEntitiesOfClass(
                LivingEntity.class,
                thermalArea,
                entity -> entity.isAlive() && entity != controller);
        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, THERMAL_CAMERA_EFFECT_DURATION, 0, true, false));
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount)
    {
        if (level().isClientSide || isRemoved() || isInvulnerableTo(damageSource)) {
            return false;
        }

        Entity attacker = damageSource.getEntity();
        boolean creativePlayer = attacker instanceof Player player && player.getAbilities().instabuild;

        if (creativePlayer) {
            destroy(false);
            return true;
        }

        health -= amount;
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.METAL_HIT, SoundSource.NEUTRAL, 0.8F, 1.0F);

        if (health <= 0.0F) {
            destroy(true);
        }

        return true;
    }

    private void destroy(boolean dropCore)
    {
        if (remoteControlled) {
            stopControlling(DRONE_MODE_DEACTIVATED);
        }

        level().playSound(null, getX(), getY(), getZ(), SoundEvents.METAL_BREAK, SoundSource.NEUTRAL, 0.9F, 1.0F);

        if (dropCore) {
            spawnAtLocation(createDroneCoreStack());
        }

        discard();
    }

    private ServerPlayer getController()
    {
        if (!(level() instanceof ServerLevel serverLevel) || controllingPlayerUUID == null) {
            return null;
        }

        return serverLevel.getServer().getPlayerList().getPlayer(controllingPlayerUUID);
    }

    public static ScoutDroneEntity getControlledDrone(ServerPlayer player)
    {
        List<ScoutDroneEntity> drones = player.serverLevel().getEntitiesOfClass(
                ScoutDroneEntity.class,
                player.getBoundingBox().inflate(INFINITE_RANGE_MODULE_CONTROL_RANGE + 8.0D),
                drone -> drone.isControlledBy(player));
        return drones.isEmpty() ? null : drones.get(0);
    }

    private double getMaxControlRangeSqr()
    {
        double range = getMaxControlRange();
        return range * range;
    }

    private void openModuleMenu(ServerPlayer player)
    {
        net.minecraftforge.network.NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName()
            {
                return Component.literal("M\u00f3dulos del dron");
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory, Player player)
            {
                return new ScoutDroneModuleMenu(containerId, inventory, ScoutDroneEntity.this);
            }
        }, (FriendlyByteBuf buffer) -> buffer.writeInt(getId()));
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
