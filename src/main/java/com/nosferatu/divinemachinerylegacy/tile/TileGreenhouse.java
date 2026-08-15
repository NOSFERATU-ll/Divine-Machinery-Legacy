package com.nosferatu.divinemachinerylegacy.tile;

import cofh.api.energy.IEnergyReceiver;
import com.nosferatu.divinemachinerylegacy.item.ItemGreenhouseUpgrade;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.ManaNetworkEvent;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;

import java.util.List;

/** Reforked Greenhouse adapted to Botania/CoFH RF APIs available on Minecraft 1.7.10. */
public class TileGreenhouse extends TileEntity implements ISidedInventory, IManaPool, ISparkAttachable, IEnergyReceiver {
    public static final int FLOWER_START = 0;
    public static final int FLOWER_END = 6;
    public static final int FUEL_START = 7;
    public static final int FUEL_END = 27;
    public static final int UPGRADE_START = 28;
    public static final int UPGRADE_END = 35;
    public static final int INVENTORY_SIZE = 36;

    public static final int BASE_MANA_CAPACITY = 1000000;
    public static final int BASE_ENERGY_CAPACITY = 1000000;
    public static final int BASE_ENERGY_COST = 2000;
    public static final int BASE_SLEEP = 20;
    public static final int MAX_HEAT = 40;

    private final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
    private int mana;
    private int energy;
    private int heat;
    private int sleep = BASE_SLEEP;
    private boolean registeredInManaNetwork;

    @Override
    public void updateEntity() {
        if (worldObj == null) return;
        if (!worldObj.isRemote && !registeredInManaNetwork && !isInvalid()) {
            ManaNetworkEvent.addPool(this);
            registeredInManaNetwork = true;
        }
        if (worldObj.isRemote) return;

        mana = Math.min(mana, getManaCapacity());
        energy = Math.min(energy, getEnergyCapacity());
        if (sleep > 0) {
            sleep--;
            markDirty();
            return;
        }

        boolean crafted = false;
        int manaSpace = getManaCapacity() - mana;
        if (manaSpace > 0 && energy > 0) {
            for (int fs = FLOWER_START; fs <= FLOWER_END && manaSpace > 0 && energy > 0; fs++) {
                ItemStack flower = inventory[fs];
                if (!GreenhouseFlowerRegistry.isGeneratingFlower(flower)) continue;
                int fuelSlot = findFuel(flower);
                if (fuelSlot < 0) continue;
                ItemStack fuel = inventory[fuelSlot];
                int baseMana = GreenhouseFlowerRegistry.getBaseManaPerFuel(flower, fuel, worldObj.rand);
                if (baseMana <= 0) continue;

                double manaMul = hasCategory(1) ? 1.25D : 1D;
                manaMul *= heatMultiplier();
                double energyMul = hasCategory(1) ? 2D : 1D;
                if (hasCategory(3)) energyMul *= 0.5D;
                int manaPerFuel = Math.max(1, (int) Math.round(baseMana * manaMul));
                int energyPerFuel = Math.max(1, (int) Math.round(BASE_ENERGY_COST * energyMul));

                int craftCount = Math.min(flower.stackSize, fuel.stackSize);
                craftCount = Math.min(craftCount, energy / energyPerFuel);
                craftCount = Math.min(craftCount, manaSpace / manaPerFuel);
                if (craftCount <= 0) continue;

                fuel.stackSize -= craftCount;
                if (fuel.stackSize <= 0) inventory[fuelSlot] = null;
                energy -= energyPerFuel * craftCount;
                int add = manaPerFuel * craftCount;
                mana += add;
                manaSpace -= add;
                crafted = true;
            }
        }

        if (hasCategory(7)) heat = crafted ? Math.min(MAX_HEAT, heat + 1) : Math.max(0, heat - 1);
        else heat = 0;
        sleep = getCycleLength();
        markDirty();
    }

