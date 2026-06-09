package com.example.examplemod.network;

import com.example.examplemod.entity.ScoutDroneEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class DroneThermalTogglePacket
{
    private final boolean enabled;

    public DroneThermalTogglePacket(boolean enabled)
    {
        this.enabled = enabled;
    }

    public static void encode(DroneThermalTogglePacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(packet.enabled);
    }

    public static DroneThermalTogglePacket decode(FriendlyByteBuf buffer)
    {
        return new DroneThermalTogglePacket(buffer.readBoolean());
    }

    public static void handle(DroneThermalTogglePacket packet, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            ScoutDroneEntity drone = ScoutDroneEntity.getControlledDrone(player);
            if (drone == null || !drone.hasThermalCameraModule()) {
                return;
            }

            drone.setThermalCameraEnabled(packet.enabled);
        });
        context.setPacketHandled(true);
    }
}
