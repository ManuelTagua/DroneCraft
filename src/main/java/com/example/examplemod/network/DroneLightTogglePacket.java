package com.example.examplemod.network;

import com.example.examplemod.entity.ScoutDroneEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.network.NetworkEvent;

public class DroneLightTogglePacket
{
    private final boolean enabled;

    public DroneLightTogglePacket(boolean enabled)
    {
        this.enabled = enabled;
    }

    public static void encode(DroneLightTogglePacket packet, FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(packet.enabled);
    }

    public static DroneLightTogglePacket decode(FriendlyByteBuf buffer)
    {
        return new DroneLightTogglePacket(buffer.readBoolean());
    }

    public static void handle(DroneLightTogglePacket packet, Supplier<NetworkEvent.Context> contextSupplier)
    {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            ScoutDroneEntity drone = ScoutDroneEntity.getControlledDrone(player);
            if (drone == null || !drone.hasLightModule()) {
                player.removeEffect(MobEffects.NIGHT_VISION);
                return;
            }

            drone.setLightEnabled(packet.enabled);
            if (!packet.enabled) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
        });
        context.setPacketHandled(true);
    }
}
