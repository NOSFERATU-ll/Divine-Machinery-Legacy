package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * 1.7.10 Runic Altar GUI using the original Botanical Machinery Extra
 * Reforked artwork with explicit permission from BOLT_M4G1C.
 * See docs/ASSET_PERMISSION_BOTANICAL_EXTRA_REFORKED.md.
 */
public class GuiMechanicalRunicAltar extends GuiContainer {

    private static final ResourceLocation GUI_BASE = new ResourceLocation(
            DivineMachineryLegacy.MODID, "textures/gui/reforked/base_mechanical_runic_altar.png");
    private static final ResourceLocation GUI_UPGRADED = new ResourceLocation(
            DivineMachineryLegacy.MODID, "textures/gui/reforked/upgraded_mechanical_runic_altar.png");
    private static final ResourceLocation GUI_ADVANCED = new ResourceLocation(
            DivineMachineryLegacy.MODID, "textures/gui/reforked/advanced_mechanical_runic_altar.png");
    private static final ResourceLocation GUI_ULTIMATE = new ResourceLocation(
            DivineMachineryLegacy.MODID, "textures/gui/reforked/ultimate_mechanical_runic_altar.png");

    private final TileMechanicalRunicAltar tile;
    private final ContainerMechanicalRunicAltar container;

    public GuiMechanicalRunicAltar(InventoryPlayer playerInventory, TileMechanicalRunicAltar tile) {
        super(new ContainerMechanicalRunicAltar(playerInventory, tile));
        this.tile = tile;
        this.container = (ContainerMechanicalRunicAltar) inventorySlots;
        this.xSize = 220;
        this.ySize = 233;
    }

    private ResourceLocation getTierTexture() {
        switch (tile.getTier()) {
            case SAFFRON:
                return GUI_UPGRADED;
            case SHADOW:
                return GUI_ADVANCED;
            case CRIMSON:
                return GUI_ULTIMATE;
            case MALACHITE:
            default:
                return GUI_BASE;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        MachineTier tier = tile.getTier();
        fontRendererObj.drawString("Mechanical Runic Altar - " + tier.getKey(), 8, 7, 0x303030);

        int maxProgress = container.getSyncedMaxProgress();
        String status = tile.getCurrentBatch() > 0
                ? "Batch x" + tile.getCurrentBatch() + "  " + tile.getProgress() + "/" + maxProgress
                : "Idle";
        fontRendererObj.drawString(status, 8, 126, 0x303030);

        String mana = tile.hasInfiniteMana()
                ? "Mana: INFINITE"
                : "Mana: " + tile.getCurrentMana() + " / " + tile.getManaCapacity();
        fontRendererObj.drawString(mana, 8, 140, 0x335588);

        if (tile.hasInfiniteLivingrock()) {
            fontRendererObj.drawString("Livingrock: INFINITE", 112, 140, 0x347A34);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        int left = guiLeft;
        int top = guiTop;

        // Extra Reforked's machine panel is a 256x256 texture with a 216x140
        // functional machine region. Draw that region unchanged, then extend the
        // lower area for the 1.7.10 player inventory used by this backport.
        mc.getTextureManager().bindTexture(getTierTexture());
        drawTexturedModalRect(left + 2, top, 0, 0, 216, 140);

        drawRect(left, top + 140, left + xSize, top + ySize, 0xFFC6C6C6);
        drawRect(left + 4, top + 144, left + xSize - 4, top + ySize - 4, 0xFF8B8B8B);

        // Mana overlay: retain the original artwork while making the backport's
        // very large tier buffers readable at a glance.
        int barX1 = left + 8;
        int barY1 = top + 133;
        int barX2 = left + 212;
        int barY2 = top + 138;
        drawRect(barX1, barY1, barX2, barY2, 0xAA202020);
        double ratio = tile.hasInfiniteMana()
                ? 1D
                : (tile.getManaCapacity() <= 0 ? 0D
                : Math.min(1D, Math.max(0D, tile.getCurrentMana() / (double) tile.getManaCapacity())));
        int fill = (int) ((barX2 - barX1) * ratio);
        drawRect(barX1, barY1, barX1 + fill, barY2, 0xFF2B75C9);

        // 1.7.10 slot positions are deliberately kept stable for pipes/AE2. The
        // thin frames make those active positions explicit over the upstream art.
        for (Object object : inventorySlots.inventorySlots) {
            Slot slot = (Slot) object;
            int x = left + slot.xDisplayPosition - 1;
            int y = top + slot.yDisplayPosition - 1;
            drawRect(x, y, x + 18, y + 18, 0xAA222222);
            drawRect(x + 1, y + 1, x + 17, y + 17, 0xAAE0E0E0);
        }
    }
}
