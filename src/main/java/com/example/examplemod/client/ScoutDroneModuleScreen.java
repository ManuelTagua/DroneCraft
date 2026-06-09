package com.example.examplemod.client;

import com.example.examplemod.menu.ScoutDroneModuleMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ScoutDroneModuleScreen extends AbstractContainerScreen<ScoutDroneModuleMenu>
{
    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 204;
    private static final int PLAYER_INVENTORY_X = 38;
    private static final int PLAYER_INVENTORY_Y = 120;
    private static final int HOTBAR_Y = 178;
    private static final int[] MODULE_SLOT_X = {32, 68, 104, 140, 176, 86, 122};
    private static final int[] MODULE_SLOT_Y = {43, 43, 43, 43, 43, 70, 70};

    public ScoutDroneModuleScreen(ScoutDroneModuleMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = PLAYER_INVENTORY_X;
        inventoryLabelY = 108;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY)
    {
        int x = leftPos;
        int y = topPos;
        drawPanel(guiGraphics, x, y, imageWidth, imageHeight);

        for (int i = 0; i < MODULE_SLOT_X.length; i++) {
            drawSlot(guiGraphics, x + MODULE_SLOT_X[i] - 1, y + MODULE_SLOT_Y[i] - 1);
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(guiGraphics, x + PLAYER_INVENTORY_X + column * 18 - 1, y + PLAYER_INVENTORY_Y + row * 18 - 1);
            }
        }

        for (int column = 0; column < 9; column++) {
            drawSlot(guiGraphics, x + PLAYER_INVENTORY_X + column * 18 - 1, y + HOTBAR_Y - 1);
        }
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height)
    {
        guiGraphics.fill(x, y, x + width, y + height, 0xFFC6C6C6);
        guiGraphics.fill(x, y, x + width, y + 1, 0xFFFFFFFF);
        guiGraphics.fill(x, y, x + 1, y + height, 0xFFFFFFFF);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, 0xFF555555);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, 0xFF555555);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFFEFEFEF);
        guiGraphics.fill(x + 1, y + 1, x + 2, y + height - 1, 0xFFEFEFEF);
        guiGraphics.fill(x + 2, y + height - 2, x + width - 1, y + height - 1, 0xFF8B8B8B);
        guiGraphics.fill(x + width - 2, y + 2, x + width - 1, y + height - 1, 0xFF8B8B8B);
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y)
    {
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF373737);
        guiGraphics.fill(x + 1, y + 1, x + 18, y + 18, 0xFFFFFFFF);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
        guiGraphics.fill(x + 2, y + 2, x + 17, y + 17, 0xFFB8B8B8);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
