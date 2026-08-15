package com.nosferatu.divinemachinerylegacy.tile;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.ManaNetworkEvent;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Automated Orechid using Botania 1.7.10's native oreWeights/OreDictionary system. */
public class TileMechanicalOrechid extends TileEntity implements ISidedInventory, IManaPool, ISparkAttachable {
    public static final int SLOT_UPGRADE_START = 0;
    public static final int SLOT_UPGRADE_END = 1;
    public static final int SLOT_FILTER_START = 2;
    public static final int SLOT_FILTER_END = 8;
    public static final int SLOT_INPUT_START = 9;
    public static final int SLOT_INPUT_END = 22;
    public static final int SLOT_OUTPUT_START = 23;
    public static final int SLOT_OUTPUT_END = 36;
    public static final int INVENTORY_SIZE = 37;

    public static final int ORE_MANA_COST = 10000;
    public static final int COOLDOWN_TICKS = 10;

    private final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
    private int mana;
    private int cooldown;
    private boolean registeredInManaNetwork;
    private boolean internalInventoryMutation;

    @Override
    public void updateEntity() {
        if (worldObj == null) return;
        if (!worldObj.isRemote && !registeredInManaNetwork && !isInvalid()) {
            ManaNetworkEvent.addPool(this);
            registeredInManaNetwork = true;
        }
        if (worldObj.isRemote) return;

        if (cooldown > 0) cooldown--;
        if (cooldown > 0) return;

        processInputs();
        refillInfiniteStone();
        cooldown = COOLDOWN_TICKS;
        markDirty();
    }

    private void processInputs() {
        for (int i = 0; i < getInputSlotCount(); i++) {
            int inputSlot = SLOT_INPUT_START + i;
            int outputSlot = SLOT_OUTPUT_START + i;
            ItemStack input = inventory[inputSlot];
            if (!isStone(input) || inventory[outputSlot] != null) continue;

            ItemStack ore = chooseOre();
            if (ore == null) continue;

            int amount = Math.min(input.stackSize, getTier().getParallelCrafts());
            amount = Math.min(amount, ore.getMaxStackSize());
            if (!hasInfiniteMana()) amount = Math.min(amount, mana / ORE_MANA_COST);
            if (amount <= 0) continue;

            if (!hasInfiniteMana()) mana -= amount * ORE_MANA_COST;
            input.stackSize -= amount;
            if (input.stackSize <= 0) inventory[inputSlot] = null;

            ore.stackSize = amount;
            inventory[outputSlot] = ore;
            worldObj.playSoundEffect(xCoord + .5D, yCoord + .5D, zCoord + .5D,
                    "botania:orechid", .6F, 1F);
        }
    }

    private ItemStack chooseOre() {
        boolean filtered = hasAnyFilter();
        List<WeightedOre> candidates = new ArrayList<WeightedOre>();
        int totalWeight = 0;

        for (Map.Entry<String, Integer> entry : BotaniaAPI.oreWeights.entrySet()) {
            int weight = entry.getValue() == null ? 0 : entry.getValue();
            if (weight <= 0) continue;
            ItemStack representative = representative(entry.getKey());
            if (representative == null) continue;
            if (filtered && !matchesAnyFilter(representative)) continue;
            candidates.add(new WeightedOre(representative, weight));
            totalWeight += weight;
        }

        if (candidates.isEmpty() || totalWeight <= 0) return null;
        int roll = worldObj.rand.nextInt(totalWeight);
        for (WeightedOre candidate : candidates) {
            roll -= candidate.weight;
            if (roll < 0) return candidate.stack.copy();
        }
        return candidates.get(candidates.size() - 1).stack.copy();
    }

    private ItemStack representative(String oreName) {
        List<ItemStack> ores = OreDictionary.getOres(oreName);
        for (ItemStack stack : ores) {
            if (stack == null || stack.getItem() == null) continue;
            String className = stack.getItem().getClass().getName();
            if (className.startsWith("gregtech") || className.startsWith("gregapi")) continue;
            ItemStack copy = stack.copy();
            copy.stackSize = 1;
            return copy;
        }
        return null;
    }

