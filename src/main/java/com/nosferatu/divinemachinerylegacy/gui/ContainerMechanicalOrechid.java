package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalOrechid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Exact tier slot geometry from Extra Reforked's Mechanical Orechid. */
public class ContainerMechanicalOrechid extends Container {
    private static final int HIDDEN = -1000;
    private final TileMechanicalOrechid tile;
    private int lastMana = Integer.MIN_VALUE;
    private int lastCooldown = Integer.MIN_VALUE;
    private int clientMana;

    public ContainerMechanicalOrechid(InventoryPlayer playerInventory, TileMechanicalOrechid tile) {
        this.tile = tile;
        MachineTier tier = tile.getTier();

        int upLeft = tier == MachineTier.SHADOW || tier == MachineTier.CRIMSON ? 22 : HIDDEN;
        int upRight = tier == MachineTier.SHADOW || tier == MachineTier.CRIMSON ? 178 : HIDDEN;
        int upY = tier == MachineTier.SHADOW || tier == MachineTier.CRIMSON ? 74 : HIDDEN;
        addSlotToContainer(new MachineSlot(tile, 0, upLeft, upY));
        addSlotToContainer(new MachineSlot(tile, 1, upRight, upY));

        int filterCount = tile.getFilterSlotCount();
        int filterX = tier == MachineTier.MALACHITE ? 82 : tier == MachineTier.SAFFRON ? 64 : 46;
        for (int i = 0; i < 7; i++) {
            addSlotToContainer(new MachineSlot(tile, TileMechanicalOrechid.SLOT_FILTER_START + i,
                    i < filterCount ? filterX + i * 18 : HIDDEN,
                    i < filterCount ? 22 : HIDDEN));
        }

        int inputCount = tile.getInputSlotCount();
        int inputX = tier == MachineTier.MALACHITE ? 64 : tier == MachineTier.SAFFRON ? 28
                : tier == MachineTier.SHADOW ? 64 : 46;
        int inputCols = tier == MachineTier.MALACHITE ? 5 : tier == MachineTier.SAFFRON ? 9
                : tier == MachineTier.SHADOW ? 5 : 7;
        for (int i = 0; i < 14; i++) {
            addSlotToContainer(new MachineSlot(tile, TileMechanicalOrechid.SLOT_INPUT_START + i,
                    i < inputCount ? inputX + (i % inputCols) * 18 : HIDDEN,
                    i < inputCount ? 45 + (i / inputCols) * 18 : HIDDEN));
        }

        int outputCount = tile.getOutputSlotCount();
        int outputX = inputX;
        int outputY = tier == MachineTier.MALACHITE || tier == MachineTier.SAFFRON ? 83 : 105;
        for (int i = 0; i < 14; i++) {
            addSlotToContainer(new OutputSlot(tile, TileMechanicalOrechid.SLOT_OUTPUT_START + i,
                    i < outputCount ? outputX + (i % inputCols) * 18 : HIDDEN,
                    i < outputCount ? outputY + (i / inputCols) * 18 : HIDDEN));
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
        return tier == MachineTier.SHADOW || tier == MachineTier.CRIMSON ? 164 : 124;
    }

    @Override public boolean canInteractWith(EntityPlayer player) { return tile.isUseableByPlayer(player); }

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
        int machineSlots = TileMechanicalOrechid.INVENTORY_SIZE;

        if (index < machineSlots) {
            if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) return null;
        } else {
            boolean moved = false;
            for (int i = 0; i < tile.getUpgradeSlotCount() && !moved; i++) {
                int target = TileMechanicalOrechid.SLOT_UPGRADE_START + i;
                if (tile.isItemValidForSlot(target, stack)) moved = mergeItemStack(stack, target, target + 1, false);
            }
            if (!moved && tile.isItemValidForSlot(TileMechanicalOrechid.SLOT_INPUT_START, stack)) {
                moved = mergeItemStack(stack, TileMechanicalOrechid.SLOT_INPUT_START,
                        TileMechanicalOrechid.SLOT_INPUT_START + tile.getInputSlotCount(), false);
            }
            if (!moved && tile.isItemValidForSlot(TileMechanicalOrechid.SLOT_FILTER_START, stack)) {
                moved = mergeItemStack(stack, TileMechanicalOrechid.SLOT_FILTER_START,
                        TileMechanicalOrechid.SLOT_FILTER_START + tile.getFilterSlotCount(), false);
            }
            if (!moved) return null;
        }

        if (stack.stackSize == 0) slot.putStack(null);
        else slot.onSlotChanged();
        return original;
    }

    private static class MachineSlot extends Slot {
        MachineSlot(TileMechanicalOrechid tile, int index, int x, int y) { super(tile, index, x, y); }
        @Override public boolean isItemValid(ItemStack stack) { return inventory.isItemValidForSlot(getSlotIndex(), stack); }
        @Override public int getSlotStackLimit() {
            int i = getSlotIndex();
            return i <= TileMechanicalOrechid.SLOT_FILTER_END ? 1 : super.getSlotStackLimit();
        }
    }

    private static class OutputSlot extends Slot {
        OutputSlot(TileMechanicalOrechid tile, int index, int x, int y) { super(tile, index, x, y); }
        @Override public boolean isItemValid(ItemStack stack) { return false; }
    }
}
