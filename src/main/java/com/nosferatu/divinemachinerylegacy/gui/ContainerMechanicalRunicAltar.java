package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMechanicalRunicAltar extends Container {

    private static final int HIDDEN_SLOT = -1000;

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
        MachineTier tier = tile.getTier();

        // Match Extra Reforked's four original Runic Altar GUI layouts.
        int[][] livingrock = livingrockCoordinates(tier);
        for (int i = 0; i < 3; i++) {
            addSlotToContainer(new SlotLivingrock(tile, i, livingrock[i][0], livingrock[i][1]));
        }

        int[][] upgrades = upgradeCoordinates(tier);
        addSlotToContainer(new SlotUpgrade(tile, TileMechanicalRunicAltar.SLOT_UPGRADE_START,
                upgrades[0][0], upgrades[0][1]));
        addSlotToContainer(new SlotUpgrade(tile, TileMechanicalRunicAltar.SLOT_UPGRADE_START + 1,
                upgrades[1][0], upgrades[1][1]));

        // Original 4x4 input grid.
        int index = TileMechanicalRunicAltar.SLOT_INPUT_START;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                addSlotToContainer(new Slot(tile, index++, 27 + col * 18, 25 + row * 18));
            }
        }

        // Original 4x4 output grid.
        index = TileMechanicalRunicAltar.SLOT_OUTPUT_START;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                addSlotToContainer(new SlotOutput(tile, index++, 119 + col * 18, 25 + row * 18));
            }
        }

        // Extra Reforked centers its 188x95 inventory module under a 216x140
        // machine panel. These are the corresponding slot coordinates.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 28 + col * 18, 154 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 28 + col * 18, 212));
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

    private static int[][] livingrockCoordinates(MachineTier tier) {
        if (tier == MachineTier.MALACHITE) {
            return new int[][]{{100, 100}, {HIDDEN_SLOT, HIDDEN_SLOT}, {HIDDEN_SLOT, HIDDEN_SLOT}};
        }
        return new int[][]{{81, 100}, {100, 100}, {119, 100}};
    }

    private static int[][] upgradeCoordinates(MachineTier tier) {
        if (tier == MachineTier.SHADOW) {
            return new int[][]{{45, 100}, {HIDDEN_SLOT, HIDDEN_SLOT}};
        }
        if (tier == MachineTier.CRIMSON) {
            return new int[][]{{45, 100}, {155, 100}};
        }
        return new int[][]{{HIDDEN_SLOT, HIDDEN_SLOT}, {HIDDEN_SLOT, HIDDEN_SLOT}};
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
