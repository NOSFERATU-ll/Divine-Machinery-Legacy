package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalIndustrialAgglomerationFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Tier-faithful slot layout from Extra Reforked's Industrial Agglomeration Factory. */
public class ContainerMechanicalIndustrialAgglomerationFactory extends Container {
    private static final int HIDDEN = -1000;
    private final TileMechanicalIndustrialAgglomerationFactory tile;

    private int lastMana = Integer.MIN_VALUE;
    private int lastProgress = Integer.MIN_VALUE;
    private int lastBatch = Integer.MIN_VALUE;
    private int clientMana;
    private int clientProgress;

    public ContainerMechanicalIndustrialAgglomerationFactory(InventoryPlayer playerInventory,
                                                              TileMechanicalIndustrialAgglomerationFactory tile) {
        this.tile = tile;
        MachineTier tier = tile.getTier();
        int offset = tier == MachineTier.CRIMSON ? 0 : 10;

        // Reforked gives both Shadow and Crimson two upgrade slots for this machine.
        int upgradeY = tier == MachineTier.CRIMSON ? 69 : 61;
        int upgradeLeft = tier == MachineTier.CRIMSON ? 17 : 17 + offset;
        int upgradeRight = tier == MachineTier.CRIMSON ? 183 : 163 + offset;
        addSlotToContainer(new UpgradeSlot(tile, 0,
                tile.getUpgradeSlotCount() > 0 ? upgradeLeft : HIDDEN,
                tile.getUpgradeSlotCount() > 0 ? upgradeY : HIDDEN));
        addSlotToContainer(new UpgradeSlot(tile, 1,
                tile.getUpgradeSlotCount() > 1 ? upgradeRight : HIDDEN,
                tile.getUpgradeSlotCount() > 1 ? upgradeY : HIDDEN));

        int inputCount = tile.getInputSlotCount();
        for (int i = 0; i < 14; i++) {
            int x = HIDDEN, y = HIDDEN;
            if (i < inputCount) {
                if (tier == MachineTier.CRIMSON) {
                    x = 46 + (i % 7) * 18;
                    y = 33 + (i / 7) * 18;
                } else {
                    int startX = tier == MachineTier.MALACHITE ? 72
                            : tier == MachineTier.SAFFRON ? 54 : 36;
                    x = offset + startX + i * 18;
                    y = 39;
                }
            }
            addSlotToContainer(new MachineSlot(tile,
                    TileMechanicalIndustrialAgglomerationFactory.SLOT_INPUT_START + i, x, y));
        }

        int outputCount = tile.getOutputSlotCount();
        for (int i = 0; i < 12; i++) {
            int x = HIDDEN, y = HIDDEN;
            if (i < outputCount) {
                if (tier == MachineTier.CRIMSON) {
                    x = 55 + (i % 6) * 18;
                    y = 87 + (i / 6) * 18;
                } else {
                    int startX = tier == MachineTier.MALACHITE ? 63 : 45;
                    x = offset + startX + i * 18;
                    y = 75;
                }
            }
            addSlotToContainer(new OutputSlot(tile,
                    TileMechanicalIndustrialAgglomerationFactory.SLOT_OUTPUT_START + i, x, y));
        }

        int machineHeight = tier == MachineTier.CRIMSON ? 147 : 131;
        int playerY = machineHeight + 14;
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
            if (progress != lastProgress) {
                crafter.sendProgressBarUpdate(this, 2, progress & 0xFFFF);
                crafter.sendProgressBarUpdate(this, 3, (progress >>> 16) & 0xFFFF);
            }
            if (batch != lastBatch) crafter.sendProgressBarUpdate(this, 4, batch);
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
                clientProgress = (clientProgress & 0xFFFF0000) | unsigned;
                tile.setClientProgress(clientProgress);
                break;
            case 3:
                clientProgress = (clientProgress & 0x0000FFFF) | (unsigned << 16);
                tile.setClientProgress(clientProgress);
                break;
            case 4:
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
        int machineSlots = TileMechanicalIndustrialAgglomerationFactory.INVENTORY_SIZE;

        if (index < machineSlots) {
            if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) return null;
        } else {
            boolean moved = false;
            for (int i = 0; i < tile.getUpgradeSlotCount() && !moved; i++) {
                int target = TileMechanicalIndustrialAgglomerationFactory.SLOT_UPGRADE_START + i;
                if (tile.isItemValidForSlot(target, stack)) {
                    moved = mergeItemStack(stack, target, target + 1, false);
                }
            }
            if (!moved) {
                moved = mergeItemStack(stack,
                        TileMechanicalIndustrialAgglomerationFactory.SLOT_INPUT_START,
                        TileMechanicalIndustrialAgglomerationFactory.SLOT_INPUT_START + tile.getInputSlotCount(), false);
            }
            if (!moved) return null;
        }

        if (stack.stackSize == 0) slot.putStack(null);
        else slot.onSlotChanged();
        return original;
    }

    private static class MachineSlot extends Slot {
        MachineSlot(TileMechanicalIndustrialAgglomerationFactory tile, int index, int x, int y) {
            super(tile, index, x, y);
        }
        @Override public boolean isItemValid(ItemStack stack) {
            return inventory.isItemValidForSlot(getSlotIndex(), stack);
        }
    }

    private static class UpgradeSlot extends MachineSlot {
        UpgradeSlot(TileMechanicalIndustrialAgglomerationFactory tile, int index, int x, int y) {
            super(tile, index, x, y);
        }
        @Override public int getSlotStackLimit() { return 1; }
    }

    private static class OutputSlot extends Slot {
        OutputSlot(TileMechanicalIndustrialAgglomerationFactory tile, int index, int x, int y) {
            super(tile, index, x, y);
        }
        @Override public boolean isItemValid(ItemStack stack) { return false; }
    }
}
