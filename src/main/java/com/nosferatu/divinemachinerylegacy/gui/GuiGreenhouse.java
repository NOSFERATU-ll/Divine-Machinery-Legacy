package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.tile.TileGreenhouse;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

/** Greenhouse screen using the permitted Reforked GUI and bar artwork. */
public class GuiGreenhouse extends GuiContainer {
    private static final ResourceLocation GUI = tex("textures/gui/reforked/greenhouse.png");
    private static final ResourceLocation INVENTORY = tex("textures/gui/reforked/inventory_modules/ultimate_inventory.png");
    private static final ResourceLocation MANA = tex("textures/gui/reforked/misc/current_mana.png");
    private static final ResourceLocation ENERGY = tex("textures/gui/reforked/misc/current_energy.png");
    private static final ResourceLocation HEAT = tex("textures/gui/reforked/misc/current_heat.png");
    private final TileGreenhouse tile;

    public GuiGreenhouse(InventoryPlayer inventory, TileGreenhouse tile) {
        super(new ContainerGreenhouse(inventory, tile));
        this.tile = tile;
        xSize = 216;
        ySize = 233;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(StatCollector.translateToLocal("container.divinemachinerylegacy.greenhouse"), 8, 6, 0x303030);
        fontRendererObj.drawString(String.format("%,d RF", tile.getEnergy()), 80, 123, 0x303030);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F,1F,1F,1F);
        mc.getTextureManager().bindTexture(GUI);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 216, 136);
        mc.getTextureManager().bindTexture(INVENTORY);
        drawTexturedModalRect(guiLeft + 14, guiTop + 138, 0, 0, 188, 95);

        drawBar(MANA, guiLeft + 43, guiTop + 104, tile.getCurrentMana(), tile.getManaCapacity(), 130, 5);
        drawBar(ENERGY, guiLeft + 43, guiTop + 114, tile.getEnergy(), tile.getEnergyCapacity(), 130, 5);
        int heatHeight = (int) Math.round(40D * tile.getHeat() / TileGreenhouse.MAX_HEAT);
        if (heatHeight > 0) {
            mc.getTextureManager().bindTexture(HEAT);
            drawStandalone(guiLeft + 10, guiTop + 100 - heatHeight, 4, heatHeight, 1D, heatHeight / 40D);
            drawStandalone(guiLeft + 202, guiTop + 100 - heatHeight, 4, heatHeight, 1D, heatHeight / 40D);
        }
    }

    private void drawBar(ResourceLocation texture, int x, int y, int value, int max, int width, int height) {
        if (max <= 0 || value <= 0) return;
        double ratio = Math.min(1D, Math.max(0D, value / (double) max));
        int w = (int) Math.round(width * ratio);
        mc.getTextureManager().bindTexture(texture);
        drawStandalone(x, y, w, height, ratio, 1D);
    }

    private void drawStandalone(int x, int y, int width, int height, double uMax, double vMax) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + height, zLevel, 0D, vMax);
        t.addVertexWithUV(x + width, y + height, zLevel, uMax, vMax);
        t.addVertexWithUV(x + width, y, zLevel, uMax, 0D);
        t.addVertexWithUV(x, y, zLevel, 0D, 0D);
        t.draw();
    }

    private static ResourceLocation tex(String path) { return new ResourceLocation(DivineMachineryLegacy.MODID, path); }
}
