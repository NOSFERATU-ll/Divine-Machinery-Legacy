package com.nosferatu.divinemachinerylegacy.tile;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.ManaNetworkEvent;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;

import java.util.List;

/** 1.7.10 port of Extra Reforked's automated Jaded Amaranthus. */
public class TileJadedAmaranthus extends TileEntity implements ISidedInventory, IManaPool, ISparkAttachable {
    public static final int SLOT_UPGRADE_START = 0;
    public static final int SLOT_UPGRADE_END = 1;
    public static final int SLOT_OUTPUT_START = 2;
    public static final int SLOT_OUTPUT_END = 17;
    public static final int INVENTORY_SIZE = 18;

    public static final int MANA_CAPACITY = 1000000;
    public static final int COOLDOWN_TICKS = 30;
    public static final int COUNT_CRAFT = 1;
    public static final int MANA_PER_CRAFT_UNIT = 200;

    private final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
    private int mana;
    private int cooldown = COOLDOWN_TICKS;
    private boolean registeredInManaNetwork;

    @Override
    public void updateEntity() {
        if (worldObj == null) return;
        if (!worldObj.isRemote && !registeredInManaNetwork && !isInvalid()) {
            ManaNetworkEvent.addPool(this);
            registeredInManaNetwork = true;
        }
        if (worldObj.isRemote) return;

        if (hasInfiniteMana() && mana != MANA_CAPACITY) mana = MANA_CAPACITY;

        if (mana <= 0) {
            cooldown = COOLDOWN_TICKS;
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            markDirty();
            return;
        }

        boolean petal = hasPetalUpgrade();
        boolean petalBlock = hasPetalBlockUpgrade();

        for (int color = 0; color < 16; color++) {
            int slot = SLOT_OUTPUT_START + color;
            ItemStack current = inventory[slot];
            ItemStack product = productForColor(color, petal, petalBlock);
            if (product == null) continue;
            if (current != null && !stacksSameType(current, product)) continue;
            if (current != null && current.stackSize >= current.getMaxStackSize()) continue;

            int manaCost = MANA_PER_CRAFT_UNIT * COUNT_CRAFT * (petalBlock ? 9 : 1);
            if (mana < manaCost) break;

            int currentCount = current == null ? 0 : current.stackSize;
            int produced = petal && !petalBlock ? COUNT_CRAFT * 2 : COUNT_CRAFT;
            product.stackSize = Math.min(product.getMaxStackSize(), currentCount + produced);
            inventory[slot] = product;
            mana -= manaCost;
        }

        cooldown = COOLDOWN_TICKS;
        markDirty();
    }

    private ItemStack productForColor(int color, boolean petal, boolean petalBlock) {
        if (petalBlock) return new ItemStack(ModBlocks.petalBlock, 1, color);
        if (petal) return new ItemStack(ModItems.petal, 1, color);
        return new ItemStack(ModBlocks.flower, 1, color);
    }

    private boolean stacksSameType(ItemStack a, ItemStack b) {
        return a != null && b != null && a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage();
    }

    private boolean hasUpgrade(net.minecraft.item.Item item) {
        for (int slot = SLOT_UPGRADE_START; slot <= SLOT_UPGRADE_END; slot++) {
            ItemStack stack = inventory[slot];
            if (stack != null && stack.getItem() == item) return true;
        }
        return false;
    }

    public boolean hasInfiniteMana() { return hasUpgrade(DivineMachineryLegacy.catalystManaInfinity); }
    public boolean hasPetalUpgrade() { return hasUpgrade(DivineMachineryLegacy.catalystPetal); }
    public boolean hasPetalBlockUpgrade() { return hasUpgrade(DivineMachineryLegacy.catalystPetalBlock); }

    private boolean isUpgrade(ItemStack stack) {
        return stack != null && (stack.getItem() == DivineMachineryLegacy.catalystManaInfinity
                || stack.getItem() == DivineMachineryLegacy.catalystPetal
                || stack.getItem() == DivineMachineryLegacy.catalystPetalBlock);
    }

    public int getCooldown() { return cooldown; }

