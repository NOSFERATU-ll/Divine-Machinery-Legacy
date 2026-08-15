package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalManaInfuser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Exact Reforked tier slot geometry adapted to the stable 28-slot 1.7.10 inventory. */
public class ContainerMechanicalManaInfuser extends Container {
    private static final int HIDDEN = -1000;
    private final TileMechanicalManaInfuser tile;
    private int lastMana = Integer.MIN_VALUE;
    private int lastProgress = Integer.MIN_VALUE;
    private int lastRecipe = Integer.MIN_VALUE;
    private int lastBatch = Integer.MIN_VALUE;
    private int clientMana;
    private int clientProgress;

    public ContainerMechanicalManaInfuser(InventoryPlayer playerInventory, TileMechanicalManaInfuser tile) {
        this.tile = tile;
        MachineTier tier = tile.getTier();

        int ux0 = HIDDEN, ux1 = HIDDEN, uy = HIDDEN;
        if (tier == MachineTier.SHADOW) { ux0 = 17; ux1 = 163; uy = 61; }
        else if (tier == MachineTier.CRIMSON) { ux0 = 17; ux1 = 183; uy = 69; }
        addSlotToContainer(new UpgradeSlot(tile, 0, ux0, uy));
        addSlotToContainer(new UpgradeSlot(tile, 1, ux1, uy));

        int inputCount = tile.getInputSlotCount();
        int inputX, inputY, inputCols;
        if (tier == MachineTier.MALACHITE) { inputX = 72; inputY = 39; inputCols = 3; }
        else if (tier == MachineTier.SAFFRON) { inputX = 54; inputY = 39; inputCols = 5; }
        else if (tier == MachineTier.SHADOW) { inputX = 36; inputY = 39; inputCols = 7; }
        else { inputX = 46; inputY = 33; inputCols = 7; }
        for (int i = 0; i < 14; i++) {
            addSlotToContainer(new InputSlot(tile, TileMechanicalManaInfuser.SLOT_INPUT_START + i,
                    i < inputCount ? inputX + (i % inputCols) * 18 : HIDDEN,
                    i < inputCount ? inputY + (i / inputCols) * 18 : HIDDEN));
        }

        int outputCount = tile.getOutputSlotCount();
        int outputX = tier == MachineTier.MALACHITE ? 63 : tier == MachineTier.CRIMSON ? 55 : 45;
        int outputY = tier == MachineTier.CRIMSON ? 87 : 75;
        int outputCols = tier == MachineTier.MALACHITE ? 4 : 6;
        for (int i = 0; i < 12; i++) {
            addSlotToContainer(new OutputSlot(tile, TileMechanicalManaInfuser.SLOT_OUTPUT_START + i,
                    i < outputCount ? outputX + (i % outputCols) * 18 : HIDDEN,
                    i < outputCount ? outputY + (i / outputCols) * 18 : HIDDEN));
        }

        int machineHeight = tier == MachineTier.CRIMSON ? 147 : 131;
        int playerX = tier == MachineTier.CRIMSON ? 28 : 18;
        int playerY = machineHeight + 14;
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9,
                    playerX + col * 18, playerY + row * 18));
        for (int col = 0; col < 9; col++)
            addSlotToContainer(new Slot(playerInventory, col, playerX + col * 18, playerY + 58));
    }

    @Override public boolean canInteractWith(EntityPlayer player) { return tile.isUseableByPlayer(player); }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int mana = tile.getCurrentMana();
        int progress = tile.getProgress();
        int recipe = tile.getMaxProgress();
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
            if (recipe != lastRecipe) {
                crafter.sendProgressBarUpdate(this, 4, recipe & 0xFFFF);
                crafter.sendProgressBarUpdate(this, 5, (recipe >>> 16) & 0xFFFF);
            }
            if (batch != lastBatch) crafter.sendProgressBarUpdate(this, 6, batch);
        }
        lastMana = mana; lastProgress = progress; lastRecipe = recipe; lastBatch = batch;
    }

    @Override
    public void updateProgressBar(int id, int value) {
        int u = value & 0xFFFF;
        if (id == 0) { clientMana = (clientMana & 0xFFFF0000) | u; tile.setClientMana(clientMana); }
        else if (id == 1) { clientMana = (clientMana & 0xFFFF) | (u << 16); tile.setClientMana(clientMana); }
        else if (id == 2) { clientProgress = (clientProgress & 0xFFFF0000) | u; tile.setClientProgress(clientProgress); }
        else if (id == 3) { clientProgress = (clientProgress & 0xFFFF) | (u << 16); tile.setClientProgress(clientProgress); }
        else if (id == 4 || id == 5) {
            // Client only needs max progress for drawing; encode it through a synthetic active recipe is unsafe.
            // Gui reads the separately reconstructed field below.
            if (id == 4) clientMaxProgress = (clientMaxProgress & 0xFFFF0000) | u;
            else clientMaxProgress = (clientMaxProgress & 0xFFFF) | (u << 16);
        } else if (id == 6) tile.setClientActiveBatch(u);
    }

    private int clientMaxProgress;
    public int getClientMaxProgress() { return clientMaxProgress; }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return null;
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        int machineSlots = TileMechanicalManaInfuser.INVENTORY_SIZE;
        if (index < machineSlots) {
            if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) return null;
        } else {
            boolean moved = false;
            for (int i = 0; i < tile.getUpgradeSlotCount() && !moved; i++) {
                int target = TileMechanicalManaInfuser.SLOT_UPGRADE_START + i;
                if (tile.isItemValidForSlot(target, stack)) moved = mergeItemStack(stack, target, target + 1, false);
            }
            if (!moved && tile.isItemValidForSlot(TileMechanicalManaInfuser.SLOT_INPUT_START, stack))
                moved = mergeItemStack(stack, TileMechanicalManaInfuser.SLOT_INPUT_START,
                        TileMechanicalManaInfuser.SLOT_INPUT_START + tile.getInputSlotCount(), false);
            if (!moved) return null;
        }
        if (stack.stackSize == 0) slot.putStack(null); else slot.onSlotChanged();
        return original;
    }

    private static class UpgradeSlot extends Slot {
        UpgradeSlot(TileMechanicalManaInfuser tile, int index, int x, int y) { super(tile,index,x,y); }
        @Override public boolean isItemValid(ItemStack stack) { return inventory.isItemValidForSlot(getSlotIndex(), stack); }
        @Override public int getSlotStackLimit() { return 1; }
    }
    private static class InputSlot extends Slot {
        InputSlot(TileMechanicalManaInfuser tile, int index, int x, int y) { super(tile,index,x,y); }
        @Override public boolean isItemValid(ItemStack stack) { return inventory.isItemValidForSlot(getSlotIndex(), stack); }
    }
    private static class OutputSlot extends Slot {
        OutputSlot(TileMechanicalManaInfuser tile, int index, int x, int y) { super(tile,index,x,y); }
        @Override public boolean isItemValid(ItemStack stack) { return false; }
    }
}
