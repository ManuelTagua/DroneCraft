package com.example.examplemod.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class DroneCameraPacket
{
    private final int droneEntityId;
    private final boolean active;

    public DroneCameraPacket(int droneEntityId, boolean active)
    {
        this.droneEntityId = droneEntityId;
        this.active = active;
    }

    public static void encode(DroneCameraPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeInt(packet.droneEntityId);
        buffer.writeBoolean(packet.active);
    }

    public static DroneCameraPacket decode(FriendlyByteBuf buffer)
    {
        return new DroneCameraPacket(buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(DroneCameraPacket packet, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            if (packet.active) {
                com.example.examplemod.client.DroneClientPilot.startPiloting(packet.droneEntityId);
            } else {
                com.example.examplemod.client.DroneClientPilot.stopPiloting();
            }
        }));
        context.setPacketHandled(true);
    }
}