    private int findFuel(ItemStack flower) {
        int end = hasCategory(2) ? FUEL_END : FUEL_START + 6;
        for (int i = FUEL_START; i <= end; i++) {
            if (GreenhouseFlowerRegistry.isFuelFor(flower, inventory[i])) return i;
        }
        return -1;
    }

    private double heatMultiplier() {
        if (!hasCategory(7)) return 1D;
        if (heat >= 40) return 1.20D;
        if (heat >= 30) return 1.15D;
        if (heat >= 20) return 1.10D;
        if (heat >= 10) return 1.05D;
        return 1D;
    }

    private ItemGreenhouseUpgrade getUpgrade(int category) {
        ItemStack stack = inventory[UPGRADE_START + category];
        return stack != null && stack.getItem() instanceof ItemGreenhouseUpgrade
                ? (ItemGreenhouseUpgrade) stack.getItem() : null;
    }

    private boolean hasCategory(int category) { return getUpgrade(category) != null; }

    public int getFlowerSlotLimit() {
        ItemGreenhouseUpgrade upgrade = getUpgrade(0);
        return upgrade == null ? 1 : Math.max(1, upgrade.getValue());
    }

    public int getCycleLength() {
        ItemGreenhouseUpgrade upgrade = getUpgrade(4);
        int reduction = upgrade == null ? 0 : upgrade.getValue();
        return Math.max(1, (int) Math.round(BASE_SLEEP * (1D - reduction / 100D)));
    }

    public int getManaCapacity() { return scaledCapacity(BASE_MANA_CAPACITY, getUpgrade(6)); }
    public int getEnergyCapacity() { return scaledCapacity(BASE_ENERGY_CAPACITY, getUpgrade(5)); }

    private int scaledCapacity(int base, ItemGreenhouseUpgrade upgrade) {
        if (upgrade == null || upgrade.getValue() <= 1) return base;
        long value = (long) base * upgrade.getValue();
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
    }

    public int getHeat() { return heat; }
    public int getSleep() { return sleep; }
    public int getEnergy() { return energy; }