    private boolean hasAnyFilter() {
        for (int i = 0; i < getFilterSlotCount(); i++) {
            if (inventory[SLOT_FILTER_START + i] != null) return true;
        }
        return false;
    }

    private boolean matchesAnyFilter(ItemStack output) {
        for (int i = 0; i < getFilterSlotCount(); i++) {
            ItemStack filter = inventory[SLOT_FILTER_START + i];
            if (filter != null && OreDictionary.itemMatches(output, filter, false)) return true;
        }
        return false;
    }

    private boolean isValidFilter(ItemStack filter) {
        if (filter == null) return false;
        for (String oreName : BotaniaAPI.oreWeights.keySet()) {
            ItemStack output = representative(oreName);
            if (output != null && OreDictionary.itemMatches(output, filter, false)) return true;
        }
        return false;
    }

    private void refillInfiniteStone() {
        if (!hasInfiniteStone()) return;
        internalInventoryMutation = true;
        try {
            for (int i = 0; i < getInputSlotCount(); i++) {
                inventory[SLOT_INPUT_START + i] = new ItemStack(Blocks.stone, 64, 0);
            }
        } finally {
            internalInventoryMutation = false;
        }
    }

    private boolean isStone(ItemStack stack) {
        return stack != null && Block.getBlockFromItem(stack.getItem()) == Blocks.stone && stack.getItemDamage() == 0;
    }