    @Override public int getCurrentMana() { return mana; }
    @Override public boolean isFull() { return mana >= MANA_CAPACITY; }
    @Override public void recieveMana(int amount) {
        long next = (long) mana + amount;
        if (next < 0L) next = 0L;
        if (next > MANA_CAPACITY) next = MANA_CAPACITY;
        int updated = (int) next;
        if (updated != mana) { mana = updated; markDirty(); }
    }
    @Override public boolean canRecieveManaFromBursts() { return !isFull(); }
    @Override public boolean isOutputtingPower() { return false; }
    @Override public boolean canAttachSpark(ItemStack stack) { return true; }
    @Override public void attachSpark(ISparkEntity spark) { }
    @Override public int getAvailableSpaceForMana() { return Math.max(0, MANA_CAPACITY - mana); }

    @Override
    public ISparkEntity getAttachedSpark() {
        if (worldObj == null) return null;
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
        List entities = worldObj.getEntitiesWithinAABB(Entity.class, box);
        ISparkEntity found = null;
        for (Object obj : entities) {
            if (!(obj instanceof ISparkEntity)) continue;
            if (found != null) return null;
            found = (ISparkEntity) obj;
        }
        return found;
    }
    @Override public boolean areIncomingTranfersDone() { return false; }

    @Override public void invalidate() {
        if (registeredInManaNetwork) { ManaNetworkEvent.removePool(this); registeredInManaNetwork = false; }
        super.invalidate();
    }
    @Override public void onChunkUnload() {
        if (registeredInManaNetwork) { ManaNetworkEvent.removePool(this); registeredInManaNetwork = false; }
        super.onChunkUnload();
    }

    @Override public int getSizeInventory() { return INVENTORY_SIZE; }
    @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < INVENTORY_SIZE ? inventory[slot] : null; }
    @Override public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = getStackInSlot(slot); if (stack == null) return null;
        ItemStack removed;
        if (stack.stackSize <= amount) { removed = stack; inventory[slot] = null; }
        else { removed = stack.splitStack(amount); if (stack.stackSize <= 0) inventory[slot] = null; }
        markDirty(); return removed;
    }
    @Override public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot); inventory[slot] = null; markDirty(); return stack;
    }
    @Override public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= INVENTORY_SIZE) return;
        inventory[slot] = stack;
        int limit = slot <= SLOT_UPGRADE_END ? 1 : 64;
        if (stack != null && stack.stackSize > limit) stack.stackSize = limit;
        markDirty();
    }
    @Override public String getInventoryName() { return "container.divinemachinerylegacy.jaded_amaranthus"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj != null && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + .5D, yCoord + .5D, zCoord + .5D) <= 64D;
    }
    @Override public void openInventory() { }
    @Override public void closeInventory() { }
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END && isUpgrade(stack);
    }
    @Override public int[] getAccessibleSlotsFromSide(int side) {
        int[] slots = new int[INVENTORY_SIZE]; for (int i = 0; i < INVENTORY_SIZE; i++) slots[i] = i; return slots;
    }
    @Override public boolean canInsertItem(int slot, ItemStack stack, int side) { return isItemValidForSlot(slot, stack); }
    @Override public boolean canExtractItem(int slot, ItemStack stack, int side) { return slot >= SLOT_OUTPUT_START && slot <= SLOT_OUTPUT_END; }

    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); tag.setInteger("Mana", mana); tag.setInteger("Cooldown", cooldown);
        NBTTagList list = new NBTTagList();
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory[slot]; if (stack == null) continue;
            NBTTagCompound entry = new NBTTagCompound(); entry.setByte("Slot", (byte) slot); stack.writeToNBT(entry); list.appendTag(entry);
        }
        tag.setTag("Items", list);
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag); mana = Math.max(0, Math.min(MANA_CAPACITY, tag.getInteger("Mana")));
        cooldown = Math.max(0, tag.getInteger("Cooldown"));
        for (int i = 0; i < INVENTORY_SIZE; i++) inventory[i] = null;
        NBTTagList list = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i); int slot = entry.getByte("Slot") & 255;
            if (slot < INVENTORY_SIZE) inventory[slot] = ItemStack.loadItemStackFromNBT(entry);
        }
    }
    public void setClientMana(int value) { if (worldObj != null && worldObj.isRemote) mana = Math.max(0, Math.min(MANA_CAPACITY, value)); }
    public void setClientCooldown(int value) { if (worldObj != null && worldObj.isRemote) cooldown = Math.max(0, value); }
}
