package com.example.examplemod.network;

import com.example.examplemod.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking
{
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId;

    public static void register()
    {
        CHANNEL.registerMessage(packetId++, DroneInputPacket.class, DroneInputPacket::encode, DroneInputPacket::decode, DroneInputPacket::handle);
        CHANNEL.registerMessage(packetId++, DroneExitPilotPacket.class, DroneExitPilotPacket::encode, DroneExitPilotPacket::decode, DroneExitPilotPacket::handle);
        CHANNEL.registerMessage(packetId++, DroneCameraPacket.class, DroneCameraPacket::encode, DroneCameraPacket::decode, DroneCameraPacket::handle);
        CHANNEL.registerMessage(packetId++, DroneLightTogglePacket.class, DroneLightTogglePacket::encode, DroneLightTogglePacket::decode, DroneLightTogglePacket::handle);
        CHANNEL.registerMessage(packetId++, DroneThermalTogglePacket.class, DroneThermalTogglePacket::encode, DroneThermalTogglePacket::decode, DroneThermalTogglePacket::handle);
        CHANNEL.registerMessage(packetId++, DroneReturnPacket.class, DroneReturnPacket::encode, DroneReturnPacket::decode, DroneReturnPacket::handle);
        CHANNEL.registerMessage(packetId++, DroneExplodePacket.class, DroneExplodePacket::encode, DroneExplodePacket::decode, DroneExplodePacket::handle);
    }
}
