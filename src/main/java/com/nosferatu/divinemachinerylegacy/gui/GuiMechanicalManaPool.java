package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalManaPool;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Uses the permitted Extra Reforked development GUI artwork. */
public class GuiMechanicalManaPool extends GuiContainer {

    private static final int WIDTH = 216;
    private static final int INVENTORY_WIDTH = 188;
    private static final int INVENTORY_HEIGHT = 95;
    private static final int INVENTORY_Y = 136;
    private static final int MANA_BAR_WIDTH = 130;

    private static final ResourceLocation GUI_MALACHITE = texture("textures/gui/reforked/base_mechanical_mana_pool.png");
    private static final ResourceLocation GUI_SAFFRON = texture("textures/gui/reforked/upgraded_mechanical_mana_pool.png");
    private static final ResourceLocation GUI_SHADOW = texture("textures/gui/reforked/advanced_mechanical_mana_pool.png");
    private static final ResourceLocation GUI_CRIMSON = texture("textures/gui/reforked/ultimate_mechanical_mana_pool.png");

    private static final ResourceLocation INVENTORY_MALACHITE = texture("textures/gui/reforked/inventory_modules/base_inventory.png");
    private static final ResourceLocation INVENTORY_SAFFRON = texture("textures/gui/reforked/inventory_modules/upgrade_inventory.png");
    private static final ResourceLocation INVENTORY_SHADOW = texture("textures/gui/reforked/inventory_modules/advanced_inventory.png");
    private static final ResourceLocation INVENTORY_CRIMSON = texture("textures/gui/reforked/inventory_modules/ultimate_inventory.png");

    private static final ResourceLocation MANA_BAR = texture("textures/gui/reforked/misc/current_mana.png");
    private static final ResourceLocation INFINITE_MANA_BAR = texture("textures/gui/reforked/misc/infinity_mana.png");

    private final TileMechanicalManaPool tile;

    public GuiMechanicalManaPool(InventoryPlayer playerInventory, TileMechanicalManaPool tile) {
        super(new ContainerMechanicalManaPool(playerInventory, tile));
        this.tile = tile;
        this.xSize = WIDTH;
        this.ySize = INVENTORY_Y + INVENTORY_HEIGHT;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        MachineTier tier = tile.getTier();
        int offset = tier == MachineTier.CRIMSON ? 0 : 10;
        fontRendererObj.drawString("Mechanical Mana Pool - " + displayTier(tier), offset + 8, 6, 0x303030);

        String mana = tile.hasInfiniteMana()
                ? "Mana: INFINITE"
                : "Mana: " + tile.getCurrentMana() + " / " + tile.getManaCapacity();
        fontRendererObj.drawString(mana, offset + 33, 113, 0x24535A);

        if (tile.getLastBatch() > 0 && tile.getCooldown() > 0) {
            fontRendererObj.drawString("Last craft: x" + tile.getLastBatch(), offset + 73, 91, 0x303030);
        }
        if (tile.hasManaVoid()) {
            fontRendererObj.drawString("Mana Void active", offset + 65, 102, 0x603030);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        MachineTier tier = tile.getTier();
        int offset = tier == MachineTier.CRIMSON ? 0 : 10;
        int machineWidth = tier == MachineTier.CRIMSON ? 216 : 196;
        int machineHeight = tier == MachineTier.CRIMSON ? 134 : 124;

        mc.getTextureManager().bindTexture(machineTexture(tier));
        drawTexturedModalRect(guiLeft + offset, guiTop, 0, 0, machineWidth, machineHeight);

        mc.getTextureManager().bindTexture(inventoryTexture(tier));
        int inventoryX = guiLeft + (WIDTH - INVENTORY_WIDTH) / 2;
        drawTexturedModalRect(inventoryX, guiTop + INVENTORY_Y, 0, 0, INVENTORY_WIDTH, INVENTORY_HEIGHT);

        double ratio = tile.hasInfiniteMana()
                ? 1D
                : (tile.getManaCapacity() <= 0 ? 0D
                : Math.min(1D, Math.max(0D, tile.getCurrentMana() / (double) tile.getManaCapacity())));
        int manaWidth = (int) Math.round(MANA_BAR_WIDTH * ratio);
        if (manaWidth > 0) {
            mc.getTextureManager().bindTexture(tile.hasInfiniteMana() ? INFINITE_MANA_BAR : MANA_BAR);
            drawStandaloneTexture(guiLeft + offset + 33, guiTop + 102,
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
