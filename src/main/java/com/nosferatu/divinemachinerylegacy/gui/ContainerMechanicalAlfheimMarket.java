package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalAlfheimMarket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Slot layout matching Extra Reforked's four Alfheim Market tiers. */
public class ContainerMechanicalAlfheimMarket extends Container {
    private static final int HIDDEN = -1000;
    private final TileMechanicalAlfheimMarket tile;

    private int lastMana = Integer.MIN_VALUE;
    private int lastProgress = Integer.MIN_VALUE;
    private int lastBatch = Integer.MIN_VALUE;
    private int clientMana;

    public ContainerMechanicalAlfheimMarket(InventoryPlayer playerInventory, TileMechanicalAlfheimMarket tile) {
        this.tile = tile;
        MachineTier tier = tile.getTier();

        addSlotToContainer(new MachineSlot(tile, TileMechanicalAlfheimMarket.SLOT_UPGRADE,
                tile.hasUpgradeSlot() ? 100 : HIDDEN,
                tile.hasUpgradeSlot() ? (tier == MachineTier.CRIMSON ? 96 : 81) : HIDDEN));

        int inputCount = tile.getInputSlotCount();
        int inputCols = tier == MachineTier.MALACHITE ? 1 : tier == MachineTier.SAFFRON ? 2 : 3;
        int inputRows = tier == MachineTier.CRIMSON ? 4 : 3;
        int inputX = tier == MachineTier.MALACHITE ? 75 : tier == MachineTier.SAFFRON ? 57 : 39;
        int inputY = tier == MachineTier.CRIMSON ? 34 : 36;

        for (int i = 0; i < 12; i++) {
            int x = HIDDEN, y = HIDDEN;
            if (i < inputCount) {
                x = inputX + (i % inputCols) * 18;
                y = inputY + (i / inputCols) * 18;
            }
            addSlotToContainer(new MachineSlot(tile, TileMechanicalAlfheimMarket.SLOT_INPUT_START + i, x, y));
        }

        int outputCount = tile.getOutputSlotCount();
        int outputCols = inputCols;
        int outputX = 125;
        int outputY = tier == MachineTier.CRIMSON ? 34 : 36;
        for (int i = 0; i < 12; i++) {
            int x = HIDDEN, y = HIDDEN;
            if (i < outputCount) {
                x = outputX + (i % outputCols) * 18;
                y = outputY + (i / outputCols) * 18;
            }
            addSlotToContainer(new OutputSlot(tile, TileMechanicalAlfheimMarket.SLOT_OUTPUT_START + i, x, y));
        }

        int playerY = machineHeight(tier) + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9,
                        28 + col * 18, playerY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 28 + col * 18, playerY + 58));
        }
    }

    private static int machineHeight(MachineTier tier) {
        return tier == MachineTier.CRIMSON ? 140 : 124;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int mana = tile.getCurrentMana();
        int progress = tile.getProgress();
        int batch = tile.getActiveBatch();

        for (Object object : crafters) {
            ICrafting crafter = (ICrafting) object;
            if (mana != lastMana) {
                crafter.sendProgressBarUpdate(this, 0, mana & 0xFFFF);
                crafter.sendProgressBarUpdate(this, 1, (mana >>> 16) & 0xFFFF);
            }
            if (progress != lastProgress) crafter.sendProgressBarUpdate(this, 2, progress);
            if (batch != lastBatch) crafter.sendProgressBarUpdate(this, 3, batch);
        }
        lastMana = mana;
        lastProgress = progress;
        lastBatch = batch;
    }

    @Override
    public void updateProgressBar(int id, int value) {
        int unsigned = value & 0xFFFF;
        switch (id) {
            case 0:
                clientMana = (clientMana & 0xFFFF0000) | unsigned;
                tile.setClientMana(clientMana);
                break;
            case 1:
                clientMana = (clientMana & 0x0000FFFF) | (unsigned << 16);
                tile.setClientMana(clientMana);
                break;
            case 2:
                tile.setClientProgress(unsigned);
                break;
            case 3:
                tile.setClientBatch(unsigned);
                break;
            default:
                break;
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return null;

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        int machineSlots = TileMechanicalAlfheimMarket.INVENTORY_SIZE;

        if (index < machineSlots) {
            if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) return null;
        } else {
            boolean moved = false;
            if (tile.isItemValidForSlot(TileMechanicalAlfheimMarket.SLOT_UPGRADE, stack)) {
                moved = mergeItemStack(stack,
                        TileMechanicalAlfheimMarket.SLOT_UPGRADE,
                        TileMechanicalAlfheimMarket.SLOT_UPGRADE + 1, false);
            }
            if (!moved) {
                moved = mergeItemStack(stack,
                        TileMechanicalAlfheimMarket.SLOT_INPUT_START,
                        TileMechanicalAlfheimMarket.SLOT_INPUT_START + tile.getInputSlotCount(), false);
            }
            if (!moved) return null;
        }

        if (stack.stackSize == 0) slot.putStack(null);
        else slot.onSlotChanged();
        return original;
    }

    private static class MachineSlot extends Slot {
        MachineSlot(TileMechanicalAlfheimMarket tile, int index, int x, int y) {
            super(tile, index, x, y);
        }
        @Override public boolean isItemValid(ItemStack stack) {
            return inventory.isItemValidForSlot(getSlotIndex(), stack);
        }
        @Override public int getSlotStackLimit() {
            return getSlotIndex() == TileMechanicalAlfheimMarket.SLOT_UPGRADE ? 1 : super.getSlotStackLimit();
        }
    }

    private static class OutputSlot extends Slot {
        OutputSlot(TileMechanicalAlfheimMarket tile, int index, int x, int y) {
            super(tile, index, x, y);
        }
        @Override public boolean isItemValid(ItemStack stack) { return false; }
    }
}
