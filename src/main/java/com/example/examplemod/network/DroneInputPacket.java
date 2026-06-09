package com.example.examplemod.network;

import com.example.examplemod.entity.ScoutDroneEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class DroneInputPacket
{
    private final boolean forward;
    private final boolean backward;
    private final boolean left;
    private final boolean right;
    private final boolean up;
    private final boolean down;
    private final float yaw;
    private final float pitch;

    public DroneInputPacket(boolean forward, boolean backward, boolean left, boolean right, boolean up, boolean down, float yaw, float pitch)
    {
        this.forward = forward;
        this.backward = backward;
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static void encode(DroneInputPacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(packet.forward);
        buffer.writeBoolean(packet.backward);
        buffer.writeBoolean(packet.left);
        buffer.writeBoolean(packet.right);
        buffer.writeBoolean(packet.up);
        buffer.writeBoolean(packet.down);
        buffer.writeFloat(packet.yaw);
        buffer.writeFloat(packet.pitch);
    }

    public static DroneInputPacket decode(FriendlyByteBuf buffer)
    {
        return new DroneInputPacket(
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readFloat());
    }

    public static void handle(DroneInputPacket packet, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            ScoutDroneEntity drone = ScoutDroneEntity.getControlledDrone(player);
            if (drone != null) {
                drone.applyPilotInput(packet.forward, packet.backward, packet.left, packet.right, packet.up, packet.down, packet.yaw, packet.pitch);
            }
        });
        context.setPacketHandled(true);
    }
}
