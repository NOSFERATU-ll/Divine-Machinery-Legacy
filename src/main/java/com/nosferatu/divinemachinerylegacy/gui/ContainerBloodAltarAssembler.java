package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.tile.TileBloodAltarAssembler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBloodAltarAssembler extends Container {
    private final TileBloodAltarAssembler tile;

    private int lastLife = Integer.MIN_VALUE;
    private int lastProgress = Integer.MIN_VALUE;
    private int lastCraftTime = Integer.MIN_VALUE;
    private int lastBatch = Integer.MIN_VALUE;

    private int clientLife;
    private int clientProgress;
    private int clientCraftTime;
    private int clientBatch;

    public ContainerBloodAltarAssembler(InventoryPlayer playerInventory, TileBloodAltarAssembler tile) {
        this.tile = tile;

        // 3x3 upgrade grid, matching bmaddon's nine upgrade slots.
        int slot = TileBloodAltarAssembler.SLOT_UPGRADE_START;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlotToContainer(new SlotUpgrade(tile, slot++, 12 + col * 18, 26 + row * 18));
            }
        }

        // 4x2 input queue.
        slot = TileBloodAltarAssembler.SLOT_INPUT_START;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                addSlotToContainer(new Slot(tile, slot++, 82 + col * 18, 35 + row * 18));
            }
        }

        // 4x2 output queue.
        slot = TileBloodAltarAssembler.SLOT_OUTPUT_START;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                addSlotToContainer(new SlotOutput(tile, slot++, 166 + col * 18, 35 + row * 18));
            }
        }

        // Player inventory.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 39 + col * 18, 129 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 39 + col * 18, 187));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int life = tile.getLifeEssence();
        int progress = tile.getProgressTicks();
        int craftTime = tile.getCraftTimeTicks();
        int batch = tile.getActiveBatch();

        for (Object object : crafters) {
            ICrafting crafter = (ICrafting) object;
            if (life != lastLife) {
                crafter.sendProgressBarUpdate(this, 0, life & 0xFFFF);
                crafter.sendProgressBarUpdate(this, 1, (life >>> 16) & 0xFFFF);
            }
            if (progress != lastProgress) crafter.sendProgressBarUpdate(this, 2, progress);
            if (craftTime != lastCraftTime) crafter.sendProgressBarUpdate(this, 3, craftTime);
            if (batch != lastBatch) crafter.sendProgressBarUpdate(this, 4, batch);
        }

        lastLife = life;
        lastProgress = progress;
        lastCraftTime = craftTime;
        lastBatch = batch;
    }

    @Override
    public void updateProgressBar(int id, int value) {
        int unsigned = value & 0xFFFF;
        switch (id) {
            case 0:
                clientLife = (clientLife & 0xFFFF0000) | unsigned;
                break;
            case 1:
                clientLife = (clientLife & 0x0000FFFF) | (unsigned << 16);
                break;
            case 2:
                clientProgress = unsigned;
                break;
            case 3:
                clientCraftTime = unsigned;
                break;
            case 4:
                clientBatch = unsigned;
                break;
            default:
                break;
        }
    }

    public int getSyncedLifeEssence() {
        return clientLife;
    }

    public int getSyncedProgress() {
        return clientProgress;
    }

    public int getSyncedCraftTime() {
        return clientCraftTime;
    }

    public int getSyncedBatch() {
        return clientBatch;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return null;

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        final int machineSlots = 25; // hidden pending-output slot is intentionally not exposed.

        if (index < machineSlots) {
            if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) return null;
        } else {
            boolean moved = false;
            for (int upgradeSlot = TileBloodAltarAssembler.SLOT_UPGRADE_START;
                 upgradeSlot <= TileBloodAltarAssembler.SLOT_UPGRADE_END && !moved;
                 upgradeSlot++) {
                if (tile.isItemValidForSlot(upgradeSlot, stack)) {
                    moved = mergeItemStack(stack, 0, 9, false);
                }
            }
            if (!moved) moved = mergeItemStack(stack, 9, 17, false);
            if (!moved) return null;
        }

        if (stack.stackSize == 0) slot.putStack(null);
        else slot.onSlotChanged();
        return original;
    }

    private static class SlotUpgrade extends Slot {
        SlotUpgrade(TileBloodAltarAssembler tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return inventory.isItemValidForSlot(getSlotIndex(), stack);
        }
    }

    private static class SlotOutput extends Slot {
        SlotOutput(TileBloodAltarAssembler tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}
