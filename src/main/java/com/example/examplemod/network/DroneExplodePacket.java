package com.example.examplemod.network;

import com.example.examplemod.entity.ScoutDroneEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class DroneExplodePacket
{
    private static final Component MISSING_EXPLOSIVE_MODULE = Component.literal("El dron no tiene módulo explosivo.");

    public static void encode(DroneExplodePacket packet, FriendlyByteBuf buffer)
    {
    }

    public static DroneExplodePacket decode(FriendlyByteBuf buffer)
    {
        return new DroneExplodePacket();
    }

    public static void handle(DroneExplodePacket packet, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            ScoutDroneEntity drone = ScoutDroneEntity.getControlledDrone(player);
            if (drone == null) {
                return;
            }
            if (!drone.hasExplosiveModule()) {
                player.displayClientMessage(MISSING_EXPLOSIVE_MODULE, true);
                return;
            }

            drone.explodeFromController();
        });
        context.setPacketHandled(true);
    }
}
