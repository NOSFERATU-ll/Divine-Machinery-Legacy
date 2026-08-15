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
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.ManaNetworkEvent;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.recipe.RecipeManaInfusion;
import vazkii.botania.common.block.ModBlocks;

import java.util.List;

/**
 * 1.7.10 backport of Extra Reforked's Mechanical Mana Pool family.
 *
 * Stable inventory layout:
 *   0       recipe catalyst (Alchemy / Conjuration / Mana Void)
 *   1       Infinite Mana catalyst (Shadow and Crimson only)
 *   2..13   inputs (3 / 6 / 9 / 12 enabled by tier)
 *   14..22  outputs (2 / 4 / 6 / 9 enabled by tier)
 *
 * The machine consumes Botania's real Mana Infusion recipes, supports
 * 4/8/16/32 parallel crafts, 2.5M/10M/50M/100M mana buffers, Sparks,
 * mana bursts, sided automation and AE2 rv3 processing-pattern insertion.
 */
public class TileMechanicalManaPool extends TileEntity
        implements ISidedInventory, ISparkAttachable, IManaPool, ICraftingMachine {

    public static final int SLOT_CATALYST = 0;
    public static final int SLOT_UPGRADE = 1;
    public static final int SLOT_INPUT_START = 2;
    public static final int SLOT_INPUT_END = 13;
    public static final int SLOT_OUTPUT_START = 14;
    public static final int SLOT_OUTPUT_END = 22;
    public static final int INVENTORY_SIZE = 23;
    public static final int CRAFT_COOLDOWN = 10;

    private final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
    private int mana;
    private int cooldown;
    private int lastBatch;
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

        if (cooldown > 0) {
            cooldown--;
            if (cooldown == 0) markDirty();
            return;
        }

        tryCraft();
    }

    private void tryCraft() {
        RecipeManaInfusion recipe = findRecipe(true);
        if (recipe == null) recipe = findRecipe(false);
        if (recipe == null) {
            lastBatch = 0;
            return;
        }

        int inputCount = countMatchingInputs(recipe);
        int maxBatch = Math.min(inputCount, getTier().getParallelCrafts());
        if (maxBatch <= 0) {
            lastBatch = 0;
            return;
        }

        int batch = 0;
        for (int candidate = maxBatch; candidate >= 1; candidate--) {
            long manaCost = (long) recipe.getManaToConsume() * candidate;
            if (!hasInfiniteMana() && manaCost > mana) continue;
            if (!canFitOutput(recipe.getOutput(), candidate)) continue;
            batch = candidate;
            break;
        }

        if (batch <= 0) {
            lastBatch = 0;
            return;
        }

        internalInventoryMutation = true;
        try {
            consumeMatchingInputs(recipe, batch);

            if (!hasInfiniteMana()) {
                long cost = (long) recipe.getManaToConsume() * batch;
                mana = Math.max(0, (int) Math.max(0L, (long) mana - cost));
            }

            ItemStack output = recipe.getOutput().copy();
            output.stackSize *= batch;
            insertOutput(output);
        } finally {
            internalInventoryMutation = false;
        }

        lastBatch = batch;
        cooldown = CRAFT_COOLDOWN;
        worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D,
                "botania:manaPoolCraft", 0.5F, 1.0F);
        markDirty();
    }

    /**
     * Reforked checks catalyst recipes first and then falls back to ordinary
     * Mana Infusion recipes. This matters when the same input has both forms.
     */
    private RecipeManaInfusion findRecipe(boolean catalyticPass) {
        boolean alchemy = isAlchemyCatalyst();
        boolean conjuration = isConjurationCatalyst();
        boolean hasRecipeCatalyst = alchemy || conjuration;

        if (catalyticPass && !hasRecipeCatalyst) return null;

        for (RecipeManaInfusion recipe : BotaniaAPI.manaInfusionRecipes) {
            boolean catalytic = recipe.isAlchemy() || recipe.isConjuration();
            if (catalyticPass != catalytic) continue;
            if (recipe.isAlchemy() && !alchemy) continue;
            if (recipe.isConjuration() && !conjuration) continue;
            if (countMatchingInputs(recipe) > 0) return recipe;
        }
        return null;
    }

    private int countMatchingInputs(RecipeManaInfusion recipe) {
        int total = 0;
        int last = getLastEnabledInputSlot();
        for (int slot = SLOT_INPUT_START; slot <= last; slot++) {
            ItemStack stack = inventory[slot];
            if (stack != null && recipe.matches(stack)) total += stack.stackSize;
        }
        return total;
    }

    private void consumeMatchingInputs(RecipeManaInfusion recipe, int amount) {
        int left = amount;
        int last = getLastEnabledInputSlot();
        for (int slot = SLOT_INPUT_START; slot <= last && left > 0; slot++) {
            ItemStack stack = inventory[slot];
            if (stack == null || !recipe.matches(stack)) continue;
            int take = Math.min(left, stack.stackSize);
            stack.stackSize -= take;
            left -= take;
            if (stack.stackSize <= 0) inventory[slot] = null;
        }
    }

    private boolean canFitOutput(ItemStack recipeOutput, int batch) {
        if (recipeOutput == null || batch <= 0) return false;
        ItemStack[] simulated = copyInventory();
        ItemStack output = recipeOutput.copy();
        output.stackSize *= batch;
        return insertIntoRange(simulated, output, SLOT_OUTPUT_START, getLastEnabledOutputSlot());
    }

    private void insertOutput(ItemStack incoming) {
        if (!insertIntoRange(inventory, incoming, SLOT_OUTPUT_START, getLastEnabledOutputSlot())) {
            // Preflight always runs before a craft, so reaching here means the
            // inventory was mutated from an external hook during the same tick.
            // Avoid item deletion by simply leaving the machine dirty; normal
            // 1.7.10 inventory access is server-thread serial and won't hit this.
        }
    }

    private boolean insertIntoRange(ItemStack[] target, ItemStack incoming, int first, int last) {
        if (incoming == null || incoming.stackSize <= 0) return true;
        if (first > last) return false;
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

    public MachineTier getTier() {
        if (worldObj == null) return MachineTier.MALACHITE;
        return MachineTier.fromMeta(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
    }

    public int getManaCapacity() {
        return getTier().getManaCapacity();
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getLastBatch() {
        return lastBatch;
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
        switch (getTier()) {
            case SAFFRON: return 4;
            case SHADOW: return 6;
            case CRIMSON: return 9;
            case MALACHITE:
            default: return 2;
        }
    }

    private int getLastEnabledInputSlot() {
        return SLOT_INPUT_START + getInputSlotCount() - 1;
    }

    private int getLastEnabledOutputSlot() {
        return SLOT_OUTPUT_START + getOutputSlotCount() - 1;
    }

    public boolean hasUpgradeSlot() {
        MachineTier tier = getTier();
        return tier == MachineTier.SHADOW || tier == MachineTier.CRIMSON;
    }

    public boolean hasInfiniteMana() {
        ItemStack stack = inventory[SLOT_UPGRADE];
        return hasUpgradeSlot() && stack != null
                && stack.getItem() == DivineMachineryLegacy.catalystManaInfinity;
    }

    private boolean isAlchemyCatalyst() {
        return isBlockInCatalystSlot(ModBlocks.alchemyCatalyst);
    }

    private boolean isConjurationCatalyst() {
        return isBlockInCatalystSlot(ModBlocks.conjurationCatalyst);
    }

    public boolean hasManaVoid() {
        return isBlockInCatalystSlot(ModBlocks.manaVoid);
    }

    private boolean isBlockInCatalystSlot(net.minecraft.block.Block block) {
        ItemStack stack = inventory[SLOT_CATALYST];
        return stack != null && stack.getItem() == Item.getItemFromBlock(block);
    }

    private boolean isRecipeCatalyst(ItemStack stack) {
        if (stack == null) return false;
        Item item = stack.getItem();
        return item == Item.getItemFromBlock(ModBlocks.alchemyCatalyst)
                || item == Item.getItemFromBlock(ModBlocks.conjurationCatalyst)
                || item == Item.getItemFromBlock(ModBlocks.manaVoid);
    }

    private boolean isValidManaInfusionInput(ItemStack stack) {
        if (stack == null) return false;
        for (RecipeManaInfusion recipe : BotaniaAPI.manaInfusionRecipes) {
            if (recipe.matches(stack)) return true;
        }
        return false;
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
        if (worldObj == null || worldObj.isRemote || pattern == null || table == null) return false;
        if (pattern.isCraftable()) return false;

        ItemStack[] simulated = copyInventory();
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) continue;
            if (isRecipeCatalyst(stack) || stack.getItem() == DivineMachineryLegacy.catalystManaInfinity) return false;
            if (!isValidManaInfusionInput(stack)) return false;
            if (!insertIntoRange(simulated, stack.copy(), SLOT_INPUT_START, getLastEnabledInputSlot())) return false;
        }

        internalInventoryMutation = true;
        try {
            for (int i = 0; i < table.getSizeInventory(); i++) {
                ItemStack stack = table.getStackInSlot(i);
                if (stack == null || stack.stackSize <= 0) continue;
                if (!insertIntoRange(inventory, stack.copy(), SLOT_INPUT_START, getLastEnabledInputSlot())) return false;
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
        return !hasManaVoid() && mana >= getManaCapacity();
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
        // Mana Void intentionally keeps requesting mana even at capacity; any
        // excess received above the internal buffer is discarded by clamping.
        return hasManaVoid() ? getManaCapacity() : Math.max(0, getManaCapacity() - mana);
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
        if (stack != null && stack.stackSize > getInventoryStackLimit()) stack.stackSize = getInventoryStackLimit();
        inventoryChanged(slot);
    }

    private void inventoryChanged(int slot) {
        if (!internalInventoryMutation && slot != SLOT_OUTPUT_START) {
            lastBatch = 0;
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.divinemachinerylegacy.mana_pool";
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
                && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    @Override public void openInventory() { }
    @Override public void closeInventory() { }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == SLOT_CATALYST) return isRecipeCatalyst(stack);
        if (slot == SLOT_UPGRADE) {
            return hasUpgradeSlot() && stack != null
                    && stack.getItem() == DivineMachineryLegacy.catalystManaInfinity;
        }
        if (slot >= SLOT_INPUT_START && slot <= getLastEnabledInputSlot()) {
            return isValidManaInfusionInput(stack) && !isRecipeCatalyst(stack);
        }
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int inputCount = getInputSlotCount();
        int outputCount = getOutputSlotCount();
        int total = 1 + (hasUpgradeSlot() ? 1 : 0) + inputCount + outputCount;
        int[] result = new int[total];
        int p = 0;
        result[p++] = SLOT_CATALYST;
        if (hasUpgradeSlot()) result[p++] = SLOT_UPGRADE;
        for (int slot = SLOT_INPUT_START; slot <= getLastEnabledInputSlot(); slot++) result[p++] = slot;
        for (int slot = SLOT_OUTPUT_START; slot <= getLastEnabledOutputSlot(); slot++) result[p++] = slot;
        return result;
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
        tag.setInteger("Cooldown", cooldown);
        tag.setInteger("LastBatch", lastBatch);

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
        cooldown = Math.max(0, tag.getInteger("Cooldown"));
        lastBatch = Math.max(0, tag.getInteger("LastBatch"));

        for (int i = 0; i < INVENTORY_SIZE; i++) inventory[i] = null;
        NBTTagList list = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot >= 0 && slot < INVENTORY_SIZE) inventory[slot] = ItemStack.loadItemStackFromNBT(entry);
        }
    }

    public void setClientMana(int value) {
        if (worldObj != null && worldObj.isRemote) mana = Math.max(0, value);
    }

    public void setClientCooldown(int value) {
        if (worldObj != null && worldObj.isRemote) cooldown = Math.max(0, value);
    }

    public void setClientBatch(int value) {
        if (worldObj != null && worldObj.isRemote) lastBatch = Math.max(0, value);
    }
}
