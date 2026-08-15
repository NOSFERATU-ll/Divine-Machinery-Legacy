package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.tile.TileGreenhouse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Reforked Greenhouse slot layout: 7 flowers, 21 fuels and 8 fixed upgrade slots. */
public class ContainerGreenhouse extends Container {
    private final TileGreenhouse tile;
    private int lastMana = Integer.MIN_VALUE, lastEnergy = Integer.MIN_VALUE;
    private int lastHeat = Integer.MIN_VALUE, lastSleep = Integer.MIN_VALUE;
    private int clientMana, clientEnergy;

    public ContainerGreenhouse(InventoryPlayer playerInventory, TileGreenhouse tile) {
        this.tile = tile;
        for (int i = 0; i < 7; i++) addSlotToContainer(new GreenhouseSlot(tile, i, 46 + i * 18, 23));
        for (int i = 0; i < 21; i++) addSlotToContainer(new GreenhouseSlot(tile, TileGreenhouse.FUEL_START + i,
                46 + (i % 7) * 18, 45 + (i / 7) * 18));

        int[][] u = {{22,34},{22,54},{22,74},{22,94},{178,34},{178,54},{178,74},{178,94}};
        for (int i = 0; i < 8; i++) addSlotToContainer(new GreenhouseSlot(tile, TileGreenhouse.UPGRADE_START + i, u[i][0], u[i][1]));

        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++)
            addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 28 + col * 18, 150 + row * 18));
        for (int col = 0; col < 9; col++) addSlotToContainer(new Slot(playerInventory, col, 28 + col * 18, 208));
    }

    @Override public boolean canInteractWith(EntityPlayer player) { return tile.isUseableByPlayer(player); }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int mana = tile.getCurrentMana(), energy = tile.getEnergy(), heat = tile.getHeat(), sleep = tile.getSleep();
        for (Object o : crafters) {
            ICrafting c = (ICrafting) o;
            if (mana != lastMana) { c.sendProgressBarUpdate(this,0,mana & 0xffff); c.sendProgressBarUpdate(this,1,(mana >>> 16) & 0xffff); }
            if (energy != lastEnergy) { c.sendProgressBarUpdate(this,2,energy & 0xffff); c.sendProgressBarUpdate(this,3,(energy >>> 16) & 0xffff); }
            if (heat != lastHeat) c.sendProgressBarUpdate(this,4,heat);
            if (sleep != lastSleep) c.sendProgressBarUpdate(this,5,sleep);
        }
        lastMana = mana; lastEnergy = energy; lastHeat = heat; lastSleep = sleep;
    }

    @Override
    public void updateProgressBar(int id, int value) {
        int u = value & 0xffff;
        if (id == 0) { clientMana = (clientMana & 0xffff0000) | u; tile.setClientMana(clientMana); }
        else if (id == 1) { clientMana = (clientMana & 0xffff) | (u << 16); tile.setClientMana(clientMana); }
        else if (id == 2) { clientEnergy = (clientEnergy & 0xffff0000) | u; tile.setClientEnergy(clientEnergy); }
        else if (id == 3) { clientEnergy = (clientEnergy & 0xffff) | (u << 16); tile.setClientEnergy(clientEnergy); }
        else if (id == 4) tile.setClientHeat(u);
        else if (id == 5) tile.setClientSleep(u);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return null;
        ItemStack stack = slot.getStack(), original = stack.copy();
        int machine = TileGreenhouse.INVENTORY_SIZE;
        if (index < machine) {
            if (!mergeItemStack(stack, machine, inventorySlots.size(), true)) return null;
        } else {
            boolean moved = false;
            for (int s = TileGreenhouse.UPGRADE_START; s <= TileGreenhouse.UPGRADE_END && !moved; s++)
                if (tile.isItemValidForSlot(s, stack)) moved = mergeItemStack(stack, s, s + 1, false);
            if (!moved && tile.isItemValidForSlot(TileGreenhouse.FLOWER_START, stack))
                moved = mergeItemStack(stack, TileGreenhouse.FLOWER_START, TileGreenhouse.FLOWER_END + 1, false);
            if (!moved) {
                for (int s = TileGreenhouse.FUEL_START; s <= TileGreenhouse.FUEL_END && !moved; s++)
                    if (tile.isItemValidForSlot(s, stack)) moved = mergeItemStack(stack, s, s + 1, false);
            }
            if (!moved) return null;
        }
        if (stack.stackSize == 0) slot.putStack(null); else slot.onSlotChanged();
        return original;
    }

    private static class GreenhouseSlot extends Slot {
        GreenhouseSlot(TileGreenhouse inv, int index, int x, int y) { super(inv, index, x, y); }
        @Override public boolean isItemValid(ItemStack stack) { return inventory.isItemValidForSlot(getSlotIndex(), stack); }
        @Override public int getSlotStackLimit() {
            int i = getSlotIndex();
            if (i >= TileGreenhouse.UPGRADE_START) return 1;
            if (i <= TileGreenhouse.FLOWER_END) return ((TileGreenhouse) inventory).getFlowerSlotLimit();
            return 64;
        }
    }
}
