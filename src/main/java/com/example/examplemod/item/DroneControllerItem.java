package com.example.examplemod.item;

import com.example.examplemod.entity.ScoutDroneEntity;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class DroneControllerItem extends Item
{
    public static final String LINKED_DRONE_UUID_TAG = "LinkedDroneUUID";

    private static final Component LINKED_MESSAGE = Component.literal("Mando vinculado al dron.");
    private static final Component RELINKED_MESSAGE = Component.literal("Mando revinculado a otro dron.");
    private static final Component NOT_LINKED_MESSAGE = Component.literal("Este mando no está vinculado a ningún dron.");
    private static final Component DEACTIVATED_MESSAGE = Component.literal("Modo dron desactivado.");
    private static final Component LINKED_DRONE_UNAVAILABLE_MESSAGE = Component.literal("El dron vinculado no está disponible.");

    public DroneControllerItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        UUID linkedDroneUuid = getLinkedDroneUuid(stack);
        if (linkedDroneUuid == null) {
            player.displayClientMessage(NOT_LINKED_MESSAGE, true);
            return InteractionResultHolder.success(stack);
        }

        Entity linkedEntity = level instanceof ServerLevel serverLevel ? serverLevel.getEntity(linkedDroneUuid) : null;
        if (!(linkedEntity instanceof ScoutDroneEntity drone) || !linkedEntity.isAlive()) {
            player.displayClientMessage(LINKED_DRONE_UNAVAILABLE_MESSAGE, true);
            return InteractionResultHolder.success(stack);
        }

        if (player instanceof ServerPlayer serverPlayer) {
            if (drone.isControlledBy(serverPlayer)) {
                drone.stopControlling(DEACTIVATED_MESSAGE);
            } else {
                drone.startControlling(serverPlayer);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag)
    {
        tooltip.add(Component.translatable("tooltip.dronecraft.drone_controller").withStyle(ChatFormatting.GRAY));

        UUID linkedDroneUuid = getLinkedDroneUuid(stack);
        if (linkedDroneUuid == null) {
            tooltip.add(Component.translatable("tooltip.dronecraft.drone_controller.unlinked").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.dronecraft.drone_controller.linked", linkedDroneUuid.toString().substring(0, 8)).withStyle(ChatFormatting.GRAY));
        }
    }

    public static InteractionResult linkToDrone(ItemStack stack, Player player, ScoutDroneEntity drone)
    {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        UUID previousDroneUuid = getLinkedDroneUuid(stack);
        boolean wasLinkedToAnotherDrone = previousDroneUuid != null && !previousDroneUuid.equals(drone.getUUID());
        stack.getOrCreateTag().putUUID(LINKED_DRONE_UUID_TAG, drone.getUUID());
        player.displayClientMessage(wasLinkedToAnotherDrone ? RELINKED_MESSAGE : LINKED_MESSAGE, true);
        return InteractionResult.CONSUME;
    }

    @Nullable
    public static UUID getLinkedDroneUuid(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.hasUUID(LINKED_DRONE_UUID_TAG)) {
            return null;
        }

        return tag.getUUID(LINKED_DRONE_UUID_TAG);
    }
}
