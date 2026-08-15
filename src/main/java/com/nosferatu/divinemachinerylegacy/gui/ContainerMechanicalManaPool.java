package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalManaPool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMechanicalManaPool extends Container {

    private static final int HIDDEN = -1000;
    private final TileMechanicalManaPool tile;

    private int lastMana = Integer.MIN_VALUE;
    private int lastCooldown = Integer.MIN_VALUE;
    private int lastBatch = Integer.MIN_VALUE;
    private int clientMana;

    public ContainerMechanicalManaPool(InventoryPlayer playerInventory, TileMechanicalManaPool tile) {
        this.tile = tile;
        MachineTier tier = tile.getTier();
        int offset = tier == MachineTier.CRIMSON ? 0 : 10;

        int catalystX = offset + (tier == MachineTier.CRIMSON ? 100 : 98);
        int catalystY = tier == MachineTier.SHADOW ? 72 : (tier == MachineTier.CRIMSON ? 78 : 76);
        addSlotToContainer(new MachineSlot(tile, TileMechanicalManaPool.SLOT_CATALYST, catalystX, catalystY));

        int upgradeX = offset + (tier == MachineTier.CRIMSON ? 100 : 98);
        int upgradeY = tier == MachineTier.CRIMSON ? 42 : 36;
        addSlotToContainer(new MachineSlot(tile, TileMechanicalManaPool.SLOT_UPGRADE,
                tile.hasUpgradeSlot() ? upgradeX : HIDDEN,
                tile.hasUpgradeSlot() ? upgradeY : HIDDEN));

        int inputCount = tile.getInputSlotCount();
        int inputCols = 3;
        int inputStartX = offset + (tier == MachineTier.CRIMSON ? 34 : 33);
        int inputStartY;
        if (tier == MachineTier.MALACHITE) inputStartY = 54;
        else if (tier == MachineTier.CRIMSON) inputStartY = 32;
        else inputStartY = 36;

        for (int i = 0; i < 12; i++) {
            int x = HIDDEN;
            int y = HIDDEN;
            if (i < inputCount) {
                x = inputStartX + (i % inputCols) * 18;
                y = inputStartY + (i / inputCols) * 18;
            }
            addSlotToContainer(new MachineSlot(tile, TileMechanicalManaPool.SLOT_INPUT_START + i, x, y));
        }

        int outputCount = tile.getOutputSlotCount();
        int outputCols = tier == MachineTier.CRIMSON ? 3 : 2;
        int outputStartX = offset + (tier == MachineTier.CRIMSON ? 129 : 128);
        int outputStartY;
        if (tier == MachineTier.MALACHITE) outputStartY = 54;
        else if (tier == MachineTier.CRIMSON) outputStartY = 42;
        else outputStartY = 36;

        for (int i = 0; i < 9; i++) {
            int x = HIDDEN;
            int y = HIDDEN;
            if (i < outputCount) {
                x = outputStartX + (i % outputCols) * 18;
                y = outputStartY + (i / outputCols) * 18;
            }
            addSlotToContainer(new OutputSlot(tile, TileMechanicalManaPool.SLOT_OUTPUT_START + i, x, y));
        }

        // Reforked inventory module, centered under the widest machine GUI.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 28 + col * 18, 153 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 28 + col * 18, 211));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int mana = tile.getCurrentMana();
        int cooldown = tile.getCooldown();
        int batch = tile.getLastBatch();

        for (Object object : crafters) {
            ICrafting crafter = (ICrafting) object;
            if (mana != lastMana) {
                crafter.sendProgressBarUpdate(this, 0, mana & 0xFFFF);
                crafter.sendProgressBarUpdate(this, 1, (mana >>> 16) & 0xFFFF);
            }
            if (cooldown != lastCooldown) crafter.sendProgressBarUpdate(this, 2, cooldown);
            if (batch != lastBatch) crafter.sendProgressBarUpdate(this, 3, batch);
        }

        lastMana = mana;
        lastCooldown = cooldown;
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
                tile.setClientCooldown(unsigned);
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
        final int machineSlots = TileMechanicalManaPool.INVENTORY_SIZE;

        if (index < machineSlots) {
            if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) return null;
        } else {
            boolean moved;
            if (tile.isItemValidForSlot(TileMechanicalManaPool.SLOT_CATALYST, stack)) {
                moved = mergeItemStack(stack, 0, 1, false);
            } else if (tile.isItemValidForSlot(TileMechanicalManaPool.SLOT_UPGRADE, stack)) {
                moved = mergeItemStack(stack, 1, 2, false);
            } else {
                moved = mergeItemStack(stack,
                        TileMechanicalManaPool.SLOT_INPUT_START,
                        TileMechanicalManaPool.SLOT_INPUT_START + tile.getInputSlotCount(), false);
            }
            if (!moved) return null;
        }

        if (stack.stackSize == 0) slot.putStack(null);
        else slot.onSlotChanged();
        return original;
    }

    private static class MachineSlot extends Slot {
        MachineSlot(TileMechanicalManaPool tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return inventory.isItemValidForSlot(getSlotIndex(), stack);
        }
    }

    private static class OutputSlot extends Slot {
        OutputSlot(TileMechanicalManaPool tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}
