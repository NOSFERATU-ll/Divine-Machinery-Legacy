package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.tile.TileJadedAmaranthus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Exact slot layout from Extra Reforked's Jaded Amaranthus. */
public class ContainerJadedAmaranthus extends Container {
    private final TileJadedAmaranthus tile;
    private int lastMana = Integer.MIN_VALUE;
    private int lastCooldown = Integer.MIN_VALUE;
    private int clientMana;

    public ContainerJadedAmaranthus(InventoryPlayer playerInventory, TileJadedAmaranthus tile) {
        this.tile = tile;

        addSlotToContainer(new UpgradeSlot(tile, 0, 72, 23));
        addSlotToContainer(new UpgradeSlot(tile, 1, 108, 23));

        for (int i = 0; i < 16; i++) {
            addSlotToContainer(new OutputSlot(tile, TileJadedAmaranthus.SLOT_OUTPUT_START + i,
                    27 + (i % 8) * 18, 49 + (i / 8) * 18));
        }

        // Reforked OTHER inventory module: first player slot = (18, 138).
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9,
                        18 + col * 18, 138 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 18 + col * 18, 196));
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
        for (Object object : crafters) {
            ICrafting crafter = (ICrafting) object;
            if (mana != lastMana) {
                crafter.sendProgressBarUpdate(this, 0, mana & 0xFFFF);
                crafter.sendProgressBarUpdate(this, 1, (mana >>> 16) & 0xFFFF);
            }
            if (cooldown != lastCooldown) crafter.sendProgressBarUpdate(this, 2, cooldown);
        }
        lastMana = mana;
        lastCooldown = cooldown;
    }

    @Override
    public void updateProgressBar(int id, int value) {
        int unsigned = value & 0xFFFF;
        if (id == 0) {
            clientMana = (clientMana & 0xFFFF0000) | unsigned;
            tile.setClientMana(clientMana);
        } else if (id == 1) {
            clientMana = (clientMana & 0x0000FFFF) | (unsigned << 16);
            tile.setClientMana(clientMana);
        } else if (id == 2) {
            tile.setClientCooldown(unsigned);
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return null;
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        int machineSlots = TileJadedAmaranthus.INVENTORY_SIZE;

        if (index < machineSlots) {
            if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) return null;
        } else {
            boolean moved = false;
            for (int target = TileJadedAmaranthus.SLOT_UPGRADE_START;
                 target <= TileJadedAmaranthus.SLOT_UPGRADE_END && !moved; target++) {
                if (tile.isItemValidForSlot(target, stack)) {
                    moved = mergeItemStack(stack, target, target + 1, false);
                }
            }
            if (!moved) return null;
        }

        if (stack.stackSize == 0) slot.putStack(null);
        else slot.onSlotChanged();
        return original;
    }

    private static class UpgradeSlot extends Slot {
        UpgradeSlot(TileJadedAmaranthus tile, int index, int x, int y) { super(tile, index, x, y); }
        @Override public boolean isItemValid(ItemStack stack) { return inventory.isItemValidForSlot(getSlotIndex(), stack); }
        @Override public int getSlotStackLimit() { return 1; }
    }

    private static class OutputSlot extends Slot {
        OutputSlot(TileJadedAmaranthus tile, int index, int x, int y) { super(tile, index, x, y); }
        @Override public boolean isItemValid(ItemStack stack) { return false; }
    }
}
