package com.nosferatu.divinemachinerylegacy.gui;

import appeng.api.AEApi;
import com.nosferatu.divinemachinerylegacy.item.ItemBloodAltarTierCard;
import com.nosferatu.divinemachinerylegacy.item.ItemBloodMachineUpgrade;
import com.nosferatu.divinemachinerylegacy.tile.TileBloodAltarAssembler;
import com.nosferatu.divinemachinerylegacy.tile.TileBloodAltarAssemblerExtended;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** 1.7.10 menu layout matching bmaddon: 9 patterns, 9 upgrades, player inventory. */
public class ContainerBloodAltarAssembler extends Container {
    private static final int PATTERN_CONTAINER_START = 0;
    private static final int PATTERN_CONTAINER_END = 8;
    private static final int UPGRADE_CONTAINER_START = 9;
    private static final int UPGRADE_CONTAINER_END = 17;
    private static final int MACHINE_VISIBLE_SLOTS = 18;

    private final TileBloodAltarAssembler tile;
    private final boolean hasPatternSlots;

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
        this.hasPatternSlots = tile instanceof TileBloodAltarAssemblerExtended;

        if (hasPatternSlots) {
            TileBloodAltarAssemblerExtended extended = (TileBloodAltarAssemblerExtended) tile;
            for (int col = 0; col < TileBloodAltarAssemblerExtended.PATTERN_SLOT_COUNT; col++) {
                addSlotToContainer(new SlotPattern(extended,
                        TileBloodAltarAssemblerExtended.SLOT_PATTERN_START + col,
                        8 + col * 18, 45));
            }
        } else {
            // Legacy chunks created before the pattern-aware tile existed keep
            // nine harmless placeholder slots offscreen until the block is replaced.
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new SlotDisabled(tile, -1, -1000, -1000));
            }
        }

        // bmaddon screen JSON places all nine upgrade slots in one horizontal row.
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new SlotUpgrade(tile,
                    TileBloodAltarAssembler.SLOT_UPGRADE_START + col,
                    8 + col * 18, 97));
        }

        // Player inventory in the 176x213 AE2-style frame.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 132 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 190));
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
            case 0: clientLife = (clientLife & 0xFFFF0000) | unsigned; break;
            case 1: clientLife = (clientLife & 0x0000FFFF) | (unsigned << 16); break;
            case 2: clientProgress = unsigned; break;
            case 3: clientCraftTime = unsigned; break;
            case 4: clientBatch = unsigned; break;
            default: break;
        }
    }

    public int getSyncedLifeEssence() { return clientLife; }
    public int getSyncedProgress() { return clientProgress; }
    public int getSyncedCraftTime() { return clientCraftTime; }
    public int getSyncedBatch() { return clientBatch; }

    public boolean isPatternContainerSlot(int containerIndex) {
        return hasPatternSlots
                && containerIndex >= PATTERN_CONTAINER_START
                && containerIndex <= PATTERN_CONTAINER_END;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return null;

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        if (index < MACHINE_VISIBLE_SLOTS) {
            if (!mergeItemStack(stack, MACHINE_VISIBLE_SLOTS, inventorySlots.size(), true)) return null;
        } else {
            boolean moved = false;

            if (hasPatternSlots) {
                TileBloodAltarAssemblerExtended extended = (TileBloodAltarAssemblerExtended) tile;
                if (extended.isItemValidForSlot(TileBloodAltarAssemblerExtended.SLOT_PATTERN_START, stack)) {
                    moved = mergeItemStack(stack, PATTERN_CONTAINER_START, PATTERN_CONTAINER_END + 1, false);
                }
            }

            if (!moved && tile.isItemValidForSlot(TileBloodAltarAssembler.SLOT_UPGRADE_START, stack)) {
                moved = mergeItemStack(stack, UPGRADE_CONTAINER_START, UPGRADE_CONTAINER_END + 1, false);
            }

            if (!moved) {
                int playerIndex = index - MACHINE_VISIBLE_SLOTS;
                if (playerIndex < 27) {
                    moved = mergeItemStack(stack, MACHINE_VISIBLE_SLOTS + 27,
                            MACHINE_VISIBLE_SLOTS + 36, false);
                } else {
                    moved = mergeItemStack(stack, MACHINE_VISIBLE_SLOTS,
                            MACHINE_VISIBLE_SLOTS + 27, false);
                }
            }

            if (!moved) return null;
        }

        if (stack.stackSize == 0) slot.putStack(null);
        else slot.onSlotChanged();
        return original;
    }

    private static class SlotPattern extends Slot {
        SlotPattern(TileBloodAltarAssemblerExtended tile, int index, int x, int y) {
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

    private class SlotUpgrade extends Slot {
        SlotUpgrade(TileBloodAltarAssembler tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            if (!inventory.isItemValidForSlot(getSlotIndex(), stack)) return false;
            int max = getMaxInstalled(stack);
            if (max <= 0) return false;
            return countInstalled(stack) < max;
        }

        @Override
        public int getSlotStackLimit() {
            // Matches modern RestrictedInputSlot#setStackLimit(1).
            return 1;
        }

        private int getMaxInstalled(ItemStack candidate) {
            if (candidate == null) return 0;
            Item item = candidate.getItem();
            if (item instanceof ItemBloodAltarTierCard) return 1;
            if (item instanceof ItemBloodMachineUpgrade) {
                ItemBloodMachineUpgrade upgrade = (ItemBloodMachineUpgrade) item;
                return upgrade.getType() == ItemBloodMachineUpgrade.Type.PARALLEL
                        ? TileBloodAltarAssembler.MAX_PARALLEL_CARDS
                        : TileBloodAltarAssembler.MAX_BLOOD_MAGIC_SPEED_CARDS;
            }
            Item aeSpeed = AEApi.instance().definitions().materials().cardSpeed().maybeItem().orNull();
            return aeSpeed != null && item == aeSpeed ? TileBloodAltarAssembler.MAX_AE2_SPEED_CARDS : 0;
        }

        private int countInstalled(ItemStack candidate) {
            int count = 0;
            Item item = candidate.getItem();
            for (int slot = TileBloodAltarAssembler.SLOT_UPGRADE_START;
                 slot <= TileBloodAltarAssembler.SLOT_UPGRADE_END; slot++) {
                ItemStack installed = tile.getStackInSlot(slot);
                if (installed != null && installed.getItem() == item) count += installed.stackSize;
            }
            return count;
        }
    }

    private static class SlotDisabled extends Slot {
        SlotDisabled(TileBloodAltarAssembler tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override public boolean isItemValid(ItemStack stack) { return false; }
        @Override public boolean canTakeStack(EntityPlayer player) { return false; }
    }
}
