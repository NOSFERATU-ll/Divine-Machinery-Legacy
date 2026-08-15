package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Development GUI using the original Botanical Machinery Extra Reforked art
 * with explicit permission from BOLT_M4G1C.
 * See docs/ASSET_PERMISSION_BOTANICAL_EXTRA_REFORKED.md.
 */
public class GuiMechanicalRunicAltar extends GuiContainer {

    private static final int MACHINE_WIDTH = 216;
    private static final int MACHINE_HEIGHT = 140;
    private static final int INVENTORY_WIDTH = 188;
    private static final int INVENTORY_HEIGHT = 95;
    private static final int INVENTORY_OFFSET_Y = 2;
    private static final int MANA_BAR_WIDTH = 130;

    private static final ResourceLocation GUI_MALACHITE = texture("textures/gui/reforked/base_mechanical_runic_altar.png");
    private static final ResourceLocation GUI_SAFFRON = texture("textures/gui/reforked/upgraded_mechanical_runic_altar.png");
    private static final ResourceLocation GUI_SHADOW = texture("textures/gui/reforked/advanced_mechanical_runic_altar.png");
    private static final ResourceLocation GUI_CRIMSON = texture("textures/gui/reforked/ultimate_mechanical_runic_altar.png");

    private static final ResourceLocation INVENTORY_MALACHITE = texture("textures/gui/reforked/inventory_modules/base_inventory.png");
    private static final ResourceLocation INVENTORY_SAFFRON = texture("textures/gui/reforked/inventory_modules/upgrade_inventory.png");
    private static final ResourceLocation INVENTORY_SHADOW = texture("textures/gui/reforked/inventory_modules/advanced_inventory.png");
    private static final ResourceLocation INVENTORY_CRIMSON = texture("textures/gui/reforked/inventory_modules/ultimate_inventory.png");

    private static final ResourceLocation MANA_BAR = texture("textures/gui/reforked/misc/current_mana.png");
    private static final ResourceLocation INFINITE_MANA_BAR = texture("textures/gui/reforked/misc/infinity_mana.png");

    private final TileMechanicalRunicAltar tile;
    private final ContainerMechanicalRunicAltar container;

    public GuiMechanicalRunicAltar(InventoryPlayer playerInventory, TileMechanicalRunicAltar tile) {
        super(new ContainerMechanicalRunicAltar(playerInventory, tile));
        this.tile = tile;
        this.container = (ContainerMechanicalRunicAltar) inventorySlots;
        this.xSize = MACHINE_WIDTH;
        this.ySize = MACHINE_HEIGHT + INVENTORY_OFFSET_Y + INVENTORY_HEIGHT;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        MachineTier tier = tile.getTier();
        fontRendererObj.drawString("Mechanical Runic Altar - " + displayTier(tier), 8, 6, 0x303030);

        int maxProgress = container.getSyncedMaxProgress();
        if (tile.getCurrentBatch() > 0) {
            String status = "x" + tile.getCurrentBatch() + "  " + tile.getProgress() + "/" + maxProgress;
            fontRendererObj.drawString(status, 82, 109, 0x303030);
        }

        String mana = tile.hasInfiniteMana()
                ? "Mana: INFINITE"
                : "Mana: " + tile.getCurrentMana() + " / " + tile.getManaCapacity();
        fontRendererObj.drawString(mana, 43, 128, 0x24535A);

        if (tile.hasInfiniteLivingrock()) {
            fontRendererObj.drawString("Livingrock: INFINITE", 72, 91, 0x2D604D);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        MachineTier tier = tile.getTier();
        int left = guiLeft;
        int top = guiTop;

        mc.getTextureManager().bindTexture(machineTexture(tier));
        drawTexturedModalRect(left, top, 0, 0, MACHINE_WIDTH, MACHINE_HEIGHT);

        // Extra Reforked stores the craft-progress strip immediately to the
        // right of the 216 px machine panel in the same 256x256 texture.
        int maxProgress = container.getSyncedMaxProgress();
        if (tile.getProgress() > 0 && maxProgress > 0) {
            float ratio = Math.min(1F, tile.getProgress() / (float) maxProgress);
            int progressWidth = Math.round(11F * ratio);
            if (progressWidth > 0) {
                drawTexturedModalRect(left + 102, top + 41, MACHINE_WIDTH, 0, progressWidth, 37);
            }
        }

        mc.getTextureManager().bindTexture(inventoryTexture(tier));
        int inventoryX = left + (MACHINE_WIDTH - INVENTORY_WIDTH) / 2;
        int inventoryY = top + MACHINE_HEIGHT + INVENTORY_OFFSET_Y;
        drawTexturedModalRect(inventoryX, inventoryY, 0, 0, INVENTORY_WIDTH, INVENTORY_HEIGHT);

        double manaRatio = tile.hasInfiniteMana()
                ? 1D
                : (tile.getManaCapacity() <= 0 ? 0D
                : Math.min(1D, Math.max(0D, tile.getCurrentMana() / (double) tile.getManaCapacity())));
        int manaWidth = (int) Math.round(MANA_BAR_WIDTH * manaRatio);
        if (manaWidth > 0) {
            mc.getTextureManager().bindTexture(tile.hasInfiniteMana() ? INFINITE_MANA_BAR : MANA_BAR);
            drawStandaloneTexture(left + 43, top + 121, manaWidth, 5, manaWidth / (double) MANA_BAR_WIDTH, 1D);
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
            case SAFFRON:
                return GUI_SAFFRON;
            case SHADOW:
                return GUI_SHADOW;
            case CRIMSON:
                return GUI_CRIMSON;
            case MALACHITE:
            default:
                return GUI_MALACHITE;
        }
    }

    private static ResourceLocation inventoryTexture(MachineTier tier) {
        switch (tier) {
            case SAFFRON:
                return INVENTORY_SAFFRON;
            case SHADOW:
                return INVENTORY_SHADOW;
            case CRIMSON:
                return INVENTORY_CRIMSON;
            case MALACHITE:
            default:
                return INVENTORY_MALACHITE;
        }
    }

    private static String displayTier(MachineTier tier) {
        String key = tier.getKey();
        return Character.toUpperCase(key.charAt(0)) + key.substring(1);
    }
}
