package com.example.examplemod.item;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.ScoutDroneEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class DroneCoreItem extends Item
{
    private static final Component NOT_ENOUGH_SPACE = Component.literal("No hay espacio suficiente para desplegar el dron.");
    private static final Component FLAT_SURFACE_REQUIRED = Component.literal("El dron debe colocarse sobre una superficie plana.");

    public DroneCoreItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (context.getClickedFace() != Direction.UP) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (!level.isClientSide && player != null) {
                player.displayClientMessage(FLAT_SURFACE_REQUIRED, true);
            }
            return InteractionResult.FAIL;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos spawnBlockPos = context.getClickedPos().above();
        double spawnX = spawnBlockPos.getX() + 0.5D;
        double spawnY = spawnBlockPos.getY();
        double spawnZ = spawnBlockPos.getZ() + 0.5D;

        ScoutDroneEntity scoutDrone = ExampleMod.SCOUT_DRONE.get().create(level);
        if (scoutDrone == null) {
            return InteractionResult.FAIL;
        }

        float yaw = player != null ? player.getYRot() : 0.0F;
        scoutDrone.moveTo(spawnX, spawnY, spawnZ, yaw, 0.0F);
        scoutDrone.loadModulesFromCoreStack(stack);

        AABB boundingBox = scoutDrone.getBoundingBox();
        if (!level.noCollision(scoutDrone, boundingBox) || collidesWithEntities(level, scoutDrone, boundingBox)) {
            if (player != null) {
                player.displayClientMessage(NOT_ENOUGH_SPACE, true);
            }
            return InteractionResult.FAIL;
        }

        level.addFreshEntity(scoutDrone);

        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        SoundSource soundSource = player != null ? player.getSoundSource() : SoundSource.PLAYERS;
        level.playSound(null, spawnX, spawnY, spawnZ, SoundType.METAL.getPlaceSound(), soundSource, 1.0F, 1.0F);

        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("tooltip.dronecraft.drone").withStyle(ChatFormatting.GRAY));

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ScoutDroneEntity.DRONE_MODULES_TAG)) {
            return;
        }

        CompoundTag modulesTag = tag.getCompound(ScoutDroneEntity.DRONE_MODULES_TAG);
        boolean hasAnyModule = false;
        if (modulesTag.contains(ScoutDroneEntity.RANGE_MODULE_TAG)
                || modulesTag.contains(ScoutDroneEntity.CAMERA_MODULE_TAG)
                || modulesTag.contains(ScoutDroneEntity.UTILITY_MODULE_TAG)
                || modulesTag.contains(ScoutDroneEntity.EXPLOSIVE_MODULE_TAG)
                || modulesTag.contains(ScoutDroneEntity.RETURN_MODULE_TAG)
                || modulesTag.contains(ScoutDroneEntity.SPEED_MODULE_TAG)
                || modulesTag.contains(ScoutDroneEntity.THERMAL_CAMERA_MODULE_TAG)) {
            hasAnyModule = true;
        }

        if (!hasAnyModule) {
            return;
        }

        tooltip.add(Component.translatable("tooltip.dronecraft.installed_modules").withStyle(ChatFormatting.GRAY));
        addRangeTooltip(modulesTag, tooltip);
        if (modulesTag.contains(ScoutDroneEntity.CAMERA_MODULE_TAG)) {
            tooltip.add(Component.translatable("tooltip.dronecraft.module.zoom").withStyle(ChatFormatting.GRAY));
        }
        if (modulesTag.contains(ScoutDroneEntity.UTILITY_MODULE_TAG)) {
            tooltip.add(Component.translatable("tooltip.dronecraft.module.light").withStyle(ChatFormatting.GRAY));
        }
        if (modulesTag.contains(ScoutDroneEntity.EXPLOSIVE_MODULE_TAG)) {
            tooltip.add(Component.translatable("tooltip.dronecraft.module.explosive").withStyle(ChatFormatting.GRAY));
        }
        if (modulesTag.contains(ScoutDroneEntity.RETURN_MODULE_TAG)) {
            tooltip.add(Component.translatable("tooltip.dronecraft.module.return").withStyle(ChatFormatting.GRAY));
        }
        if (modulesTag.contains(ScoutDroneEntity.SPEED_MODULE_TAG)) {
            tooltip.add(Component.translatable("tooltip.dronecraft.module.speed").withStyle(ChatFormatting.GRAY));
        }
        if (modulesTag.contains(ScoutDroneEntity.THERMAL_CAMERA_MODULE_TAG)) {
            tooltip.add(Component.translatable("tooltip.dronecraft.module.thermal").withStyle(ChatFormatting.GRAY));
        }
    }

    private static void addRangeTooltip(CompoundTag modulesTag, List<Component> tooltip)
    {
        if (!modulesTag.contains(ScoutDroneEntity.RANGE_MODULE_TAG)) {
            return;
        }

        ItemStack rangeStack = ItemStack.of(modulesTag.getCompound(ScoutDroneEntity.RANGE_MODULE_TAG));
        String rangeNameKey = "tooltip.dronecraft.range.basic";
        if (rangeStack.is(ExampleMod.ADVANCED_RANGE_MODULE.get())) {
            rangeNameKey = "tooltip.dronecraft.range.advanced";
        } else if (rangeStack.is(ExampleMod.ELITE_RANGE_MODULE.get())) {
            rangeNameKey = "tooltip.dronecraft.range.elite";
        } else if (rangeStack.is(ExampleMod.INFINITE_RANGE_MODULE.get())) {
            rangeNameKey = "tooltip.dronecraft.range.infinite";
        }
        tooltip.add(Component.translatable("tooltip.dronecraft.module.range", Component.translatable(rangeNameKey)).withStyle(ChatFormatting.GRAY));
    }

    private static boolean collidesWithEntities(Level level, ScoutDroneEntity scoutDrone, AABB boundingBox)
    {
        return !level.getEntities(scoutDrone, boundingBox, entity -> !entity.isSpectator() && entity.isAlive()).isEmpty();
    }
}
