package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalAlfheimMarket;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Uses the permitted Extra Reforked Mechanical Alfheim Market GUI artwork. */
public class GuiMechanicalAlfheimMarket extends GuiContainer {
    private static final int WIDTH = 216;
    private static final int MACHINE_WIDTH = 196;
    private static final int INVENTORY_WIDTH = 188;
    private static final int INVENTORY_HEIGHT = 95;
    private static final int MANA_BAR_WIDTH = 130;

    private static final ResourceLocation GUI_MALACHITE = texture("textures/gui/reforked/base_alfheim_market.png");
    private static final ResourceLocation GUI_SAFFRON = texture("textures/gui/reforked/upgraded_alfheim_market.png");
    private static final ResourceLocation GUI_SHADOW = texture("textures/gui/reforked/advanced_alfheim_market.png");
    private static final ResourceLocation GUI_CRIMSON = texture("textures/gui/reforked/ultimate_alfheim_market.png");

    private static final ResourceLocation INVENTORY_MALACHITE = texture("textures/gui/reforked/inventory_modules/base_inventory.png");
    private static final ResourceLocation INVENTORY_SAFFRON = texture("textures/gui/reforked/inventory_modules/upgrade_inventory.png");
    private static final ResourceLocation INVENTORY_SHADOW = texture("textures/gui/reforked/inventory_modules/advanced_inventory.png");
    private static final ResourceLocation INVENTORY_CRIMSON = texture("textures/gui/reforked/inventory_modules/ultimate_inventory.png");

    private static final ResourceLocation MANA_BAR = texture("textures/gui/reforked/misc/current_mana.png");
    private static final ResourceLocation INFINITE_MANA_BAR = texture("textures/gui/reforked/misc/infinity_mana.png");

    private final TileMechanicalAlfheimMarket tile;

    public GuiMechanicalAlfheimMarket(InventoryPlayer playerInventory, TileMechanicalAlfheimMarket tile) {
        super(new ContainerMechanicalAlfheimMarket(playerInventory, tile));
        this.tile = tile;
        this.xSize = WIDTH;
        this.ySize = machineHeight(tile.getTier()) + 2 + INVENTORY_HEIGHT;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString("Alfheim Market - " + displayTier(tile.getTier()), 18, 6, 0x303030);
        if (tile.getActiveBatch() > 0) {
            int y = tile.getTier() == MachineTier.CRIMSON ? 108 : 92;
            fontRendererObj.drawString("Batch: x" + tile.getActiveBatch(), 78, y, 0x303030);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        MachineTier tier = tile.getTier();
        int offset = 10;
        int machineHeight = machineHeight(tier);

        mc.getTextureManager().bindTexture(machineTexture(tier));
        drawTexturedModalRect(guiLeft + offset, guiTop, 0, 0, MACHINE_WIDTH, machineHeight);

        if (tile.getProgress() > 0) {
            int progressWidth = Math.round(16F * tile.getProgress() / (float) TileMechanicalAlfheimMarket.RECIPE_MANA);
            if (progressWidth > 0) {
                int progressY = tier == MachineTier.CRIMSON ? 61 : 53;
                drawTexturedModalRect(guiLeft + 100, guiTop + progressY,
                        MACHINE_WIDTH, 0, Math.min(16, progressWidth), 16);
            }
        }

        mc.getTextureManager().bindTexture(inventoryTexture(tier));
        int inventoryX = guiLeft + (WIDTH - INVENTORY_WIDTH) / 2;
        int inventoryY = guiTop + machineHeight + 2;
        drawTexturedModalRect(inventoryX, inventoryY, 0, 0, INVENTORY_WIDTH, INVENTORY_HEIGHT);

        double ratio = tile.hasInfiniteMana() ? 1D
                : Math.min(1D, Math.max(0D, tile.getCurrentMana() / (double) tile.getManaCapacity()));
        int manaWidth = (int) Math.round(MANA_BAR_WIDTH * ratio);
        if (manaWidth > 0) {
            int manaY = tier == MachineTier.CRIMSON ? 118 : 102;
            mc.getTextureManager().bindTexture(tile.hasInfiniteMana() ? INFINITE_MANA_BAR : MANA_BAR);
            drawStandaloneTexture(guiLeft + 43, guiTop + manaY,
                    manaWidth, 5, manaWidth / (double) MANA_BAR_WIDTH, 1D);
        }
    }

    private void drawStandaloneTexture(int x, int y, int width, int height, double uMax, double vMax) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, zLevel, 0D, vMax);
        tessellator.addVertexWithUV(x + width, y + height, zLevel, uMax, vMax);
        tessellator.addVertexWithUV(x + width, y, zLevel, uMax, 0D);
        tessellator.addVertexWithUV(x, y, zLevel, 0D, 0D);
        tessellator.draw();
    }

    private static int machineHeight(MachineTier tier) {
        return tier == MachineTier.CRIMSON ? 140 : 124;
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(DivineMachineryLegacy.MODID, path);
    }

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
