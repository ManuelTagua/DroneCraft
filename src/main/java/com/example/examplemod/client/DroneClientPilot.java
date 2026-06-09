package com.example.examplemod.client;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.ScoutDroneEntity;
import com.example.examplemod.network.DroneExplodePacket;
import com.example.examplemod.network.DroneExitPilotPacket;
import com.example.examplemod.network.DroneInputPacket;
import com.example.examplemod.network.DroneLightTogglePacket;
import com.example.examplemod.network.DroneReturnPacket;
import com.example.examplemod.network.DroneThermalTogglePacket;
import com.example.examplemod.network.ModNetworking;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DroneClientPilot
{
    private static final KeyMapping EXIT_PILOT_KEY = new KeyMapping(
            "key.dronecraft.exit_pilot",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.dronecraft");
    private static final KeyMapping CYCLE_ZOOM_KEY = new KeyMapping(
            "key.dronecraft.cycle_zoom",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Z,
            "key.categories.dronecraft");
    private static final KeyMapping TOGGLE_LIGHT_KEY = new KeyMapping(
            "key.dronecraft.toggle_light",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            "key.categories.dronecraft");
    private static final KeyMapping SELF_DESTRUCT_KEY = new KeyMapping(
            "key.dronecraft.self_destruct",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.dronecraft");
    private static final KeyMapping RETURN_DRONE_KEY = new KeyMapping(
            "key.dronecraft.return_drone",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.dronecraft");
    private static final KeyMapping TOGGLE_THERMAL_KEY = new KeyMapping(
            "key.dronecraft.toggle_thermal",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.dronecraft");

    private static final int[] ZOOM_LEVELS = {1, 2, 4};
    private static final int SELF_DESTRUCT_HOLD_TICKS = 100;
    private static final double CAMERA_HEIGHT_OFFSET = 0.45D;
    private static final double CAMERA_FORWARD_OFFSET = 1.15D;
    private static final double CAMERA_COLLISION_MARGIN = 0.08D;
    private static final Component MISSING_EXPLOSIVE_MODULE = Component.literal("El dron no tiene módulo explosivo.");
    private static final Component MISSING_RETURN_MODULE = Component.literal("El dron no tiene módulo de retorno.");

    private static int pilotedDroneEntityId = -1;
    private static ScoutDroneEntity pilotedDrone;
    private static DroneCameraEntity droneCameraEntity;
    private static Vec3 playerAnchorPosition;
    private static float playerAnchorYaw;
    private static float playerAnchorPitch;
    private static float droneYaw;
    private static float dronePitch;
    private static int savedSelectedSlot = -1;
    private static long lastInputPacketGameTime = -1L;
    private static int zoomLevelIndex;
    private static boolean lightEnabled;
    private static boolean thermalEnabled;
    private static int selfDestructTicks;
    private static boolean selfDestructTriggered;
    private static CameraType savedCameraType;

    public static void registerKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.register(EXIT_PILOT_KEY);
        event.register(CYCLE_ZOOM_KEY);
        event.register(TOGGLE_LIGHT_KEY);
        event.register(SELF_DESTRUCT_KEY);
        event.register(RETURN_DRONE_KEY);
        event.register(TOGGLE_THERMAL_KEY);
    }

    public static void startPiloting(int droneEntityId)
    {
        clearDroneKeyClicks();
        resetPilotModuleState();
        pilotedDroneEntityId = droneEntityId;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            savedCameraType = minecraft.options.getCameraType();
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            playerAnchorPosition = minecraft.player.position();
            playerAnchorYaw = minecraft.player.getYRot();
            playerAnchorPitch = minecraft.player.getXRot();
            savedSelectedSlot = minecraft.player.getInventory().selected;
            lastInputPacketGameTime = -1L;
        }

        ScoutDroneEntity drone = getPilotedDrone(minecraft);
        if (drone != null) {
            pilotedDrone = drone;
            droneYaw = drone.getYRot();
            dronePitch = Mth.clamp(drone.getXRot(), -89.0F, 89.0F);
            createOrResetCamera(minecraft, drone);
        } else if (minecraft.player != null) {
            droneYaw = playerAnchorYaw;
            dronePitch = playerAnchorPitch;
        }
    }

    public static void stopPiloting()
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (savedCameraType != null) {
            minecraft.options.setCameraType(savedCameraType);
        }
        if (minecraft.player != null) {
            minecraft.setCameraEntity(minecraft.player);
            restorePlayerState(minecraft);
            minecraft.player.removeEffect(MobEffects.NIGHT_VISION);
            if (savedSelectedSlot >= 0 && savedSelectedSlot < Inventory.getSelectionSize()) {
                minecraft.player.getInventory().selected = savedSelectedSlot;
            }
        }
        if (droneCameraEntity != null) {
            droneCameraEntity.discard();
        }
        pilotedDroneEntityId = -1;
        pilotedDrone = null;
        droneCameraEntity = null;
        playerAnchorPosition = null;
        savedSelectedSlot = -1;
        lastInputPacketGameTime = -1L;
        savedCameraType = null;
        resetPilotModuleState();
        clearDroneKeyClicks();
    }

    public static boolean isPiloting()
    {
        return pilotedDroneEntityId != -1;
    }

    public static boolean isPilotingDrone(ScoutDroneEntity drone)
    {
        return drone != null && pilotedDroneEntityId == drone.getId();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (pilotedDroneEntityId == -1) {
            resetPilotModuleState();
            clearDroneKeyClicks();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            stopPiloting();
            return;
        }

        ScoutDroneEntity drone = getPilotedDrone(minecraft);
        if (drone == null || !drone.isAlive()) {
            ModNetworking.CHANNEL.sendToServer(new DroneExitPilotPacket());
            stopPiloting();
            return;
        }
        pilotedDrone = drone;
        forceFirstPersonCamera(minecraft);

        if (droneCameraEntity == null || droneCameraEntity.level() != minecraft.level) {
            createOrResetCamera(minecraft, drone);
        } else if (minecraft.getCameraEntity() != droneCameraEntity) {
            minecraft.setCameraEntity(droneCameraEntity);
        }

        while (EXIT_PILOT_KEY.consumeClick()) {
            ModNetworking.CHANNEL.sendToServer(new DroneExitPilotPacket());
            stopPiloting();
            return;
        }

        handleModuleKeys(drone);
        if (handleReturnKey(minecraft, drone)) {
            return;
        }
        if (handleSelfDestructKey(minecraft, drone)) {
            return;
        }

        boolean forward = isKeyDown(minecraft, GLFW.GLFW_KEY_W);
        boolean backward = isKeyDown(minecraft, GLFW.GLFW_KEY_S);
        boolean left = isKeyDown(minecraft, GLFW.GLFW_KEY_A);
        boolean right = isKeyDown(minecraft, GLFW.GLFW_KEY_D);
        boolean up = isKeyDown(minecraft, GLFW.GLFW_KEY_SPACE);
        boolean down = isKeyDown(minecraft, GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyDown(minecraft, GLFW.GLFW_KEY_RIGHT_SHIFT);

        updateLocalRotationFromMouse(minecraft);
        updateCameraEntity(drone);

        restorePlayerState(minecraft);
        restoreSelectedSlot(minecraft);
        suppressPlayerMovementKeys(minecraft);
        consumeBlockedKeyMappings(minecraft);

        if (minecraft.screen != null) {
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        if (lastInputPacketGameTime == gameTime) {
            return;
        }
        lastInputPacketGameTime = gameTime;

        ModNetworking.CHANNEL.sendToServer(new DroneInputPacket(
                forward,
                backward,
                left,
                right,
                up,
                down,
                droneYaw,
                dronePitch));
    }

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event)
    {
        if (pilotedDroneEntityId == -1) {
            return;
        }

        event.getInput().leftImpulse = 0.0F;
        event.getInput().forwardImpulse = 0.0F;
        event.getInput().up = false;
        event.getInput().down = false;
        event.getInput().left = false;
        event.getInput().right = false;
        event.getInput().jumping = false;
        event.getInput().shiftKeyDown = false;
        event.getEntity().setShiftKeyDown(false);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event)
    {
        if (!isPiloting()) {
            return;
        }

        if (event.getOverlay() == VanillaGuiOverlay.HOTBAR.type()
                || event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()
                || event.getOverlay() == VanillaGuiOverlay.PLAYER_HEALTH.type()
                || event.getOverlay() == VanillaGuiOverlay.ARMOR_LEVEL.type()
                || event.getOverlay() == VanillaGuiOverlay.FOOD_LEVEL.type()
                || event.getOverlay() == VanillaGuiOverlay.EXPERIENCE_BAR.type()
                || event.getOverlay() == VanillaGuiOverlay.AIR_LEVEL.type()
                || event.getOverlay() == VanillaGuiOverlay.HELMET.type()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event)
    {
        if (!isPiloting()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ScoutDroneEntity drone = getPilotedDrone(minecraft);
        if (minecraft.player == null || drone == null) {
            return;
        }

        int distance = Mth.floor(Math.sqrt(minecraft.player.distanceToSqr(drone)) + 0.5D);
        boolean infiniteRange = drone.hasInfiniteRangeModule();
        int maxRange = Mth.floor(drone.getMaxControlRange());
        String rangeText = infiniteRange
                ? "Rango: " + distance + " / \u221e"
                : "Rango: " + distance + " / " + maxRange + " m";
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int x = 8;
        int y = 8;
        int lineY = y;

        guiGraphics.drawString(minecraft.font, "DRONE MODE", x, lineY, 0x55FF55, true);
        lineY += 12;
        guiGraphics.drawString(minecraft.font, rangeText, x, lineY, 0xFFFFFF, true);
        lineY += 12;
        guiGraphics.drawString(minecraft.font, "Zoom: " + getZoomMultiplier() + "x", x, lineY, 0xFFFFFF, true);
        lineY += 12;
        guiGraphics.drawString(minecraft.font, "Luz: " + (lightEnabled ? "ON" : "OFF"), x, lineY, lightEnabled ? 0xFFFF88 : 0xAAAAAA, true);
        lineY += 12;
        if (drone.hasLightModule()) {
            guiGraphics.drawString(minecraft.font, "N: vision nocturna", x, lineY, 0xAAAAAA, true);
            lineY += 12;
        }
        if (drone.hasReturnModule()) {
            guiGraphics.drawString(minecraft.font, "R: recuperar dron", x, lineY, 0xAAAAAA, true);
            lineY += 12;
        }
        if (drone.hasExplosiveModule()) {
            guiGraphics.drawString(minecraft.font, "Manten B: autodestruccion", x, lineY, 0xFFAAAA, true);
            lineY += 12;
        }
        if (drone.hasSpeedModule()) {
            guiGraphics.drawString(minecraft.font, "Velocidad: x" + formatSpeedMultiplier(drone.getSpeedMultiplier()), x, lineY, 0xFFFFFF, true);
            lineY += 12;
        }
        if (drone.hasThermalCameraModule()) {
            guiGraphics.drawString(minecraft.font, "C: termica", x, lineY, 0xAAAAAA, true);
            lineY += 12;
            guiGraphics.drawString(minecraft.font, "Termica: " + (thermalEnabled ? "ON" : "OFF"), x, lineY, thermalEnabled ? 0xFF8844 : 0xAAAAAA, true);
            lineY += 12;
        }
        guiGraphics.drawString(minecraft.font, "Pulsa V para salir", x, lineY, 0xAAAAAA, true);
        lineY += 12;
        if (selfDestructTicks > 0) {
            int seconds = Mth.clamp((selfDestructTicks + 19) / 20, 1, 5);
            guiGraphics.drawString(minecraft.font, "Autodestruccion: " + seconds + "/5", x, lineY, 0xFF5555, true);
            lineY += 12;
        }
        if (!infiniteRange && distance >= maxRange - 12) {
            guiGraphics.drawString(minecraft.font, "ADVERTENCIA: senal debil", x, lineY, 0xFF5555, true);
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event)
    {
        if (isPiloting() && getZoomMultiplier() > 1) {
            event.setFOV(event.getFOV() / getZoomMultiplier());
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event)
    {
        if (isPiloting()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event)
    {
        if (isPiloting() && event.getScreen() instanceof InventoryScreen) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onInteractionKeyMapping(InputEvent.InteractionKeyMappingTriggered event)
    {
        if (isPiloting()) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseScrolling(InputEvent.MouseScrollingEvent event)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (isPiloting() && minecraft.player != null) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            }
            restoreSelectedSlot(minecraft);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event)
    {
        if (!isPiloting() || event.getAction() == GLFW.GLFW_RELEASE) {
            return;
        }

        int key = event.getKey();
        if ((key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9)
                || key == GLFW.GLFW_KEY_Q
                || key == GLFW.GLFW_KEY_E
                || key == GLFW.GLFW_KEY_F) {
            Minecraft minecraft = Minecraft.getInstance();
            suppressPlayerMovementKeys(minecraft);
            consumeBlockedKeyMappings(minecraft);
            restoreSelectedSlot(minecraft);
        }
    }

    private static void handleModuleKeys(ScoutDroneEntity drone)
    {
        boolean hasZoomModule = drone.hasZoomModule();
        boolean hasLightModule = drone.hasLightModule();
        boolean hasThermalCameraModule = drone.hasThermalCameraModule();

        if (!hasZoomModule) {
            zoomLevelIndex = 0;
        }
        if (!hasLightModule && lightEnabled) {
            lightEnabled = false;
            ModNetworking.CHANNEL.sendToServer(new DroneLightTogglePacket(false));
        }
        if (!hasThermalCameraModule && thermalEnabled) {
            thermalEnabled = false;
            ModNetworking.CHANNEL.sendToServer(new DroneThermalTogglePacket(false));
        }

        while (CYCLE_ZOOM_KEY.consumeClick()) {
            if (hasZoomModule) {
                zoomLevelIndex = (zoomLevelIndex + 1) % ZOOM_LEVELS.length;
            }
        }

        while (TOGGLE_LIGHT_KEY.consumeClick()) {
            if (hasLightModule) {
                lightEnabled = !lightEnabled;
                ModNetworking.CHANNEL.sendToServer(new DroneLightTogglePacket(lightEnabled));
            }
        }

        while (TOGGLE_THERMAL_KEY.consumeClick()) {
            if (hasThermalCameraModule) {
                thermalEnabled = !thermalEnabled;
                ModNetworking.CHANNEL.sendToServer(new DroneThermalTogglePacket(thermalEnabled));
            }
        }
    }

    private static boolean handleReturnKey(Minecraft minecraft, ScoutDroneEntity drone)
    {
        while (RETURN_DRONE_KEY.consumeClick()) {
            if (!drone.hasReturnModule()) {
                if (minecraft.player != null) {
                    minecraft.player.displayClientMessage(MISSING_RETURN_MODULE, true);
                }
                continue;
            }

            ModNetworking.CHANNEL.sendToServer(new DroneReturnPacket());
            stopPiloting();
            return true;
        }
        return false;
    }

    private static boolean handleSelfDestructKey(Minecraft minecraft, ScoutDroneEntity drone)
    {
        while (SELF_DESTRUCT_KEY.consumeClick()) {
            if (!drone.hasExplosiveModule() && minecraft.player != null) {
                minecraft.player.displayClientMessage(MISSING_EXPLOSIVE_MODULE, true);
            }
        }

        if (!drone.hasExplosiveModule() || !isKeyDown(minecraft, GLFW.GLFW_KEY_B)) {
            selfDestructTicks = 0;
            selfDestructTriggered = false;
            return false;
        }

        if (!selfDestructTriggered) {
            selfDestructTicks++;
            if (selfDestructTicks >= SELF_DESTRUCT_HOLD_TICKS) {
                selfDestructTriggered = true;
                ModNetworking.CHANNEL.sendToServer(new DroneExplodePacket());
                stopPiloting();
                return true;
            }
        }
        return false;
    }

    private static int getZoomMultiplier()
    {
        return ZOOM_LEVELS[zoomLevelIndex];
    }

    private static String formatSpeedMultiplier(double multiplier)
    {
        if (multiplier == Math.rint(multiplier)) {
            return Integer.toString((int) multiplier);
        }
        return Double.toString(multiplier);
    }

    private static void resetPilotModuleState()
    {
        zoomLevelIndex = 0;
        lightEnabled = false;
        thermalEnabled = false;
        selfDestructTicks = 0;
        selfDestructTriggered = false;
    }

    private static void clearDroneKeyClicks()
    {
        while (EXIT_PILOT_KEY.consumeClick()) {
        }
        while (CYCLE_ZOOM_KEY.consumeClick()) {
        }
        while (TOGGLE_LIGHT_KEY.consumeClick()) {
        }
        while (SELF_DESTRUCT_KEY.consumeClick()) {
        }
        while (RETURN_DRONE_KEY.consumeClick()) {
        }
        while (TOGGLE_THERMAL_KEY.consumeClick()) {
        }
    }

    private static ScoutDroneEntity getPilotedDrone(Minecraft minecraft)
    {
        if (minecraft.level == null || pilotedDroneEntityId == -1) {
            return null;
        }

        return minecraft.level.getEntity(pilotedDroneEntityId) instanceof ScoutDroneEntity scoutDrone ? scoutDrone : null;
    }

    private static boolean isKeyDown(Minecraft minecraft, int key)
    {
        return GLFW.glfwGetKey(minecraft.getWindow().getWindow(), key) == GLFW.GLFW_PRESS;
    }

    private static void forceFirstPersonCamera(Minecraft minecraft)
    {
        minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        while (minecraft.options.keyTogglePerspective.consumeClick()) {
        }
    }

    private static void restorePlayerState(Minecraft minecraft)
    {
        if (minecraft.player == null || playerAnchorPosition == null) {
            return;
        }

        minecraft.player.setDeltaMovement(Vec3.ZERO);
        minecraft.player.setShiftKeyDown(false);
        minecraft.player.setPos(playerAnchorPosition);
        minecraft.player.setYRot(playerAnchorYaw);
        minecraft.player.setXRot(playerAnchorPitch);
        minecraft.player.yRotO = playerAnchorYaw;
        minecraft.player.xRotO = playerAnchorPitch;
        minecraft.player.yHeadRot = playerAnchorYaw;
        minecraft.player.yHeadRotO = playerAnchorYaw;
        minecraft.player.yBodyRot = playerAnchorYaw;
        minecraft.player.yBodyRotO = playerAnchorYaw;
    }

    private static void createOrResetCamera(Minecraft minecraft, ScoutDroneEntity drone)
    {
        if (minecraft.level == null) {
            return;
        }

        if (droneCameraEntity != null) {
            droneCameraEntity.discard();
        }

        droneCameraEntity = new DroneCameraEntity(minecraft.level);
        droneCameraEntity.snapToDrone(getCameraTargetPosition(drone), droneYaw, dronePitch);
        minecraft.setCameraEntity(droneCameraEntity);
    }

    private static void updateCameraEntity(ScoutDroneEntity drone)
    {
        if (droneCameraEntity != null) {
            droneCameraEntity.syncVisualPose(getCameraTargetPosition(drone), droneYaw, dronePitch);
        }
    }

    private static Vec3 getCameraTargetPosition(ScoutDroneEntity drone)
    {
        Vec3 origin = drone.position().add(0.0D, CAMERA_HEIGHT_OFFSET, 0.0D);
        double yawRadians = Math.toRadians(droneYaw);
        Vec3 forward = new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
        Vec3 target = origin.add(forward.scale(CAMERA_FORWARD_OFFSET));
        return clampCameraTargetToBlocks(drone, origin, target);
    }

    private static Vec3 clampCameraTargetToBlocks(ScoutDroneEntity drone, Vec3 origin, Vec3 target)
    {
        BlockHitResult hitResult = drone.level().clip(new ClipContext(
                origin,
                target,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                drone));
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return target;
        }

        Vec3 direction = target.subtract(origin);
        if (direction.lengthSqr() < 1.0E-6D) {
            return origin;
        }

        return hitResult.getLocation().subtract(direction.normalize().scale(CAMERA_COLLISION_MARGIN));
    }

    private static void updateLocalRotationFromMouse(Minecraft minecraft)
    {
        float yawDelta = Mth.wrapDegrees(minecraft.player.getYRot() - playerAnchorYaw);
        float pitchDelta = minecraft.player.getXRot() - playerAnchorPitch;

        if (Math.abs(yawDelta) > 0.0001F || Math.abs(pitchDelta) > 0.0001F) {
            droneYaw = Mth.wrapDegrees(droneYaw + yawDelta);
            dronePitch = Mth.clamp(dronePitch + pitchDelta, -89.0F, 89.0F);
        }
    }

    private static void suppressPlayerMovementKeys(Minecraft minecraft)
    {
        minecraft.options.keyUp.setDown(false);
        minecraft.options.keyDown.setDown(false);
        minecraft.options.keyLeft.setDown(false);
        minecraft.options.keyRight.setDown(false);
        minecraft.options.keyJump.setDown(false);
        minecraft.options.keyShift.setDown(false);
        minecraft.options.keyAttack.setDown(false);
        minecraft.options.keyUse.setDown(false);
        minecraft.options.keyPickItem.setDown(false);
        minecraft.options.keyInventory.setDown(false);
        minecraft.options.keyDrop.setDown(false);
        minecraft.options.keySwapOffhand.setDown(false);
        for (KeyMapping hotbarKey : minecraft.options.keyHotbarSlots) {
            hotbarKey.setDown(false);
        }
        restoreSelectedSlot(minecraft);
    }

    private static void restoreSelectedSlot(Minecraft minecraft)
    {
        if (minecraft.player != null && savedSelectedSlot >= 0 && savedSelectedSlot < Inventory.getSelectionSize()) {
            minecraft.player.getInventory().selected = savedSelectedSlot;
        }
    }

    private static void consumeBlockedKeyMappings(Minecraft minecraft)
    {
        if (minecraft.player == null) {
            return;
        }

        while (minecraft.options.keyDrop.consumeClick()) {
            restoreSelectedSlot(minecraft);
        }
        while (minecraft.options.keySwapOffhand.consumeClick()) {
            restoreSelectedSlot(minecraft);
        }
        while (minecraft.options.keyInventory.consumeClick()) {
            restoreSelectedSlot(minecraft);
        }
        for (KeyMapping hotbarKey : minecraft.options.keyHotbarSlots) {
            while (hotbarKey.consumeClick()) {
                restoreSelectedSlot(minecraft);
            }
        }
    }
}
