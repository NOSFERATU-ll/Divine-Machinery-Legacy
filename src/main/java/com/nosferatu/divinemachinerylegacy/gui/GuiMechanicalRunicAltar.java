package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;

/**
 * Deliberately code-drawn placeholder GUI. No upstream Extra Reforked GUI
 * texture is redistributed because its README reserves those assets.
 */
public class GuiMechanicalRunicAltar extends GuiContainer {

    private final TileMechanicalRunicAltar tile;
    private final ContainerMechanicalRunicAltar container;

    public GuiMechanicalRunicAltar(InventoryPlayer playerInventory, TileMechanicalRunicAltar tile) {
        super(new ContainerMechanicalRunicAltar(playerInventory, tile));
        this.tile = tile;
        this.container = (ContainerMechanicalRunicAltar) inventorySlots;
        this.xSize = 220;
        this.ySize = 233;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        MachineTier tier = tile.getTier();
        fontRendererObj.drawString("Mechanical Runic Altar - " + tier.getKey(), 8, 7, 0xE8E8E8);
        fontRendererObj.drawString("Livingrock", 8, 39, 0xB8B8B8);
        fontRendererObj.drawString("Inputs", 8, 45, 0xB8B8B8);
        fontRendererObj.drawString("Outputs", 128, 45, 0xB8B8B8);

        int maxProgress = container.getSyncedMaxProgress();
        String status = tile.getCurrentBatch() > 0
                ? "Batch x" + tile.getCurrentBatch() + "  " + tile.getProgress() + "/" + maxProgress
                : "Idle";
        fontRendererObj.drawString(status, 8, 126, 0xD8D8D8);

        String mana = "Mana: " + tile.getCurrentMana() + " / " + tile.getManaCapacity();
        fontRendererObj.drawString(mana, 8, 140, 0xA8C8FF);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        int left = guiLeft;
        int top = guiTop;

        // Main background.
        drawRect(left, top, left + xSize, top + ySize, 0xFF20242A);
        drawRect(left + 4, top + 4, left + xSize - 4, top + ySize - 4, 0xFF30363D);

        // Mana bar.
        int barX1 = left + 8;
        int barY1 = top + 133;
        int barX2 = left + 212;
        int barY2 = top + 138;
        drawRect(barX1, barY1, barX2, barY2, 0xFF161A20);
        if (tile.getManaCapacity() > 0) {
            double ratio = Math.min(1D, Math.max(0D, tile.getCurrentMana() / (double) tile.getManaCapacity()));
            int fill = (int) ((barX2 - barX1) * ratio);
            drawRect(barX1, barY1, barX1 + fill, barY2, 0xFF2B75C9);
        }

        // Slot frames for every exposed machine/player slot.
        for (Object object : inventorySlots.inventorySlots) {
            Slot slot = (Slot) object;
            int x = left + slot.xDisplayPosition - 1;
            int y = top + slot.yDisplayPosition - 1;
            drawRect(x, y, x + 18, y + 18, 0xFF121519);
            drawRect(x + 1, y + 1, x + 17, y + 17, 0xFF4A5159);
            drawRect(x + 2, y + 2, x + 16, y + 16, 0xFF252A30);
        }
    }
}
