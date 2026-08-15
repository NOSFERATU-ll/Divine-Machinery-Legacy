package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalManaInfuser;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Uses the permitted Extra Reforked Mana Infuser GUI artwork. */
public class GuiMechanicalManaInfuser extends GuiContainer {
    private static final int INVENTORY_WIDTH = 188;
    private static final int INVENTORY_HEIGHT = 95;
    private static final int MANA_BAR_WIDTH = 130;

    private static final ResourceLocation BASE = tex("textures/gui/reforked/base_mana_infuser.png");
    private static final ResourceLocation UPGRADED = tex("textures/gui/reforked/upgraded_mana_infuser.png");
    private static final ResourceLocation ADVANCED = tex("textures/gui/reforked/advanced_mana_infuser.png");
    private static final ResourceLocation ULTIMATE = tex("textures/gui/reforked/ultimate_mana_infuser.png");
    private static final ResourceLocation INV_BASE = tex("textures/gui/reforked/inventory_modules/base_inventory.png");
    private static final ResourceLocation INV_UPGRADED = tex("textures/gui/reforked/inventory_modules/upgrade_inventory.png");
    private static final ResourceLocation INV_ADVANCED = tex("textures/gui/reforked/inventory_modules/advanced_inventory.png");
    private static final ResourceLocation INV_ULTIMATE = tex("textures/gui/reforked/inventory_modules/ultimate_inventory.png");
    private static final ResourceLocation MANA = tex("textures/gui/reforked/misc/current_mana.png");
    private static final ResourceLocation MANA_INF = tex("textures/gui/reforked/misc/infinity_mana.png");

    private final TileMechanicalManaInfuser tile;
    private final ContainerMechanicalManaInfuser infuserContainer;

    public GuiMechanicalManaInfuser(InventoryPlayer inventory, TileMechanicalManaInfuser tile) {
        this(new ContainerMechanicalManaInfuser(inventory, tile), tile);
    }

    private GuiMechanicalManaInfuser(ContainerMechanicalManaInfuser container, TileMechanicalManaInfuser tile) {
        super(container);
        this.infuserContainer = container;
        this.tile = tile;
        this.xSize = tile.getTier() == MachineTier.CRIMSON ? 216 : 196;
        this.ySize = machineHeight(tile.getTier()) + 2 + INVENTORY_HEIGHT;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String tier = tile.getTier().getKey();
        fontRendererObj.drawString("Mana Infuser - " + Character.toUpperCase(tier.charAt(0)) + tier.substring(1), 10, 7, 0x303030);
        if (tile.getActiveBatch() > 0)
            fontRendererObj.drawString("x" + tile.getActiveBatch(), xSize / 2 - 5, machineHeight(tile.getTier()) - 36, 0x303030);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        MachineTier tier = tile.getTier();
        int height = machineHeight(tier);

        mc.getTextureManager().bindTexture(machineTexture(tier));
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, height);

        mc.getTextureManager().bindTexture(inventoryTexture(tier));
        drawTexturedModalRect(guiLeft + (xSize - INVENTORY_WIDTH) / 2,
                guiTop + height + 2, 0, 0, INVENTORY_WIDTH, INVENTORY_HEIGHT);

        double ratio = tile.hasInfiniteMana() ? 1D
                : Math.min(1D, Math.max(0D, tile.getCurrentMana() / (double) tile.getManaCapacity()));
        int manaWidth = (int) Math.round(MANA_BAR_WIDTH * ratio);
        if (manaWidth > 0) {
            mc.getTextureManager().bindTexture(tile.hasInfiniteMana() ? MANA_INF : MANA);
            int manaX = tier == MachineTier.CRIMSON ? 43 : 33;
            int manaY = tier == MachineTier.CRIMSON ? 128 : 109;
            drawStandalone(guiLeft + manaX, guiTop + manaY, manaWidth, 5,
                    manaWidth / (double) MANA_BAR_WIDTH, 1D);
        }

        int max = infuserContainer.getClientMaxProgress();
        if (max > 0 && tile.getProgress() > 0) {
            double p = Math.min(1D, tile.getProgress() / (double) max);
            int overlayHeight = Math.max(1, (int) Math.round(16D * p));
            mc.getTextureManager().bindTexture(machineTexture(tier));
            int dx = tier == MachineTier.CRIMSON ? 88 : 78;
            int dy = tier == MachineTier.CRIMSON ? 69 : 57;
            // Reforked stores this 40px-wide progress strip directly to the right of the main GUI.
            drawTexturedModalRect(guiLeft + dx, guiTop + dy, xSize, 0, 40, overlayHeight);
        }
    }

    private void drawStandalone(int x, int y, int width, int height, double uMax, double vMax) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + height, zLevel, 0, vMax);
        t.addVertexWithUV(x + width, y + height, zLevel, uMax, vMax);
        t.addVertexWithUV(x + width, y, zLevel, uMax, 0);
        t.addVertexWithUV(x, y, zLevel, 0, 0);
        t.draw();
    }

    private static int machineHeight(MachineTier tier) { return tier == MachineTier.CRIMSON ? 147 : 131; }
    private static ResourceLocation tex(String path) { return new ResourceLocation(DivineMachineryLegacy.MODID, path); }
    private static ResourceLocation machineTexture(MachineTier tier) {
        switch (tier) {
            case SAFFRON: return UPGRADED;
            case SHADOW: return ADVANCED;
            case CRIMSON: return ULTIMATE;
            default: return BASE;
        }
    }
    private static ResourceLocation inventoryTexture(MachineTier tier) {
        switch (tier) {
            case SAFFRON: return INV_UPGRADED;
            case SHADOW: return INV_ADVANCED;
            case CRIMSON: return INV_ULTIMATE;
            default: return INV_BASE;
        }
    }
}
