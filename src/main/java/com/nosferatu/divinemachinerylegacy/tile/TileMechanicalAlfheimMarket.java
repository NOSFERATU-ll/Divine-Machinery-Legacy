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
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.ManaNetworkEvent;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.recipe.RecipeElvenTrade;

import java.util.List;

/** Automated 1.7.10 Alfheim Portal trading machine based on Extra Reforked. */
public class TileMechanicalAlfheimMarket extends TileEntity
        implements ISidedInventory, IManaPool, ISparkAttachable, ICraftingMachine {

    public static final int SLOT_UPGRADE = 0;
    public static final int SLOT_INPUT_START = 1;
    public static final int SLOT_INPUT_END = 12;
    public static final int SLOT_OUTPUT_START = 13;
    public static final int SLOT_OUTPUT_END = 24;
    public static final int INVENTORY_SIZE = 25;

    public static final int RECIPE_MANA = 500;
    public static final int MANA_PER_TICK = 25;

    private final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
    private int mana;
    private int progress;
    private int activeBatch;
    private RecipeElvenTrade activeRecipe;
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

        if (activeRecipe == null) findRecipe();
        if (activeRecipe == null || activeBatch <= 0) return;

        Plan plan = plan(activeRecipe, activeBatch);
        if (plan == null || !canFitOutput(activeRecipe.getOutput(), activeBatch)) {
            resetOperation();
            markDirty();
            return;
        }

        int advance = Math.min(MANA_PER_TICK, RECIPE_MANA - progress);
        if (!hasInfiniteMana()) {
            advance = Math.min(advance, mana / activeBatch);
            if (advance <= 0) return;
            mana -= advance * activeBatch;
        }

        progress += advance;
        if (progress >= RECIPE_MANA) craft(plan);
        markDirty();
    }

    private void findRecipe() {
        for (RecipeElvenTrade recipe : BotaniaAPI.elvenTradeRecipes) {
            if (recipe == null || recipe.getOutput() == null) continue;
            for (int batch = getTier().getParallelCrafts(); batch >= 1; batch--) {
                if (plan(recipe, batch) == null) continue;
                if (!canFitOutput(recipe.getOutput(), batch)) continue;
                activeRecipe = recipe;
                activeBatch = batch;
                progress = 0;
                return;
            }
        }
    }

    private Plan plan(RecipeElvenTrade recipe, int batch) {
        int count = getInputSlotCount();
        int[] available = new int[count];
        int[] use = new int[count];
        for (int i = 0; i < count; i++) {
            ItemStack stack = inventory[SLOT_INPUT_START + i];
            available[i] = stack == null ? 0 : stack.stackSize;
        }

        List<Object> requirements = recipe.getInputs();
        for (int craft = 0; craft < batch; craft++) {
            for (Object requirement : requirements) {
                int found = -1;
                for (int i = 0; i < count; i++) {
                    if (available[i] > 0 && matches(requirement, inventory[SLOT_INPUT_START + i])) {
                        found = i;
                        break;
                    }
                }
                if (found < 0) return null;
                available[found]--;
                use[found]++;
            }
        }
        return new Plan(use);
    }

    private boolean matches(Object requirement, ItemStack stack) {
        if (stack == null) return false;
        if (requirement instanceof ItemStack) {
            ItemStack wanted = (ItemStack) requirement;
            return wanted.getItem() == stack.getItem() && wanted.getItemDamage() == stack.getItemDamage();
        }
        if (requirement instanceof String) {
            for (ItemStack ore : OreDictionary.getOres((String) requirement)) {
                if (OreDictionary.itemMatches(ore, stack, false)) return true;
            }
        }
        return false;
    }

    private boolean validInput(ItemStack stack) {
        if (stack == null) return false;
        for (RecipeElvenTrade recipe : BotaniaAPI.elvenTradeRecipes) {
            for (Object requirement : recipe.getInputs()) {
                if (matches(requirement, stack)) return true;
            }
        }
        return false;
    }

    private void craft(Plan plan) {
        ItemStack output = activeRecipe.getOutput().copy();
        output.stackSize *= activeBatch;

        internalInventoryMutation = true;
        try {
            for (int i = 0; i < plan.use.length; i++) {
                int amount = plan.use[i];
                if (amount <= 0) continue;
                ItemStack stack = inventory[SLOT_INPUT_START + i];
                stack.stackSize -= amount;
                if (stack.stackSize <= 0) inventory[SLOT_INPUT_START + i] = null;
            }
            insertIntoRange(inventory, output, SLOT_OUTPUT_START, getLastEnabledOutputSlot());
        } finally {
            internalInventoryMutation = false;
        }

        resetOperation();
        markDirty();
    }

    private boolean canFitOutput(ItemStack output, int batch) {
        ItemStack[] simulated = copyInventory();
        ItemStack result = output.copy();
        result.stackSize *= batch;
        return insertIntoRange(simulated, result, SLOT_OUTPUT_START, getLastEnabledOutputSlot());
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

    private void resetOperation() {
        activeRecipe = null;
        activeBatch = 0;
        progress = 0;
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
            case SAFFRON: return 6;
            case SHADOW: return 9;
            case CRIMSON: return 12;
            case MALACHITE:
            default: return 3;
        }
    }

    public int getOutputSlotCount() {
        return getInputSlotCount();
    }

    public boolean hasUpgradeSlot() {
        MachineTier tier = getTier();
        return tier == MachineTier.SHADOW || tier == MachineTier.CRIMSON;
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

    public boolean hasInfiniteMana() {
        ItemStack stack = inventory[SLOT_UPGRADE];
        return hasUpgradeSlot() && stack != null && stack.getItem() == DivineMachineryLegacy.catalystManaInfinity;
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

        ItemStack[] simulated = copyInventory();
        boolean any = false;
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) continue;
            any = true;
            if (!validInput(stack) || !insertInput(simulated, stack.copy())) return false;
        }
        if (!any) return false;

        internalInventoryMutation = true;
        try {
            for (int i = 0; i < table.getSizeInventory(); i++) {
                ItemStack stack = table.getStackInSlot(i);
                if (stack != null && stack.stackSize > 0 && !insertInput(inventory, stack.copy())) return false;
            }
        } finally {
            internalInventoryMutation = false;
        }
        resetOperation();
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
        // Botania 1.7.10 resolves attachment spatially.
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
        inventory[slot] = null;
        inventoryChanged(slot);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= INVENTORY_SIZE) return;
        inventory[slot] = stack;
        int limit = slot == SLOT_UPGRADE ? 1 : getInventoryStackLimit();
        if (stack != null && stack.stackSize > limit) stack.stackSize = limit;
        inventoryChanged(slot);
    }

    private void inventoryChanged(int slot) {
        if (!internalInventoryMutation && slot < SLOT_OUTPUT_START) resetOperation();
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.divinemachinerylegacy.alfheim_market";
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
        if (slot == SLOT_UPGRADE) {
            return hasUpgradeSlot() && stack != null && stack.getItem() == DivineMachineryLegacy.catalystManaInfinity;
        }
        return slot >= SLOT_INPUT_START && slot <= getLastEnabledInputSlot() && validInput(stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int[] slots = new int[(hasUpgradeSlot() ? 1 : 0) + getInputSlotCount() + getOutputSlotCount()];
        int p = 0;
        if (hasUpgradeSlot()) slots[p++] = SLOT_UPGRADE;
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
    // Persistence / client sync
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
        activeRecipe = null;

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

    private static final class Plan {
        final int[] use;
        Plan(int[] use) {
            this.use = use;
        }
    }
}
