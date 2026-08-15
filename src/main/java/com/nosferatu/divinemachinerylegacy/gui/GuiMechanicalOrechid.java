package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalOrechid;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Uses the permitted Extra Reforked Mechanical Orechid GUI artwork. */
public class GuiMechanicalOrechid extends GuiContainer {
    private static final int WIDTH = 216;
    private static final int INVENTORY_WIDTH = 188;
    private static final int INVENTORY_HEIGHT = 95;
    private static final int MANA_BAR_WIDTH = 130;

    private static final ResourceLocation GUI_MALACHITE = texture("textures/gui/reforked/base_orechid.png");
    private static final ResourceLocation GUI_SAFFRON = texture("textures/gui/reforked/upgraded_orechid.png");
    private static final ResourceLocation GUI_SHADOW = texture("textures/gui/reforked/advanced_orechid.png");
    private static final ResourceLocation GUI_CRIMSON = texture("textures/gui/reforked/ultimate_orechid.png");
    private static final ResourceLocation INVENTORY_MALACHITE = texture("textures/gui/reforked/inventory_modules/base_inventory.png");
    private static final ResourceLocation INVENTORY_SAFFRON = texture("textures/gui/reforked/inventory_modules/upgrade_inventory.png");
    private static final ResourceLocation INVENTORY_SHADOW = texture("textures/gui/reforked/inventory_modules/advanced_inventory.png");
    private static final ResourceLocation INVENTORY_CRIMSON = texture("textures/gui/reforked/inventory_modules/ultimate_inventory.png");
    private static final ResourceLocation MANA_BAR = texture("textures/gui/reforked/misc/current_mana.png");
    private static final ResourceLocation INFINITE_MANA_BAR = texture("textures/gui/reforked/misc/infinity_mana.png");

    private final TileMechanicalOrechid tile;

    public GuiMechanicalOrechid(InventoryPlayer playerInventory, TileMechanicalOrechid tile) {
        super(new ContainerMechanicalOrechid(playerInventory, tile));
        this.tile = tile;
        this.xSize = WIDTH;
        this.ySize = machineHeight(tile.getTier()) + 2 + INVENTORY_HEIGHT;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString("Mechanical Orechid - " + displayTier(tile.getTier()), 18, 6, 0x303030);
        if (tile.hasInfiniteStone()) {
            int y = machineHeight(tile.getTier()) - 34;
            fontRendererObj.drawString("Stone: INF", 76, y, 0x303030);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        MachineTier tier = tile.getTier();
        int machineWidth = tier == MachineTier.MALACHITE ? 196 : 206;
        int machineHeight = machineHeight(tier);
        int machineX = guiLeft + (WIDTH - machineWidth) / 2;

        mc.getTextureManager().bindTexture(machineTexture(tier));
        drawTexturedModalRect(machineX, guiTop, 0, 0, machineWidth, machineHeight);

        mc.getTextureManager().bindTexture(inventoryTexture(tier));
        drawTexturedModalRect(guiLeft + (WIDTH - INVENTORY_WIDTH) / 2,
                guiTop + machineHeight + 2, 0, 0, INVENTORY_WIDTH, INVENTORY_HEIGHT);

        double ratio = tile.hasInfiniteMana() ? 1D
                : Math.min(1D, Math.max(0D, tile.getCurrentMana() / (double) tile.getManaCapacity()));
        int manaWidth = (int) Math.round(MANA_BAR_WIDTH * ratio);
        if (manaWidth > 0) {
            mc.getTextureManager().bindTexture(tile.hasInfiniteMana() ? INFINITE_MANA_BAR : MANA_BAR);
            int manaY = tier == MachineTier.SHADOW || tier == MachineTier.CRIMSON ? 144 : 104;
            drawStandaloneTexture(guiLeft + 43, guiTop + manaY,
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

    private static int machineHeight(MachineTier tier) {
        return tier == MachineTier.SHADOW || tier == MachineTier.CRIMSON ? 164 : 124;
    }

    private static ResourceLocation texture(String path) { return new ResourceLocation(DivineMachineryLegacy.MODID, path); }

    private static ResourceLocation machineTexture(MachineTier tier) {
        switch (tier) {
            case SAFFRON: return GUI_SAFFRON;
            case SHADOW: return GUI_SHADOW;
            case CRIMSON: return GUI_CRIMSON;
            case MALACHITE:
            default: return GUI_MALACHITE;
        }
    }

    private static ResourceLocation inventoryTexture(MachineTier tier) {
        switch (tier) {
            case SAFFRON: return INVENTORY_SAFFRON;
            case SHADOW: return INVENTORY_SHADOW;
            case CRIMSON: return INVENTORY_CRIMSON;
            case MALACHITE:
            default: return INVENTORY_MALACHITE;
        }
    }

    private static String displayTier(MachineTier tier) {
        String key = tier.getKey();
        return Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }
}
