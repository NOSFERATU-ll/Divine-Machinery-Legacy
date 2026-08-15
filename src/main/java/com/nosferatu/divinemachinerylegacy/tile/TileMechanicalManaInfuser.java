package com.nosferatu.divinemachinerylegacy.tile;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.recipe.ManaInfuserRecipe;
import com.nosferatu.divinemachinerylegacy.recipe.ManaInfuserRecipes;
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

import java.util.List;

/** 1.7.10 Mechanical Mana Infuser, replacing the missing MythicBotany recipe layer. */
public class TileMechanicalManaInfuser extends TileEntity
        implements ISidedInventory, ISparkAttachable, IManaPool, ICraftingMachine {
    public static final int SLOT_UPGRADE_START = 0;
    public static final int SLOT_UPGRADE_END = 1;
    public static final int SLOT_INPUT_START = 2;
    public static final int SLOT_INPUT_END = 15;
    public static final int SLOT_OUTPUT_START = 16;
    public static final int SLOT_OUTPUT_END = 27;
    public static final int INVENTORY_SIZE = 28;
    public static final int BASE_MANA_PER_TICK = 5000;

    private final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
    private int mana;
    private int progress;
    private int activeRecipe = -1;
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

        ManaInfuserRecipe recipe = ManaInfuserRecipes.get(activeRecipe);
        if (recipe == null) {
            chooseOperation();
            recipe = ManaInfuserRecipes.get(activeRecipe);
            if (recipe == null) return;
        }

        if (!isTierAllowed(recipe) || recipe.getMaxCrafts(inventory, SLOT_INPUT_START,
                getLastEnabledInputSlot(), activeBatch) < activeBatch || !canFitOutput(recipe, activeBatch)) {
            resetOperation();
            markDirty();
            return;
        }

        int remaining = recipe.getManaCost() - progress;
        int advance = Math.min(getManaPerTick(), remaining);
        if (!hasInfiniteMana()) {
            advance = Math.min(advance, mana / Math.max(1, activeBatch));
            if (advance <= 0) return;
            mana -= advance * activeBatch;
        }
        progress += advance;

        if (progress >= recipe.getManaCost()) finishCraft(recipe);
        markDirty();
    }

    private void chooseOperation() {
        int index = 0;
        for (ManaInfuserRecipe recipe : ManaInfuserRecipes.all()) {
            if (isTierAllowed(recipe)) {
                int max = recipe.getMaxCrafts(inventory, SLOT_INPUT_START, getLastEnabledInputSlot(),
                        getTier().getParallelCrafts());
                for (int batch = max; batch >= 1; batch--) {
                    if (canFitOutput(recipe, batch)) {
                        activeRecipe = index;
                        activeBatch = batch;
                        progress = 0;
                        return;
                    }
                }
            }
            index++;
        }
    }

    private boolean isTierAllowed(ManaInfuserRecipe recipe) {
        return recipe != null && getTier().ordinal() >= recipe.getMinimumTier();
    }

    private void finishCraft(ManaInfuserRecipe recipe) {
        internalInventoryMutation = true;
        try {
            recipe.consume(inventory, SLOT_INPUT_START, getLastEnabledInputSlot(), activeBatch);
            ItemStack output = recipe.getOutput();
            output.stackSize *= activeBatch;
            insertIntoRange(inventory, output, SLOT_OUTPUT_START, getLastEnabledOutputSlot());
        } finally {
            internalInventoryMutation = false;
        }
        worldObj.playSoundEffect(xCoord + .5D, yCoord + .5D, zCoord + .5D,
                "botania:manaPoolCraft", .7F, 1.15F);
        resetOperation();
    }

    private void resetOperation() {
        activeRecipe = -1;
        activeBatch = 0;
        progress = 0;
    }

    private boolean canFitOutput(ManaInfuserRecipe recipe, int batch) {
        ItemStack[] simulated = copyInventory();
        ItemStack output = recipe.getOutput();
        output.stackSize *= batch;
        return insertIntoRange(simulated, output, SLOT_OUTPUT_START, getLastEnabledOutputSlot());
    }

    private boolean insertInput(ItemStack[] target, ItemStack incoming) {
        return insertIntoRange(target, incoming, SLOT_INPUT_START, getLastEnabledInputSlot());
    }

    private boolean insertIntoRange(ItemStack[] target, ItemStack incoming, int first, int last) {
        if (incoming == null || incoming.stackSize <= 0) return true;
        int left = incoming.stackSize;
        for (int slot = first; slot <= last && left > 0; slot++) {
            ItemStack existing = target[slot];
            if (existing == null || !canStacksMerge(existing, incoming)) continue;
            int room = Math.min(existing.getMaxStackSize(), getInventoryStackLimit()) - existing.stackSize;
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

    private static boolean canStacksMerge(ItemStack a, ItemStack b) {
        return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    private ItemStack[] copyInventory() {
        ItemStack[] copy = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) copy[i] = inventory[i] == null ? null : inventory[i].copy();
        return copy;
    }

    public MachineTier getTier() {
        return worldObj == null ? MachineTier.MALACHITE
                : MachineTier.fromMeta(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
    }
    public int getManaCapacity() { return getTier().getManaCapacity(); }
    public int getProgress() { return progress; }
    public int getMaxProgress() {
        ManaInfuserRecipe recipe = ManaInfuserRecipes.get(activeRecipe);
        return recipe == null ? 0 : recipe.getManaCost();
    }
    public int getActiveBatch() { return activeBatch; }
    public int getManaPerTick() { return BASE_MANA_PER_TICK * (hasSpeedUpgrade() ? 4 : 1); }

    public int getInputSlotCount() {
        switch (getTier()) {
            case SAFFRON: return 5;
            case SHADOW: return 7;
            case CRIMSON: return 14;
            default: return 3;
        }
    }
    public int getOutputSlotCount() {
        switch (getTier()) {
            case SAFFRON: return 6;
            case SHADOW: return 6;
            case CRIMSON: return 12;
            default: return 4;
        }
    }
    public int getUpgradeSlotCount() {
        MachineTier tier = getTier();
        return tier == MachineTier.SHADOW || tier == MachineTier.CRIMSON ? 2 : 0;
    }
    private int getLastEnabledInputSlot() { return SLOT_INPUT_START + getInputSlotCount() - 1; }
    private int getLastEnabledOutputSlot() { return SLOT_OUTPUT_START + getOutputSlotCount() - 1; }

    private boolean hasUpgrade(Item item) {
        for (int i = 0; i < getUpgradeSlotCount(); i++) {
            ItemStack stack = inventory[SLOT_UPGRADE_START + i];
            if (stack != null && stack.getItem() == item) return true;
        }
        return false;
    }
    public boolean hasInfiniteMana() { return hasUpgrade(DivineMachineryLegacy.catalystManaInfinity); }
    public boolean hasSpeedUpgrade() { return hasUpgrade(DivineMachineryLegacy.catalystSpeed); }
    private boolean isUpgrade(ItemStack stack) {
        return stack != null && (stack.getItem() == DivineMachineryLegacy.catalystManaInfinity
                || stack.getItem() == DivineMachineryLegacy.catalystSpeed);
    }

    @Override public boolean acceptsPlans() { return true; }
    @Override
    public boolean pushPattern(ICraftingPatternDetails pattern, InventoryCrafting table, ForgeDirection direction) {
        if (worldObj == null || worldObj.isRemote || pattern == null || table == null || pattern.isCraftable()) return false;
        ItemStack[] simulated = copyInventory();
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!ManaInfuserRecipes.isIngredient(stack) || !insertInput(simulated, stack.copy())) return false;
        }
        boolean makesRecipe = false;
        for (ManaInfuserRecipe recipe : ManaInfuserRecipes.all()) {
            if (isTierAllowed(recipe) && recipe.getMaxCrafts(simulated, SLOT_INPUT_START, getLastEnabledInputSlot(), 1) > 0) {
                makesRecipe = true;
                break;
            }
        }
        if (!makesRecipe) return false;
        internalInventoryMutation = true;
        try {
            for (int i = 0; i < table.getSizeInventory(); i++) {
                ItemStack stack = table.getStackInSlot(i);
                if (stack != null && stack.stackSize > 0 && !insertInput(inventory, stack.copy())) return false;
            }
        } finally { internalInventoryMutation = false; }
        resetOperation();
        markDirty();
        return true;
    }

    @Override public int getCurrentMana() { return mana; }
    @Override public boolean isFull() { return mana >= getManaCapacity(); }
    @Override
    public void recieveMana(int amount) {
        if (amount == 0) return;
        long next = (long) mana + amount;
        mana = (int) Math.max(0L, Math.min((long) getManaCapacity(), next));
        markDirty();
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
        for (Object object : entities) {
            if (!(object instanceof ISparkEntity)) continue;
            if (found != null) return null;
            found = (ISparkEntity) object;
        }
        return found;
    }
    @Override public boolean areIncomingTranfersDone() { return false; }

    @Override
    public void invalidate() {
        if (registeredInManaNetwork) ManaNetworkEvent.removePool(this);
        registeredInManaNetwork = false;
        super.invalidate();
    }
    @Override
    public void onChunkUnload() {
        if (registeredInManaNetwork) ManaNetworkEvent.removePool(this);
        registeredInManaNetwork = false;
        super.onChunkUnload();
    }

    @Override public int getSizeInventory() { return INVENTORY_SIZE; }
    @Override public ItemStack getStackInSlot(int slot) { return slot >= 0 && slot < INVENTORY_SIZE ? inventory[slot] : null; }
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack stack = getStackInSlot(slot);
        if (stack == null) return null;
        ItemStack result;
        if (stack.stackSize <= amount) { result = stack; inventory[slot] = null; }
        else { result = stack.splitStack(amount); if (stack.stackSize <= 0) inventory[slot] = null; }
        inventoryChanged(slot);
        return result;
    }
    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = getStackInSlot(slot);
        if (stack != null) { inventory[slot] = null; inventoryChanged(slot); }
        return stack;
    }
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= INVENTORY_SIZE) return;
        inventory[slot] = stack;
        int limit = slot <= SLOT_UPGRADE_END ? 1 : getInventoryStackLimit();
        if (stack != null && stack.stackSize > limit) stack.stackSize = limit;
        inventoryChanged(slot);
    }
    private void inventoryChanged(int slot) {
        if (!internalInventoryMutation && slot < SLOT_OUTPUT_START) resetOperation();
        markDirty();
    }
    @Override public String getInventoryName() { return "container.divinemachinerylegacy.mana_infuser"; }
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
        if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END)
            return slot - SLOT_UPGRADE_START < getUpgradeSlotCount() && isUpgrade(stack);
        if (slot >= SLOT_INPUT_START && slot <= getLastEnabledInputSlot()) return ManaInfuserRecipes.isIngredient(stack);
        return false;
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
    @Override public boolean canInsertItem(int slot, ItemStack stack, int side) { return isItemValidForSlot(slot, stack); }
    @Override public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot >= SLOT_OUTPUT_START && slot <= getLastEnabledOutputSlot();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Mana", mana);
        tag.setInteger("Progress", progress);
        tag.setInteger("ActiveRecipe", activeRecipe);
        tag.setInteger("ActiveBatch", activeBatch);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) if (inventory[i] != null) {
            NBTTagCompound stackTag = new NBTTagCompound();
            stackTag.setByte("Slot", (byte) i);
            inventory[i].writeToNBT(stackTag);
            list.appendTag(stackTag);
        }
        tag.setTag("Items", list);
    }
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        mana = tag.getInteger("Mana");
        progress = tag.getInteger("Progress");
        activeRecipe = tag.getInteger("ActiveRecipe");
        activeBatch = tag.getInteger("ActiveBatch");
        for (int i = 0; i < inventory.length; i++) inventory[i] = null;
        NBTTagList list = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound stackTag = list.getCompoundTagAt(i);
            int slot = stackTag.getByte("Slot") & 255;
            if (slot < inventory.length) inventory[slot] = ItemStack.loadItemStackFromNBT(stackTag);
        }
    }

    public void setClientMana(int value) { mana = Math.max(0, value); }
    public void setClientProgress(int value) { progress = Math.max(0, value); }
    public void setClientActiveRecipe(int value) { activeRecipe = value; }
    public void setClientActiveBatch(int value) { activeBatch = Math.max(0, value); }
}
