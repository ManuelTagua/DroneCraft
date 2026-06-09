package com.example.examplemod.menu;

import com.example.examplemod.ExampleMod;
import com.example.examplemod.entity.ScoutDroneEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ScoutDroneModuleMenu extends AbstractContainerMenu
{
    public static final int RANGE_SLOT = 0;
    public static final int CAMERA_SLOT = 1;
    public static final int UTILITY_SLOT = 2;
    public static final int EXPLOSIVE_SLOT = 3;
    public static final int RETURN_SLOT = 4;
    public static final int SPEED_SLOT = 5;
    public static final int THERMAL_SLOT = 6;
    private static final int MODULE_SLOT_COUNT = 7;
    private static final int PLAYER_INVENTORY_START = MODULE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;
    private static final int MODULE_SLOT_Y = 43;
    private static final int SECOND_MODULE_SLOT_Y = 70;
    private static final int PLAYER_INVENTORY_X = 38;
    private static final int PLAYER_INVENTORY_Y = 120;
    private static final int HOTBAR_Y = 178;

    private final ScoutDroneEntity drone;
    private final Container moduleContainer;

    public ScoutDroneModuleMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer)
    {
        this(containerId, playerInventory, getDroneFromBuffer(playerInventory, buffer));
    }

    public ScoutDroneModuleMenu(int containerId, Inventory playerInventory, ScoutDroneEntity drone)
    {
        super(ExampleMod.SCOUT_DRONE_MODULE_MENU.get(), containerId);
        this.drone = drone;
        this.moduleContainer = createModuleContainer(drone);

        addSlot(new Slot(moduleContainer, RANGE_SLOT, 32, MODULE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return ScoutDroneEntity.isRangeModule(stack);
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        addSlot(new Slot(moduleContainer, CAMERA_SLOT, 68, MODULE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return stack.is(ExampleMod.ZOOM_MODULE.get());
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        addSlot(new Slot(moduleContainer, UTILITY_SLOT, 104, MODULE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return stack.is(ExampleMod.LIGHT_MODULE.get());
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        addSlot(new Slot(moduleContainer, EXPLOSIVE_SLOT, 140, MODULE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return stack.is(ExampleMod.EXPLOSIVE_MODULE.get());
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        addSlot(new Slot(moduleContainer, RETURN_SLOT, 176, MODULE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return stack.is(ExampleMod.RETURN_MODULE.get());
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        addSlot(new Slot(moduleContainer, SPEED_SLOT, 86, SECOND_MODULE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return ScoutDroneEntity.isSpeedModule(stack);
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        addSlot(new Slot(moduleContainer, THERMAL_SLOT, 122, SECOND_MODULE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                return stack.is(ExampleMod.THERMAL_CAMERA_MODULE.get());
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, PLAYER_INVENTORY_X + column * 18, HOTBAR_Y));
        }
    }

    @Override
    public boolean stillValid(Player player)
    {
        return drone != null && drone.isAlive() && player.distanceToSqr(drone) < 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index)
    {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return result;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index >= RANGE_SLOT && index < MODULE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (ScoutDroneEntity.isRangeModule(stack)) {
            if (!moveItemStackTo(stack, RANGE_SLOT, RANGE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ExampleMod.ZOOM_MODULE.get())) {
            if (!moveItemStackTo(stack, CAMERA_SLOT, CAMERA_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ExampleMod.LIGHT_MODULE.get())) {
            if (!moveItemStackTo(stack, UTILITY_SLOT, UTILITY_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ExampleMod.EXPLOSIVE_MODULE.get())) {
            if (!moveItemStackTo(stack, EXPLOSIVE_SLOT, EXPLOSIVE_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ExampleMod.RETURN_MODULE.get())) {
            if (!moveItemStackTo(stack, RETURN_SLOT, RETURN_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (ScoutDroneEntity.isSpeedModule(stack)) {
            if (!moveItemStackTo(stack, SPEED_SLOT, SPEED_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.is(ExampleMod.THERMAL_CAMERA_MODULE.get())) {
            if (!moveItemStackTo(stack, THERMAL_SLOT, THERMAL_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= PLAYER_INVENTORY_START && index < PLAYER_INVENTORY_END) {
            if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= HOTBAR_START && index < HOTBAR_END && !moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return result;
    }

    private static ScoutDroneEntity getDroneFromBuffer(Inventory playerInventory, FriendlyByteBuf buffer)
    {
        Entity entity = playerInventory.player.level().getEntity(buffer.readInt());
        return entity instanceof ScoutDroneEntity drone ? drone : null;
    }

    private static Container createModuleContainer(ScoutDroneEntity drone)
    {
        class ModuleContainer extends SimpleContainer
        {
            private boolean syncEnabled;

            ModuleContainer()
            {
                super(MODULE_SLOT_COUNT);
            }

            void enableSync()
            {
                syncEnabled = true;
            }

            @Override
            public void setChanged()
            {
                super.setChanged();
                if (syncEnabled && drone != null && !drone.level().isClientSide) {
                    drone.setRangeModuleStack(getItem(RANGE_SLOT));
                    drone.setCameraModuleStack(getItem(CAMERA_SLOT));
                    drone.setUtilityModuleStack(getItem(UTILITY_SLOT));
                    drone.setExplosiveModuleStack(getItem(EXPLOSIVE_SLOT));
                    drone.setReturnModuleStack(getItem(RETURN_SLOT));
                    drone.setSpeedModuleStack(getItem(SPEED_SLOT));
                    drone.setThermalCameraModuleStack(getItem(THERMAL_SLOT));
                }
            }
        }

        ModuleContainer container = new ModuleContainer();

        if (drone != null) {
            container.setItem(RANGE_SLOT, drone.getRangeModuleStack().copy());
            container.setItem(CAMERA_SLOT, drone.getCameraModuleStack().copy());
            container.setItem(UTILITY_SLOT, drone.getUtilityModuleStack().copy());
            container.setItem(EXPLOSIVE_SLOT, drone.getExplosiveModuleStack().copy());
            container.setItem(RETURN_SLOT, drone.getReturnModuleStack().copy());
            container.setItem(SPEED_SLOT, drone.getSpeedModuleStack().copy());
            container.setItem(THERMAL_SLOT, drone.getThermalCameraModuleStack().copy());
        }
        container.enableSync();

        return container;
    }
}