    public MachineTier getTier() {
        return worldObj == null ? MachineTier.MALACHITE
                : MachineTier.fromMeta(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
    }

    public int getManaCapacity() { return getTier().getManaCapacity(); }
    public int getCooldown() { return cooldown; }

    public int getUpgradeSlotCount() {
        MachineTier tier = getTier();
        return tier == MachineTier.SHADOW || tier == MachineTier.CRIMSON ? 2 : 0;
    }

    public int getFilterSlotCount() {
        switch (getTier()) {
            case SAFFRON: return 5;
            case SHADOW:
            case CRIMSON: return 7;
            case MALACHITE:
            default: return 3;
        }
    }

    public int getInputSlotCount() {
        switch (getTier()) {
            case SAFFRON: return 9;
            case SHADOW: return 10;
            case CRIMSON: return 14;
            case MALACHITE:
            default: return 5;
        }
    }

    public int getOutputSlotCount() { return getInputSlotCount(); }

    private boolean hasUpgrade(net.minecraft.item.Item item) {
        for (int i = 0; i < getUpgradeSlotCount(); i++) {
            ItemStack stack = inventory[SLOT_UPGRADE_START + i];
            if (stack != null && stack.getItem() == item) return true;
        }
        return false;
    }

    public boolean hasInfiniteMana() { return hasUpgrade(DivineMachineryLegacy.catalystManaInfinity); }
    public boolean hasInfiniteStone() { return hasUpgrade(DivineMachineryLegacy.catalystStoneInfinity); }

    private boolean isUpgrade(ItemStack stack) {
        return stack != null && (stack.getItem() == DivineMachineryLegacy.catalystManaInfinity
                || stack.getItem() == DivineMachineryLegacy.catalystStoneInfinity);
    }

    @Override public int getCurrentMana() { return mana; }
    @Override public boolean isFull() { return mana >= getManaCapacity(); }

    @Override
    public void recieveMana(int amount) {
        long next = (long) mana + amount;
        if (next < 0L) next = 0L;
        if (next > getManaCapacity()) next = getManaCapacity();
        int updated = (int) next;
        if (updated != mana) {
            mana = updated;
            markDirty();
        }
    }

    @Override public boolean canRecieveManaFromBursts() { return !isFull(); }
    @Override public boolean isOutputtingPower() { return false; }
    @Override public boolean canAttachSpark(ItemStack stack) { return true; }
    @Override public void attachSpark(ISparkEntity spark) { }
    @Override public int getAvailableSpaceForMana() { return Math.max(0, getManaCapacity() - mana); }

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

    @Override
    public void invalidate() {
        if (registeredInManaNetwork) {
            ManaNetworkEvent.removePool(this);
            registeredInManaNetwork = false;
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (registeredInManaNetwork) {
            ManaNetworkEvent.removePool(this);
            registeredInManaNetwork = false;
        }
        super.onChunkUnload();
    }

    @Override public int getSizeInventory() { return INVENTORY_SIZE; }
    @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < INVENTORY_SIZE ? inventory[slot] : null; }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = getStackInSlot(slot);
        if (stack == null) return null;
        ItemStack removed;
        if (stack.stackSize <= amount) {
            removed = stack;
            inventory[slot] = null;
        } else {
            removed = stack.splitStack(amount);
            if (stack.stackSize <= 0) inventory[slot] = null;
        }
        inventoryChanged(slot);
        return removed;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot);
        inventory[slot] = null;
        inventoryChanged(slot);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= INVENTORY_SIZE) return;
        inventory[slot] = stack;
        int limit = (slot <= SLOT_UPGRADE_END || (slot >= SLOT_FILTER_START && slot <= SLOT_FILTER_END)) ? 1 : 64;
        if (stack != null && stack.stackSize > limit) stack.stackSize = limit;
        inventoryChanged(slot);
    }

    private void inventoryChanged(int slot) {
        if (!internalInventoryMutation) markDirty();
    }

    @Override public String getInventoryName() { return "container.divinemachinerylegacy.orechid"; }
    @Override public boolean hasCustomInventoryName() { return false; }
    @Override public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj != null && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + .5D, yCoord + .5D, zCoord + .5D) <= 64D;
    }

    @Override public void openInventory() { }
    @Override public void closeInventory() { }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END)
            return slot - SLOT_UPGRADE_START < getUpgradeSlotCount() && isUpgrade(stack);
        if (slot >= SLOT_FILTER_START && slot < SLOT_FILTER_START + getFilterSlotCount()) return isValidFilter(stack);
        if (slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + getInputSlotCount()) return isStone(stack);
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int[] slots = new int[getUpgradeSlotCount() + getFilterSlotCount() + getInputSlotCount() + getOutputSlotCount()];
        int p = 0;
        for (int i = 0; i < getUpgradeSlotCount(); i++) slots[p++] = SLOT_UPGRADE_START + i;
        for (int i = 0; i < getFilterSlotCount(); i++) slots[p++] = SLOT_FILTER_START + i;
        for (int i = 0; i < getInputSlotCount(); i++) slots[p++] = SLOT_INPUT_START + i;
        for (int i = 0; i < getOutputSlotCount(); i++) slots[p++] = SLOT_OUTPUT_START + i;
        return slots;
    }

    @Override public boolean canInsertItem(int slot, ItemStack stack, int side) { return isItemValidForSlot(slot, stack); }
    @Override public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot >= SLOT_OUTPUT_START && slot < SLOT_OUTPUT_START + getOutputSlotCount();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Mana", mana);
        tag.setInteger("Cooldown", cooldown);
        NBTTagList list = new NBTTagList();
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            if (inventory[slot] == null) continue;
            NBTTagCompound entry = new NBTTagCompound();
            entry.setByte("Slot", (byte) slot);
            inventory[slot].writeToNBT(entry);
            list.appendTag(entry);
        }
        tag.setTag("Items", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        mana = Math.max(0, tag.getInteger("Mana"));
        cooldown = Math.max(0, tag.getInteger("Cooldown"));
        for (int i = 0; i < INVENTORY_SIZE; i++) inventory[i] = null;
        NBTTagList list = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < INVENTORY_SIZE) inventory[slot] = ItemStack.loadItemStackFromNBT(entry);
        }
    }

    public void setClientMana(int value) { if (worldObj != null && worldObj.isRemote) mana = Math.max(0, value); }
    public void setClientCooldown(int value) { if (worldObj != null && worldObj.isRemote) cooldown = Math.max(0, value); }

    private static final class WeightedOre {
        final ItemStack stack;
        final int weight;
        WeightedOre(ItemStack stack, int weight) {
            this.stack = stack;
            this.weight = weight;
        }
    }
}
