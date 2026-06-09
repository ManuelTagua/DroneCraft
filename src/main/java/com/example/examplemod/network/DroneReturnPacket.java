package com.example.examplemod.network;

import com.example.examplemod.entity.ScoutDroneEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class DroneReturnPacket
{
    private static final Component MISSING_RETURN_MODULE = Component.literal("El dron no tiene módulo de retorno.");

    public static void encode(DroneReturnPacket packet, FriendlyByteBuf buffer)
    {
    }

    public static DroneReturnPacket decode(FriendlyByteBuf buffer)
    {
        return new DroneReturnPacket();
    }

    public static void handle(DroneReturnPacket packet, Supplier<NetworkEvent.Context> contextSupplier)
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
            if (!drone.hasReturnModule()) {
                player.displayClientMessage(MISSING_RETURN_MODULE, true);
                return;
            }

            drone.recoverToController();
        });
        context.setPacketHandled(true);
    }
}
