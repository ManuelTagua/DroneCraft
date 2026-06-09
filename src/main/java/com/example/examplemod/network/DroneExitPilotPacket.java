package com.example.examplemod.network;

import com.example.examplemod.entity.ScoutDroneEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class DroneExitPilotPacket
{
    private static final Component DEACTIVATED_MESSAGE = Component.literal("Modo dron desactivado.");

    public static void encode(DroneExitPilotPacket packet, FriendlyByteBuf buffer)
    {
    }

    public static DroneExitPilotPacket decode(FriendlyByteBuf buffer)
    {
        return new DroneExitPilotPacket();
    }

    public static void handle(DroneExitPilotPacket packet, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            ScoutDroneEntity drone = ScoutDroneEntity.getControlledDrone(player);
            if (drone != null) {
                drone.stopControlling(DEACTIVATED_MESSAGE);
            }
        });
        context.setPacketHandled(true);
    }
}
