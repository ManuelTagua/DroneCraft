package com.example.examplemod;

import com.example.examplemod.entity.ScoutDroneEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class DronePilotProtectionEvents
{
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        cancelIfPiloting(event, event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        cancelIfPiloting(event, event.getEntity());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event)
    {
        cancelIfPiloting(event, event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event)
    {
        cancelIfPiloting(event, event.getEntity());
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event)
    {
        cancelIfPiloting(event, event.getEntity());
    }

    private static void cancelIfPiloting(net.minecraftforge.eventbus.api.Event event, Player player)
    {
        if (player instanceof ServerPlayer serverPlayer && ScoutDroneEntity.getControlledDrone(serverPlayer) != null) {
            event.setCanceled(true);
        }
    }
}
