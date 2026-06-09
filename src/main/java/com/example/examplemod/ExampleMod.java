package com.example.examplemod;

import com.example.examplemod.client.ScoutDroneModel;
import com.example.examplemod.client.ScoutDroneRenderer;
import com.example.examplemod.entity.ScoutDroneEntity;
import com.example.examplemod.item.DroneControllerItem;
import com.example.examplemod.item.DroneCoreItem;
import com.example.examplemod.item.TranslatedTooltipItem;
import com.example.examplemod.menu.ScoutDroneModuleMenu;
import com.example.examplemod.network.ModNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(ExampleMod.MODID)
public class ExampleMod
{
    public static final String MODID = "dronecraft";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Item> DRONE_CORE = ITEMS.register("drone",
            () -> new DroneCoreItem(new Item.Properties()));
    public static final RegistryObject<Item> DRONE_CONTROLLER = ITEMS.register("drone_controller",
            () -> new DroneControllerItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RANGE_MODULE = ITEMS.register("range_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.range_module"));
    public static final RegistryObject<Item> BASIC_RANGE_MODULE = ITEMS.register("basic_range_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.basic_range_module"));
    public static final RegistryObject<Item> ADVANCED_RANGE_MODULE = ITEMS.register("advanced_range_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.advanced_range_module"));
    public static final RegistryObject<Item> ELITE_RANGE_MODULE = ITEMS.register("elite_range_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.elite_range_module"));
    public static final RegistryObject<Item> INFINITE_RANGE_MODULE = ITEMS.register("infinite_range_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.infinite_range_module"));
    public static final RegistryObject<Item> ZOOM_MODULE = ITEMS.register("zoom_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.zoom_module"));
    public static final RegistryObject<Item> LIGHT_MODULE = ITEMS.register("light_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.light_module"));
    public static final RegistryObject<Item> EXPLOSIVE_MODULE = ITEMS.register("explosive_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.explosive_module"));
    public static final RegistryObject<Item> RETURN_MODULE = ITEMS.register("return_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.return_module"));
    public static final RegistryObject<Item> SPEED_MODULE = ITEMS.register("speed_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.speed_module"));
    public static final RegistryObject<Item> ADVANCED_SPEED_MODULE = ITEMS.register("advanced_speed_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.advanced_speed_module"));
    public static final RegistryObject<Item> ELITE_SPEED_MODULE = ITEMS.register("elite_speed_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.elite_speed_module"));
    public static final RegistryObject<Item> THERMAL_CAMERA_MODULE = ITEMS.register("thermal_camera_module",
            () -> new TranslatedTooltipItem(new Item.Properties().stacksTo(1), "tooltip.dronecraft.thermal_camera_module"));

    public static final RegistryObject<MenuType<ScoutDroneModuleMenu>> SCOUT_DRONE_MODULE_MENU = MENU_TYPES.register("scout_drone_module",
            () -> IForgeMenuType.create(ScoutDroneModuleMenu::new));

    public static final RegistryObject<EntityType<ScoutDroneEntity>> SCOUT_DRONE = ENTITY_TYPES.register("scout_drone",
            () -> EntityType.Builder.of(ScoutDroneEntity::new, MobCategory.MISC)
                    .sized(0.8F, 0.4F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("scout_drone"));

    public static final RegistryObject<CreativeModeTab> DRONECRAFT_TAB = CREATIVE_MODE_TABS.register("dronecraft", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.dronecraft"))
            .icon(() -> DRONE_CORE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(DRONE_CORE.get());
                output.accept(DRONE_CONTROLLER.get());
                output.accept(RANGE_MODULE.get());
                output.accept(ADVANCED_RANGE_MODULE.get());
                output.accept(ELITE_RANGE_MODULE.get());
                output.accept(INFINITE_RANGE_MODULE.get());
                output.accept(ZOOM_MODULE.get());
                output.accept(LIGHT_MODULE.get());
                output.accept(EXPLOSIVE_MODULE.get());
                output.accept(RETURN_MODULE.get());
                output.accept(SPEED_MODULE.get());
                output.accept(ADVANCED_SPEED_MODULE.get());
                output.accept(ELITE_SPEED_MODULE.get());
                output.accept(THERMAL_CAMERA_MODULE.get());
            }).build());

    public ExampleMod(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ModNetworking.register();
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
        {
            event.registerEntityRenderer(SCOUT_DRONE.get(), ScoutDroneRenderer::new);
        }

        @SubscribeEvent
        public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
        {
            event.registerLayerDefinition(ScoutDroneModel.LAYER_LOCATION, ScoutDroneModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event)
        {
            com.example.examplemod.client.DroneClientPilot.registerKeyMappings(event);
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> net.minecraft.client.gui.screens.MenuScreens.register(
                    SCOUT_DRONE_MODULE_MENU.get(),
                    com.example.examplemod.client.ScoutDroneModuleScreen::new));
        }
    }
}
