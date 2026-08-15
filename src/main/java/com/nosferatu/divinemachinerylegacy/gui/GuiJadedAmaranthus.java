package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.tile.TileJadedAmaranthus;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Uses the permitted Extra Reforked Jaded Amaranthus GUI artwork. */
public class GuiJadedAmaranthus extends GuiContainer {
    private static final int MACHINE_WIDTH = 196;
    private static final int MACHINE_HEIGHT = 124;
    private static final int INVENTORY_WIDTH = 188;
    private static final int INVENTORY_HEIGHT = 95;
    private static final int MANA_BAR_WIDTH = 130;

    private static final ResourceLocation GUI = texture("textures/gui/reforked/jaded_amaranthus.png");
    private static final ResourceLocation INVENTORY = texture("textures/gui/reforked/inventory_modules/other_inventory.png");
    private static final ResourceLocation MANA_BAR = texture("textures/gui/reforked/misc/current_mana.png");
    private static final ResourceLocation INFINITE_MANA_BAR = texture("textures/gui/reforked/misc/infinity_mana.png");

    private final TileJadedAmaranthus tile;

    public GuiJadedAmaranthus(InventoryPlayer playerInventory, TileJadedAmaranthus tile) {
        super(new ContainerJadedAmaranthus(playerInventory, tile));
        this.tile = tile;
        this.xSize = MACHINE_WIDTH;
        this.ySize = MACHINE_HEIGHT + 2 + INVENTORY_HEIGHT;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString("Jaded Amaranthus", 8, 6, 0x303030);
        if (tile.hasPetalBlockUpgrade()) {
            fontRendererObj.drawString("Petal Blocks", 68, 91, 0x303030);
        } else if (tile.hasPetalUpgrade()) {
            fontRendererObj.drawString("Petals x2", 72, 91, 0x303030);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);

        mc.getTextureManager().bindTexture(GUI);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, MACHINE_WIDTH, MACHINE_HEIGHT);

        mc.getTextureManager().bindTexture(INVENTORY);
        drawTexturedModalRect(guiLeft + (MACHINE_WIDTH - INVENTORY_WIDTH) / 2,
                guiTop + MACHINE_HEIGHT + 2, 0, 0, INVENTORY_WIDTH, INVENTORY_HEIGHT);

        double ratio = tile.hasInfiniteMana() ? 1D
                : Math.min(1D, Math.max(0D, tile.getCurrentMana() / (double) TileJadedAmaranthus.MANA_CAPACITY));
        int manaWidth = (int) Math.round(MANA_BAR_WIDTH * ratio);
        if (manaWidth > 0) {
            mc.getTextureManager().bindTexture(tile.hasInfiniteMana() ? INFINITE_MANA_BAR : MANA_BAR);
            drawStandaloneTexture(guiLeft + 33, guiTop + 102,
                    manaWidth, 5, manaWidth / (double) MANA_BAR_WIDTH, 1D);
        }
    }

    private void drawStandaloneTexture(int x, int y, int width, int height, double uMax, double vMax) {
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(x, y + height, zLevel, 0D, vMax);
        tess.addVertexWithUV(x + width, y + height, zLevel, uMax, vMax);
        tess.addVertexWithUV(x + width, y, zLevel, uMax, 0D);
        tess.addVertexWithUV(x, y, zLevel, 0D, 0D);
        tess.draw();
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(DivineMachineryLegacy.MODID, path);
    }
}