    // CoFH RF input
    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        int accepted = Math.max(0, Math.min(maxReceive, getEnergyCapacity() - energy));
        if (!simulate && accepted > 0) { energy += accepted; markDirty(); }
        return accepted;
    }
    @Override public int getEnergyStored(ForgeDirection from) { return energy; }
    @Override public int getMaxEnergyStored(ForgeDirection from) { return getEnergyCapacity(); }
    @Override public boolean canConnectEnergy(ForgeDirection from) { return true; }

    // Botania mana / Spark API
    @Override public int getCurrentMana() { return mana; }
    @Override public boolean isFull() { return mana >= getManaCapacity(); }
    @Override public boolean canRecieveManaFromBursts() { return !isFull(); }
    @Override public boolean isOutputtingPower() { return false; }
    @Override public void recieveMana(int amount) {
        long next = (long) mana + amount;
        mana = (int) Math.max(0L, Math.min((long) getManaCapacity(), next));
        markDirty();
    }
    @Override public boolean canAttachSpark(ItemStack stack) { return true; }
    @Override public void attachSpark(ISparkEntity spark) { }
    @Override public int getAvailableSpaceForMana() { return Math.max(0, getManaCapacity() - mana); }
    @Override public boolean areIncomingTranfersDone() { return false; }

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

    @Override
    public void invalidate() {
        if (registeredInManaNetwork) { ManaNetworkEvent.removePool(this); registeredInManaNetwork = false; }
        super.invalidate();
    }
    @Override
    public void onChunkUnload() {
        if (registeredInManaNetwork) { ManaNetworkEvent.removePool(this); registeredInManaNetwork = false; }
        super.onChunkUnload();
    }

    // Inventory / AE2 import-bus compatibility
    @Override public int getSizeInventory() { return INVENTORY_SIZE; }
    @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < INVENTORY_SIZE ? inventory[slot] : null; }
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = getStackInSlot(slot);
        if (stack == null) return null;
        ItemStack removed;
        if (stack.stackSize <= amount) { removed = stack; inventory[slot] = null; }
        else { removed = stack.splitStack(amount); if (stack.stackSize <= 0) inventory[slot] = null; }
        markDirty();
        return removed;
    }
    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot); if (slot >= 0 && slot < INVENTORY_SIZE) inventory[slot] = null; return stack;
    }
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= INVENTORY_SIZE) return;
        inventory[slot] = stack;
        if (stack != null) {
            int limit = slot <= FLOWER_END ? getFlowerSlotLimit() : slot >= UPGRADE_START ? 1 : 64;
            if (stack.stackSize > limit) stack.stackSize = limit;
        }
        mana = Math.min(mana, getManaCapacity());
        energy = Math.min(energy, getEnergyCapacity());
        markDirty();
    }
    @Override public String getInventoryName() { return "container.divinemachinerylegacy.greenhouse"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj != null && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + .5D, yCoord + .5D, zCoord + .5D) <= 64D;
    }
    @Override public void openInventory() { }
    @Override public void closeInventory() { }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot >= FLOWER_START && slot <= FLOWER_END) return GreenhouseFlowerRegistry.isGeneratingFlower(stack);
        if (slot >= FUEL_START && slot <= FUEL_END) {
            if (slot > FUEL_START + 6 && !hasCategory(2)) return false;
            for (int f = FLOWER_START; f <= FLOWER_END; f++)
                if (GreenhouseFlowerRegistry.isFuelFor(inventory[f], stack)) return true;
            return false;
        }
        if (slot >= UPGRADE_START && slot <= UPGRADE_END && stack != null && stack.getItem() instanceof ItemGreenhouseUpgrade)
            return ((ItemGreenhouseUpgrade) stack.getItem()).getUpgradeSlot() == slot - UPGRADE_START;
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int[] slots = new int[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) slots[i] = i;
        return slots;
    }
    @Override public boolean canInsertItem(int slot, ItemStack stack, int side) { return isItemValidForSlot(slot, stack); }
    @Override public boolean canExtractItem(int slot, ItemStack stack, int side) { return false; }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Mana", mana); tag.setInteger("Energy", energy); tag.setInteger("Heat", heat); tag.setInteger("Sleep", sleep);
        NBTTagList list = new NBTTagList();
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory[slot]; if (stack == null) continue;
            NBTTagCompound entry = new NBTTagCompound(); entry.setByte("Slot", (byte) slot); stack.writeToNBT(entry); list.appendTag(entry);
        }
        tag.setTag("Items", list);
    }
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        mana = Math.max(0, tag.getInteger("Mana")); energy = Math.max(0, tag.getInteger("Energy"));
        heat = Math.max(0, Math.min(MAX_HEAT, tag.getInteger("Heat"))); sleep = Math.max(0, tag.getInteger("Sleep"));
        for (int i = 0; i < INVENTORY_SIZE; i++) inventory[i] = null;
        NBTTagList list = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i); int slot = entry.getByte("Slot") & 255;
            if (slot < INVENTORY_SIZE) inventory[slot] = ItemStack.loadItemStackFromNBT(entry);
        }
        mana = Math.min(mana, getManaCapacity()); energy = Math.min(energy, getEnergyCapacity());
    }

    public void setClientMana(int v) { if (worldObj != null && worldObj.isRemote) mana = Math.max(0, v); }
    public void setClientEnergy(int v) { if (worldObj != null && worldObj.isRemote) energy = Math.max(0, v); }
    public void setClientHeat(int v) { if (worldObj != null && worldObj.isRemote) heat = Math.max(0, Math.min(MAX_HEAT, v)); }
    public void setClientSleep(int v) { if (worldObj != null && worldObj.isRemote) sleep = Math.max(0, v); }
}
