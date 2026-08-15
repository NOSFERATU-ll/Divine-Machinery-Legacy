package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.tile.TileBloodGenerator;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

import java.util.Collections;

/** 1.7.10 rendering of bmaddon's original Blood Generator GUI. */
public class GuiBloodGenerator extends GuiContainer {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            DivineMachineryLegacy.MODID, "textures/gui/blood_generator.png");

    private final ContainerBloodGenerator container;

    public GuiBloodGenerator(InventoryPlayer playerInventory, TileBloodGenerator tile) {
        this(new ContainerBloodGenerator(playerInventory, tile));
    }

    private GuiBloodGenerator(ContainerBloodGenerator container) {
        super(container);
        this.container = container;
        this.xSize = 212;
        this.ySize = 186;
    }

    @Override
    public void initGui() {
        super.initGui();
        // bmaddon deliberately offsets this GUI ten pixels to the left.
        this.guiLeft -= 10;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Original bmaddon draws no normal foreground text; labels are part of the texture.
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        int capacity = container.getBloodCapacity();
        int amount = container.getBloodAmount();
        if (capacity > 0 && amount > 0) {
            float ratio = Math.max(0F, Math.min(1F, (float) amount / (float) capacity));
            int fillWidth = Math.round(129F * ratio);
            int indicatorWidth = Math.max(5, 5 + fillWidth);
            drawTexturedModalRect(guiLeft + 41, guiTop + 78, 0, 186, indicatorWidth, 5);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (isMouseOverBloodBar(mouseX, mouseY)) {
            String text = StatCollector.translateToLocalFormatted(
                    "tooltip.divinemachinerylegacy.blood_generator.blood",
                    container.getBloodAmount(), container.getBloodCapacity());
            drawHoveringText(Collections.singletonList(text), mouseX, mouseY, fontRendererObj);
        }
    }

    private boolean isMouseOverBloodBar(int mouseX, int mouseY) {
        int x = guiLeft + 46;
        int y = guiTop + 79;
        return mouseX >= x && mouseX < x + 130 && mouseY >= y && mouseY < y + 6;
    }
}
