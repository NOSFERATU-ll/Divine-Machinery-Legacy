package com.nosferatu.divinemachinerylegacy.tile;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
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
import vazkii.botania.common.item.ModItems;

import java.util.List;

/**
 * 1.7.10 port of Extra Reforked's Industrial Agglomeration Factory.
 *
 * Botania r1.8-249 has no generic Terra Plate recipe registry: its Terra Plate
 * hard-codes Manasteel + Mana Pearl + Mana Diamond -> Terrasteel for 500k mana.
 * This machine intentionally automates that exact 1.7.10 recipe instead of
 * inventing a modern-only recipe abstraction.
 */
public class TileMechanicalIndustrialAgglomerationFactory extends TileEntity
        implements ISidedInventory, ISparkAttachable, IManaPool, ICraftingMachine {

    public static final int SLOT_UPGRADE_START = 0;
    public static final int SLOT_UPGRADE_END = 1;
    public static final int SLOT_INPUT_START = 2;
    public static final int SLOT_INPUT_END = 15;
    public static final int SLOT_OUTPUT_START = 16;
    public static final int SLOT_OUTPUT_END = 27;
    public static final int INVENTORY_SIZE = 28;

    public static final int RECIPE_MANA = 500000;
    public static final int BASE_MANA_PER_TICK = 5000;

    private final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
    private int mana;
    private int progress;
    private int activeBatch;
    private boolean internalInventoryMutation;
    private boolean registeredInManaNetwork;

    @Override
    public void updateEntity() {
        if (worldObj == null) return;

        if (!worldObj.isRemote && !registeredInManaNetwork && !isInvalid()) {
            ManaNetworkEvent.addPool(this);
            registeredInManaNetwork = true;
        }
        if (worldObj.isRemote) return;

        if (activeBatch <= 0) {
            activeBatch = chooseBatch();
            progress = 0;
            if (activeBatch <= 0) return;
        }

        if (!canContinue(activeBatch)) {
            resetOperation();
            markDirty();
            return;
        }

        int remaining = RECIPE_MANA - progress;
        int advance = Math.min(getManaPerTick(), remaining);
        if (!hasInfiniteMana()) {
            advance = Math.min(advance, mana / activeBatch);
            if (advance <= 0) return;
            mana -= advance * activeBatch;
        }

        progress += advance;
        if (progress >= RECIPE_MANA) craft(activeBatch);
        markDirty();
    }

    private int chooseBatch() {
        int max = Math.min(getTier().getParallelCrafts(),
                Math.min(countIngredient(0), Math.min(countIngredient(1), countIngredient(2))));
        for (int batch = max; batch >= 1; batch--) {
            if (canFitOutput(batch)) return batch;
        }
        return 0;
    }

    private boolean canContinue(int batch) {
        return batch > 0
                && countIngredient(0) >= batch
                && countIngredient(1) >= batch
                && countIngredient(2) >= batch
                && canFitOutput(batch);
    }

    private int countIngredient(int meta) {
        int count = 0;
        for (int slot = SLOT_INPUT_START; slot <= getLastEnabledInputSlot(); slot++) {
            ItemStack stack = inventory[slot];
            if (isTerraIngredient(stack, meta)) count += stack.stackSize;
        }
        return count;
    }

    private void consumeIngredient(int meta, int amount) {
        int left = amount;
        for (int slot = SLOT_INPUT_START; slot <= getLastEnabledInputSlot() && left > 0; slot++) {
            ItemStack stack = inventory[slot];
            if (!isTerraIngredient(stack, meta)) continue;
            int take = Math.min(left, stack.stackSize);
            stack.stackSize -= take;
            left -= take;
            if (stack.stackSize <= 0) inventory[slot] = null;
        }
    }

    private void craft(int batch) {
        internalInventoryMutation = true;
        try {
            consumeIngredient(0, batch);
            consumeIngredient(1, batch);
            consumeIngredient(2, batch);
            insertOutput(new ItemStack(ModItems.manaResource, batch, 4));
        } finally {
            internalInventoryMutation = false;
        }

        worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D,
                "botania:terrasteelCraft", 1F, 1F);
        resetOperation();
    }

    private void resetOperation() {
        progress = 0;
        activeBatch = 0;
    }

    private boolean canFitOutput(int batch) {
        ItemStack[] simulated = copyInventory();
        return insertIntoRange(simulated, new ItemStack(ModItems.manaResource, batch, 4),
                SLOT_OUTPUT_START, getLastEnabledOutputSlot());
    }

    private void insertOutput(ItemStack stack) {
        insertIntoRange(inventory, stack, SLOT_OUTPUT_START, getLastEnabledOutputSlot());
    }

    private boolean insertIntoRange(ItemStack[] target, ItemStack incoming, int first, int last) {
        if (incoming == null || incoming.stackSize <= 0) return true;
        int left = incoming.stackSize;

        for (int slot = first; slot <= last && left > 0; slot++) {
            ItemStack existing = target[slot];
            if (existing == null || !canStacksMerge(existing, incoming)) continue;
            int limit = Math.min(existing.getMaxStackSize(), getInventoryStackLimit());
            int room = limit - existing.stackSize;
            if (room <= 0) continue;
            int moved = Math.min(room, left);
            existing.stackSize += moved;
            left -= moved;
        }
        for (int slot = first; slot <= last && left > 0; slot++) {
            if (target[slot] != null) continue;
            ItemStack placed = incoming.copy();
            placed.stackSize = Math.min(left, Math.min(placed.getMaxStackSize(), getInventoryStackLimit()));
            target[slot] = placed;
            left -= placed.stackSize;
        }
        return left == 0;
    }

    private boolean insertInput(ItemStack[] target, ItemStack incoming) {
        return insertIntoRange(target, incoming, SLOT_INPUT_START, getLastEnabledInputSlot());
    }

    private boolean canStacksMerge(ItemStack a, ItemStack b) {
        return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    private ItemStack[] copyInventory() {
        ItemStack[] copy = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            copy[i] = inventory[i] == null ? null : inventory[i].copy();
        }
        return copy;
    }

    private boolean isTerraIngredient(ItemStack stack) {
        return stack != null && stack.getItem() == ModItems.manaResource
                && stack.getItemDamage() >= 0 && stack.getItemDamage() <= 2;
    }

    private boolean isTerraIngredient(ItemStack stack, int meta) {
        return stack != null && stack.getItem() == ModItems.manaResource && stack.getItemDamage() == meta;
    }

    public MachineTier getTier() {
        return worldObj == null ? MachineTier.MALACHITE
                : MachineTier.fromMeta(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
    }

    public int getManaCapacity() {
        return getTier().getManaCapacity();
    }

    public int getInputSlotCount() {
        switch (getTier()) {
            case SAFFRON: return 5;
            case SHADOW: return 7;
            case CRIMSON: return 14;
            case MALACHITE:
            default: return 3;
        }
    }

    public int getOutputSlotCount() {
        switch (getTier()) {
            case SAFFRON: return 6;
            case SHADOW: return 6;
            case CRIMSON: return 12;
            case MALACHITE:
            default: return 4;
        }
    }

    public int getUpgradeSlotCount() {
        MachineTier tier = getTier();
        return tier == MachineTier.SHADOW || tier == MachineTier.CRIMSON ? 2 : 0;
    }

    private int getLastEnabledInputSlot() {
        return SLOT_INPUT_START + getInputSlotCount() - 1;
    }

    private int getLastEnabledOutputSlot() {
        return SLOT_OUTPUT_START + getOutputSlotCount() - 1;
    }

    public int getProgress() {
        return progress;
    }

    public int getActiveBatch() {
        return activeBatch;
    }

    public int getManaPerTick() {
        return BASE_MANA_PER_TICK * (hasSpeedUpgrade() ? 4 : 1);
    }

    private boolean hasUpgrade(Item item) {
        for (int i = 0; i < getUpgradeSlotCount(); i++) {
            ItemStack stack = inventory[SLOT_UPGRADE_START + i];
            if (stack != null && stack.getItem() == item) return true;
        }
        return false;
    }

    public boolean hasInfiniteMana() {
        return hasUpgrade(DivineMachineryLegacy.catalystManaInfinity);
    }

    public boolean hasSpeedUpgrade() {
        return hasUpgrade(DivineMachineryLegacy.catalystSpeed);
    }

    private boolean isUpgrade(ItemStack stack) {
        return stack != null && (stack.getItem() == DivineMachineryLegacy.catalystManaInfinity
                || stack.getItem() == DivineMachineryLegacy.catalystSpeed);
    }

    // ---------------------------------------------------------------------
    // AE2 rv3 processing patterns
    // ---------------------------------------------------------------------

    @Override
    public boolean acceptsPlans() {
        return true;
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails pattern, InventoryCrafting table, ForgeDirection direction) {
        if (worldObj == null || worldObj.isRemote || pattern == null || table == null || pattern.isCraftable()) return false;

        int[] ingredientCounts = new int[3];
        ItemStack[] simulated = copyInventory();
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!isTerraIngredient(stack)) return false;
            ingredientCounts[stack.getItemDamage()] += stack.stackSize;
            if (!insertInput(simulated, stack.copy())) return false;
        }
        if (ingredientCounts[0] <= 0 || ingredientCounts[1] <= 0 || ingredientCounts[2] <= 0) return false;

        internalInventoryMutation = true;
        try {
            for (int i = 0; i < table.getSizeInventory(); i++) {
                ItemStack stack = table.getStackInSlot(i);
                if (stack != null && stack.stackSize > 0 && !insertInput(inventory, stack.copy())) return false;
            }
        } finally {
            internalInventoryMutation = false;
        }
        markDirty();
        return true;
    }

    // ---------------------------------------------------------------------
    // Botania mana / Spark API
    // ---------------------------------------------------------------------

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    public boolean isFull() {
        return mana >= getManaCapacity();
    }

    @Override
    public void recieveMana(int amount) {
        if (amount == 0) return;
        int before = mana;
        long next = (long) mana + amount;
        if (next < 0L) next = 0L;
        if (next > getManaCapacity()) next = getManaCapacity();
        mana = (int) next;
        if (before != mana) markDirty();
    }

    @Override
    public boolean canRecieveManaFromBursts() {
        return !isFull();
    }

    @Override
    public boolean isOutputtingPower() {
        return false;
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public void attachSpark(ISparkEntity spark) {
        // Botania 1.7.10 resolves Spark attachment spatially.
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, getManaCapacity() - mana);
    }

    @Override
    public ISparkEntity getAttachedSpark() {
        if (worldObj == null) return null;
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                xCoord, yCoord + 1, zCoord,
                xCoord + 1, yCoord + 2, zCoord + 1);
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
    public boolean areIncomingTranfersDone() {
        return false;
    }

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

    // ---------------------------------------------------------------------
    // Inventory / sided automation
    // ---------------------------------------------------------------------

    @Override
    public int getSizeInventory() {
        return INVENTORY_SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot >= 0 && slot < INVENTORY_SIZE ? inventory[slot] : null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = getStackInSlot(slot);
        if (stack == null) return null;
        ItemStack result;
        if (stack.stackSize <= amount) {
            result = stack;
            inventory[slot] = null;
        } else {
            result = stack.splitStack(amount);
            if (stack.stackSize <= 0) inventory[slot] = null;
        }
        inventoryChanged(slot);
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null) {
            inventory[slot] = null;
            inventoryChanged(slot);
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= INVENTORY_SIZE) return;
        inventory[slot] = stack;
        int limit = slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END ? 1 : getInventoryStackLimit();
        if (stack != null && stack.stackSize > limit) stack.stackSize = limit;
        inventoryChanged(slot);
    }

    private void inventoryChanged(int slot) {
        if (!internalInventoryMutation && slot < SLOT_OUTPUT_START) resetOperation();
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.divinemachinerylegacy.industrial_agglomeration";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj != null
                && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
    }

    @Override public void openInventory() { }
    @Override public void closeInventory() { }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
            return slot - SLOT_UPGRADE_START < getUpgradeSlotCount() && isUpgrade(stack);
        }
        return slot >= SLOT_INPUT_START && slot <= getLastEnabledInputSlot() && isTerraIngredient(stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int[] slots = new int[getUpgradeSlotCount() + getInputSlotCount() + getOutputSlotCount()];
        int p = 0;
        for (int i = 0; i < getUpgradeSlotCount(); i++) slots[p++] = SLOT_UPGRADE_START + i;
        for (int i = 0; i < getInputSlotCount(); i++) slots[p++] = SLOT_INPUT_START + i;
        for (int i = 0; i < getOutputSlotCount(); i++) slots[p++] = SLOT_OUTPUT_START + i;
        return slots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot >= SLOT_OUTPUT_START && slot <= getLastEnabledOutputSlot();
    }

    // ---------------------------------------------------------------------
    // Persistence / client sync helpers
    // ---------------------------------------------------------------------

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Mana", mana);
        tag.setInteger("Progress", progress);
        tag.setInteger("Batch", activeBatch);

        NBTTagList list = new NBTTagList();
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory[slot];
            if (stack == null) continue;
            NBTTagCompound entry = new NBTTagCompound();
            entry.setByte("Slot", (byte) slot);
            stack.writeToNBT(entry);
            list.appendTag(entry);
        }
        tag.setTag("Items", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        mana = Math.max(0, tag.getInteger("Mana"));
        progress = Math.max(0, Math.min(RECIPE_MANA, tag.getInteger("Progress")));
        activeBatch = Math.max(0, tag.getInteger("Batch"));

        for (int i = 0; i < INVENTORY_SIZE; i++) inventory[i] = null;
        NBTTagList list = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < INVENTORY_SIZE) inventory[slot] = ItemStack.loadItemStackFromNBT(entry);
        }
    }

    public void setClientMana(int value) {
        if (worldObj != null && worldObj.isRemote) mana = Math.max(0, value);
    }

    public void setClientProgress(int value) {
        if (worldObj != null && worldObj.isRemote) progress = Math.max(0, Math.min(RECIPE_MANA, value));
    }

    public void setClientBatch(int value) {
        if (worldObj != null && worldObj.isRemote) activeBatch = Math.max(0, value);
    }
}
