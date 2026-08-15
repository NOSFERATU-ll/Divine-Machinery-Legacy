package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.tile.TileBloodAltarAssembler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

public class GuiBloodAltarAssembler extends GuiContainer {
    private final TileBloodAltarAssembler tile;
    private final ContainerBloodAltarAssembler container;

    public GuiBloodAltarAssembler(InventoryPlayer playerInventory, TileBloodAltarAssembler tile) {
        this(new ContainerBloodAltarAssembler(playerInventory, tile), tile);
    }

    private GuiBloodAltarAssembler(ContainerBloodAltarAssembler container, TileBloodAltarAssembler tile) {
        super(container);
        this.container = container;
        this.tile = tile;
        this.xSize = 246;
        this.ySize = 211;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = StatCollector.translateToLocal("container.divinemachinerylegacy.blood_altar_assembler");
        fontRendererObj.drawString(title, 8, 7, 0x404040);
        fontRendererObj.drawString(StatCollector.translateToLocal("gui.divinemachinerylegacy.blood_altar_assembler.upgrades"),
                10, 17, 0x505050);
        fontRendererObj.drawString(StatCollector.translateToLocal("gui.divinemachinerylegacy.blood_altar_assembler.inputs"),
                82, 17, 0x505050);
        fontRendererObj.drawString(StatCollector.translateToLocal("gui.divinemachinerylegacy.blood_altar_assembler.outputs"),
                166, 17, 0x505050);

        String tier = StatCollector.translateToLocalFormatted(
                "gui.divinemachinerylegacy.blood_altar_assembler.tier", tile.getAltarTier());
        String parallel = StatCollector.translateToLocalFormatted(
                "gui.divinemachinerylegacy.blood_altar_assembler.parallel", tile.getMaxParallelCrafts());
        fontRendererObj.drawString(tier, 82, 76, 0x404040);
        fontRendererObj.drawString(parallel, 166, 76, 0x404040);

        int life = container.getSyncedLifeEssence();
        fontRendererObj.drawString(life + " / " + TileBloodAltarAssembler.LIFE_ESSENCE_CAPACITY + " LP", 82, 91, 0x7A1111);

        int batch = container.getSyncedBatch();
        if (batch > 0) {
            fontRendererObj.drawString(StatCollector.translateToLocalFormatted(
                    "gui.divinemachinerylegacy.blood_altar_assembler.batch", batch), 166, 91, 0x404040);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        int left = guiLeft;
        int top = guiTop;
        drawRect(left, top, left + xSize, top + ySize, 0xFFC6C6C6);
        drawRect(left + 4, top + 4, left + xSize - 4, top + 112, 0xFFDEDEDE);
        drawRect(left + 4, top + 116, left + xSize - 4, top + ySize - 4, 0xFFDEDEDE);

        // Slot wells.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) drawSlotWell(left + 11 + col * 18, top + 25 + row * 18);
        }
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                drawSlotWell(left + 81 + col * 18, top + 34 + row * 18);
                drawSlotWell(left + 165 + col * 18, top + 34 + row * 18);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) drawSlotWell(left + 38 + col * 18, top + 128 + row * 18);
        }
        for (int col = 0; col < 9; col++) drawSlotWell(left + 38 + col * 18, top + 186);

        // Life Essence buffer.
        drawRect(left + 70, top + 88, left + 232, top + 100, 0xFF6F6F6F);
        int life = Math.max(0, Math.min(TileBloodAltarAssembler.LIFE_ESSENCE_CAPACITY,
                container.getSyncedLifeEssence()));
        int lifeWidth = (int) (160L * life / TileBloodAltarAssembler.LIFE_ESSENCE_CAPACITY);
        if (lifeWidth > 0) drawRect(left + 71, top + 89, left + 71 + lifeWidth, top + 99, 0xFF8F1616);

        // Current craft progress.
        drawRect(left + 70, top + 103, left + 232, top + 111, 0xFF6F6F6F);
        int max = container.getSyncedCraftTime();
        int progress = container.getSyncedProgress();
        if (max > 0 && progress > 0) {
            int width = Math.min(160, (int) (160L * progress / max));
            drawRect(left + 71, top + 104, left + 71 + width, top + 110, 0xFFB52C2C);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    private void drawSlotWell(int x, int y) {
        drawRect(x, y, x + 18, y + 18, 0xFF777777);
        drawRect(x + 1, y + 1, x + 17, y + 17, 0xFFEEEEEE);
    }
}
