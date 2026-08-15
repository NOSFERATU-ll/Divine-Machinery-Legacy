package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternData;
import com.nosferatu.divinemachinerylegacy.tile.TileBloodAltarAssembler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

/**
 * 1.7.10 recreation of bmaddon's AE2 screen definition:
 * 176x213, nine horizontal Blood Pattern slots at y=45 and nine upgrades at y=97.
 */
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
        this.xSize = 176;
        this.ySize = 213;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(
                StatCollector.translateToLocal("container.divinemachinerylegacy.blood_altar_assembler"),
                8, 6, 0x404040);
        fontRendererObj.drawString(
                StatCollector.translateToLocal("gui.divinemachinerylegacy.blood_altar_assembler.patterns"),
                8, 34, 0x404040);
        fontRendererObj.drawString(
                StatCollector.translateToLocal("gui.divinemachinerylegacy.blood_altar_assembler.tier_cards"),
                8, 86, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        int left = guiLeft;
        int top = guiTop;
        drawRect(left, top, left + xSize, top + ySize, 0xFFC6C6C6);
        drawRect(left + 4, top + 4, left + xSize - 4, top + 120, 0xFFDEDEDE);
        drawRect(left + 4, top + 124, left + xSize - 4, top + ySize - 4, 0xFFDEDEDE);

        // Pattern row from assets/ae2/screens/blood_altar_assembler.json.
        for (int col = 0; col < 9; col++) {
            drawSlotWell(left + 7 + col * 18, top + 44);
        }

        // Upgrade row from the same screen definition.
        for (int col = 0; col < 9; col++) {
            drawSlotWell(left + 7 + col * 18, top + 96);
        }

        // Player inventory.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotWell(left + 7 + col * 18, top + 131 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotWell(left + 7 + col * 18, top + 189);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        // bmaddon replaces a Blood Pattern with its output preview while Shift
        // is held. In 1.7 GuiContainer's slot renderer is private, so redraw the
        // nine slot wells and outputs after the vanilla pass to get the same result.
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            renderShiftPatternPreviews();
        }
    }

    private void renderShiftPatternPreviews() {
        for (int containerIndex = 0; containerIndex < 9; containerIndex++) {
            if (!container.isPatternContainerSlot(containerIndex)) continue;
            Slot slot = (Slot) inventorySlots.inventorySlots.get(containerIndex);
            if (slot == null || !slot.getHasStack()) continue;

            ItemStack pattern = slot.getStack();
            if (!BloodMagicPatternData.isEncoded(pattern)) continue;
            ItemStack output = BloodMagicPatternData.getOutput(pattern);
            if (output == null) continue;

            int x = guiLeft + slot.xDisplayPosition;
            int y = guiTop + slot.yDisplayPosition;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            drawRect(x, y, x + 16, y + 16, 0xFFEEEEEE);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1F, 1F, 1F, 1F);

            itemRender.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), output, x, y);
            itemRender.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), output, x, y);
        }
    }

    private void drawSlotWell(int x, int y) {
        drawRect(x, y, x + 18, y + 18, 0xFF777777);
        drawRect(x + 1, y + 1, x + 17, y + 17, 0xFFEEEEEE);
    }
}
