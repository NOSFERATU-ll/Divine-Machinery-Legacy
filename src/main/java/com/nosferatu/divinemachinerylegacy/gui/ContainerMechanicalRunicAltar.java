package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMechanicalRunicAltar extends Container {

    private final TileMechanicalRunicAltar tile;

    private int lastMana = Integer.MIN_VALUE;
    private int lastProgress = Integer.MIN_VALUE;
    private int lastMaxProgress = Integer.MIN_VALUE;
    private int lastBatch = Integer.MIN_VALUE;

    private int clientMana;
    private int clientProgress;
    private int clientMaxProgress;
    private int clientBatch;

    public ContainerMechanicalRunicAltar(InventoryPlayer playerInventory, TileMechanicalRunicAltar tile) {
        this.tile = tile;

        // Livingrock slots.
        for (int i = 0; i < 3; i++) {
            addSlotToContainer(new SlotLivingrock(tile, i, 8 + i * 18, 22));
        }

        // Upgrade slots. They are only valid on Shadow (1) / Crimson (2).
        addSlotToContainer(new SlotUpgrade(tile, TileMechanicalRunicAltar.SLOT_UPGRADE_START, 82, 22));
        addSlotToContainer(new SlotUpgrade(tile, TileMechanicalRunicAltar.SLOT_UPGRADE_START + 1, 100, 22));

        // 4x4 input area.
        int index = TileMechanicalRunicAltar.SLOT_INPUT_START;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                addSlotToContainer(new Slot(tile, index++, 8 + col * 18, 54 + row * 18));
            }
        }

        // 4x4 output area.
        index = TileMechanicalRunicAltar.SLOT_OUTPUT_START;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                addSlotToContainer(new SlotOutput(tile, index++, 128 + col * 18, 54 + row * 18));
            }
        }

        // Player inventory.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 29 + col * 18, 151 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 29 + col * 18, 209));
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
        int progress = tile.getProgress();
        int maxProgress = tile.getMaxProgress();
        int batch = tile.getCurrentBatch();

        for (Object object : crafters) {
            ICrafting crafter = (ICrafting) object;
            if (mana != lastMana) {
                crafter.sendProgressBarUpdate(this, 0, mana & 0xFFFF);
                crafter.sendProgressBarUpdate(this, 1, (mana >>> 16) & 0xFFFF);
            }
            if (progress != lastProgress) {
                crafter.sendProgressBarUpdate(this, 2, progress & 0xFFFF);
                crafter.sendProgressBarUpdate(this, 3, (progress >>> 16) & 0xFFFF);
            }
            if (maxProgress != lastMaxProgress) {
                crafter.sendProgressBarUpdate(this, 4, maxProgress & 0xFFFF);
                crafter.sendProgressBarUpdate(this, 5, (maxProgress >>> 16) & 0xFFFF);
            }
            if (batch != lastBatch) {
                crafter.sendProgressBarUpdate(this, 6, batch);
            }
        }

        lastMana = mana;
        lastProgress = progress;
        lastMaxProgress = maxProgress;
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
                clientProgress = (clientProgress & 0xFFFF0000) | unsigned;
                tile.setClientProgress(clientProgress);
                break;
            case 3:
                clientProgress = (clientProgress & 0x0000FFFF) | (unsigned << 16);
                tile.setClientProgress(clientProgress);
                break;
            case 4:
                clientMaxProgress = (clientMaxProgress & 0xFFFF0000) | unsigned;
                break;
            case 5:
                clientMaxProgress = (clientMaxProgress & 0x0000FFFF) | (unsigned << 16);
                break;
            case 6:
                clientBatch = unsigned;
                tile.setClientBatch(clientBatch);
                break;
            default:
                break;
        }
    }

    public int getSyncedMaxProgress() {
        return clientMaxProgress;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack original = null;
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return null;

        ItemStack stack = slot.getStack();
        original = stack.copy();

        final int machineSlots = 37; // 3 livingrock + 2 upgrades + 16 inputs + 16 outputs.
        if (index < machineSlots) {
            if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) return null;
        } else {
            boolean moved;
            if (stack.getItem() == DivineMachineryLegacy.catalystManaInfinity
                    || stack.getItem() == DivineMachineryLegacy.catalystLivingrockInfinity) {
                moved = mergeItemStack(stack, 3, 5, false);
            } else if (tile.isItemValidForSlot(TileMechanicalRunicAltar.SLOT_LIVINGROCK_START, stack)) {
                moved = mergeItemStack(stack, 0, 3, false);
            } else {
                moved = mergeItemStack(stack, 5, 21, false);
            }
            if (!moved) return null;
        }

        if (stack.stackSize == 0) slot.putStack(null);
        else slot.onSlotChanged();
        return original;
    }

    private static class SlotLivingrock extends Slot {
        SlotLivingrock(TileMechanicalRunicAltar tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return inventory.isItemValidForSlot(getSlotIndex(), stack);
        }
    }

    private static class SlotUpgrade extends Slot {
        SlotUpgrade(TileMechanicalRunicAltar tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return inventory.isItemValidForSlot(getSlotIndex(), stack);
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }

    private static class SlotOutput extends Slot {
        SlotOutput(TileMechanicalRunicAltar tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }
    }
}
