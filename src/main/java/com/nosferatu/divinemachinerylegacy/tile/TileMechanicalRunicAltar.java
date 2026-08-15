package com.nosferatu.divinemachinerylegacy.tile;

import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.recipe.RecipeRuneAltar;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;

import java.util.ArrayList;
import java.util.List;

/**
 * First native 1.7.10 port milestone for Botanical Machinery-style automation.
 *
 * Inventory layout is deliberately fixed across all tiers so future AE2 and GUI
 * code can target stable slot numbers:
 *   0..2   Livingrock
 *   3..4   reserved upgrade slots
 *   5..20  rune altar inputs
 *   21..36 outputs
 *
 * Tier metadata controls which Livingrock/upgrade slots are actually enabled.
 */
public class TileMechanicalRunicAltar extends TileEntity implements ISidedInventory, ISparkAttachable {

    public static final int SLOT_LIVINGROCK_START = 0;
    public static final int SLOT_LIVINGROCK_END = 2;
    public static final int SLOT_UPGRADE_START = 3;
    public static final int SLOT_UPGRADE_END = 4;
    public static final int SLOT_INPUT_START = 5;
    public static final int SLOT_INPUT_END = 20;
    public static final int SLOT_OUTPUT_START = 21;
    public static final int SLOT_OUTPUT_END = 36;
    public static final int INVENTORY_SIZE = 37;

    /** Mirrors the current Extra Reforked default: 100 mana progress per craft per tick. */
    public static final int MAX_MANA_PROGRESS_PER_TICK = 100;

    private final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];

    private int mana;
    private int progress;
    private int currentBatch;
    private RecipeRuneAltar currentRecipe;
    private MatchPlan currentPlan;
    private int pendingRecipeIndex = -1;

    private boolean internalInventoryMutation;

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) return;

        if (pendingRecipeIndex >= 0) {
            restoreSavedProcess();
        }

        if (currentRecipe == null) {
            findProcess();
        }

        if (currentRecipe == null || currentBatch <= 0) return;

        int recipeMana = currentRecipe.getManaUsage();
        int remaining = recipeMana - progress;
        if (remaining <= 0) {
            completeCurrentProcess();
            return;
        }

        int byBuffer = mana / currentBatch;
        int manaProgress = Math.min(Math.min(MAX_MANA_PROGRESS_PER_TICK, remaining), byBuffer);
        if (manaProgress <= 0) return;

        mana -= manaProgress * currentBatch;
        progress += manaProgress;
        markDirty();

        if (progress >= recipeMana) {
            completeCurrentProcess();
        }
    }

    private void findProcess() {
        MachineTier tier = getTier();
        for (RecipeRuneAltar recipe : BotaniaAPI.runeAltarRecipes) {
            for (int batch = tier.getParallelCrafts(); batch >= 1; batch--) {
                MatchPlan plan = buildPlan(recipe, batch);
                if (plan == null) continue;
                if (getLivingrockCount() < batch) continue;
                if (!canFitResults(recipe, batch, plan.runeReturns)) continue;

                currentRecipe = recipe;
                currentBatch = batch;
                currentPlan = plan;
                progress = 0;
                markDirty();
                return;
            }
        }
    }

    private void restoreSavedProcess() {
        int index = pendingRecipeIndex;
        pendingRecipeIndex = -1;

        if (index < 0 || index >= BotaniaAPI.runeAltarRecipes.size() || currentBatch <= 0) {
            resetProcess();
            return;
        }

        RecipeRuneAltar recipe = BotaniaAPI.runeAltarRecipes.get(index);
        MatchPlan plan = buildPlan(recipe, currentBatch);
        if (plan == null || getLivingrockCount() < currentBatch || !canFitResults(recipe, currentBatch, plan.runeReturns)) {
            resetProcess();
            return;
        }

        currentRecipe = recipe;
        currentPlan = plan;
        if (progress < 0 || progress > recipe.getManaUsage()) progress = 0;
    }

    private MatchPlan buildPlan(RecipeRuneAltar recipe, int batch) {
        if (batch <= 0) return null;

        int inputSlots = SLOT_INPUT_END - SLOT_INPUT_START + 1;
        int[] available = new int[inputSlots];
        int[] consume = new int[inputSlots];
        List<ItemStack> runeReturns = new ArrayList<ItemStack>();

        for (int i = 0; i < inputSlots; i++) {
            ItemStack stack = inventory[SLOT_INPUT_START + i];
            available[i] = stack == null ? 0 : stack.stackSize;
        }

        List<Object> requirements = recipe.getInputs();
        for (int craft = 0; craft < batch; craft++) {
            for (Object requirement : requirements) {
                int matched = -1;
                for (int i = 0; i < inputSlots; i++) {
                    if (available[i] <= 0) continue;
                    ItemStack candidate = inventory[SLOT_INPUT_START + i];
                    if (candidate != null && matchesRequirement(requirement, candidate)) {
                        matched = i;
                        break;
                    }
                }

                if (matched < 0) return null;

                available[matched]--;
                consume[matched]++;

                ItemStack actual = inventory[SLOT_INPUT_START + matched];
                if (actual != null && actual.getItem() == ModItems.rune) {
                    ItemStack returned = actual.copy();
                    returned.stackSize = 1;
                    addOrMerge(runeReturns, returned);
                }
            }
        }

        return new MatchPlan(consume, runeReturns);
    }

    private boolean matchesRequirement(Object requirement, ItemStack candidate) {
        if (requirement instanceof String) {
            List<ItemStack> ores = OreDictionary.getOres((String) requirement);
            for (ItemStack ore : ores) {
                if (OreDictionary.itemMatches(ore, candidate, false)) return true;
            }
            return false;
        }

        if (requirement instanceof ItemStack) {
            ItemStack wanted = (ItemStack) requirement;
            if (wanted.getItem() != candidate.getItem()) return false;
            int wantedMeta = wanted.getItemDamage();
            return wantedMeta == OreDictionary.WILDCARD_VALUE || wantedMeta == candidate.getItemDamage();
        }

        return false;
    }

    private void completeCurrentProcess() {
        if (currentRecipe == null || currentBatch <= 0) {
            resetProcess();
            return;
        }

        // Rebuild just before committing so automation cannot make the machine
        // consume stale inputs after a pipe/player changed the inventory.
        MatchPlan plan = buildPlan(currentRecipe, currentBatch);
        if (plan == null || getLivingrockCount() < currentBatch || !canFitResults(currentRecipe, currentBatch, plan.runeReturns)) {
            resetProcess();
            return;
        }

        internalInventoryMutation = true;
        try {
            for (int i = 0; i < plan.consume.length; i++) {
                int amount = plan.consume[i];
                if (amount <= 0) continue;
                int slot = SLOT_INPUT_START + i;
                ItemStack stack = inventory[slot];
                stack.stackSize -= amount;
                if (stack.stackSize <= 0) inventory[slot] = null;
            }

            removeLivingrock(currentBatch);

            ItemStack output = currentRecipe.getOutput().copy();
            output.stackSize *= currentBatch;
            insertOutput(output);

            for (ItemStack rune : plan.runeReturns) {
                insertOutput(rune.copy());
            }
        } finally {
            internalInventoryMutation = false;
        }

        worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D,
                "botania:runeAltarCraft", 1.0F, 1.0F);

        resetProcess();
        markDirty();
    }

    private void removeLivingrock(int amount) {
        int left = amount;
        int allowed = getTier().getLivingrockSlots();
        for (int i = 0; i < allowed && left > 0; i++) {
            int slot = SLOT_LIVINGROCK_START + i;
            ItemStack stack = inventory[slot];
            if (stack == null) continue;
            int take = Math.min(left, stack.stackSize);
            stack.stackSize -= take;
            left -= take;
            if (stack.stackSize <= 0) inventory[slot] = null;
        }
    }

    private int getLivingrockCount() {
        int total = 0;
        int allowed = getTier().getLivingrockSlots();
        for (int i = 0; i < allowed; i++) {
            ItemStack stack = inventory[SLOT_LIVINGROCK_START + i];
            if (isLivingrock(stack)) total += stack.stackSize;
        }
        return total;
    }

    private boolean isLivingrock(ItemStack stack) {
        return stack != null
                && stack.getItem() == Item.getItemFromBlock(ModBlocks.livingrock)
                && stack.getItemDamage() == 0;
    }

    private boolean canFitResults(RecipeRuneAltar recipe, int batch, List<ItemStack> runeReturns) {
        ItemStack[] simulated = new ItemStack[SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1];
        for (int i = 0; i < simulated.length; i++) {
            ItemStack real = inventory[SLOT_OUTPUT_START + i];
            simulated[i] = real == null ? null : real.copy();
        }

        ItemStack output = recipe.getOutput().copy();
        output.stackSize *= batch;
        if (!simulateInsert(simulated, output)) return false;

        for (ItemStack rune : runeReturns) {
            if (!simulateInsert(simulated, rune.copy())) return false;
        }
        return true;
    }

    private boolean simulateInsert(ItemStack[] slots, ItemStack incoming) {
        int left = incoming.stackSize;

        for (int i = 0; i < slots.length && left > 0; i++) {
            ItemStack existing = slots[i];
            if (existing == null) continue;
            if (!canStacksMerge(existing, incoming)) continue;
            int room = Math.min(existing.getMaxStackSize(), getInventoryStackLimit()) - existing.stackSize;
            if (room <= 0) continue;
            int moved = Math.min(room, left);
            existing.stackSize += moved;
            left -= moved;
        }

        for (int i = 0; i < slots.length && left > 0; i++) {
            if (slots[i] != null) continue;
            ItemStack placed = incoming.copy();
            placed.stackSize = Math.min(left, Math.min(placed.getMaxStackSize(), getInventoryStackLimit()));
            slots[i] = placed;
            left -= placed.stackSize;
        }

        return left == 0;
    }

    private void insertOutput(ItemStack incoming) {
        int left = incoming.stackSize;

        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && left > 0; slot++) {
            ItemStack existing = inventory[slot];
            if (existing == null || !canStacksMerge(existing, incoming)) continue;
            int room = Math.min(existing.getMaxStackSize(), getInventoryStackLimit()) - existing.stackSize;
            if (room <= 0) continue;
            int moved = Math.min(room, left);
            existing.stackSize += moved;
            left -= moved;
        }

        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && left > 0; slot++) {
            if (inventory[slot] != null) continue;
            ItemStack placed = incoming.copy();
            placed.stackSize = Math.min(left, Math.min(placed.getMaxStackSize(), getInventoryStackLimit()));
            inventory[slot] = placed;
            left -= placed.stackSize;
        }

        if (left > 0 && worldObj != null && !worldObj.isRemote) {
            ItemStack dropped = incoming.copy();
            dropped.stackSize = left;
            worldObj.spawnEntityInWorld(new EntityItem(worldObj,
                    xCoord + 0.5D, yCoord + 1.0D, zCoord + 0.5D, dropped));
        }
    }

    private boolean canStacksMerge(ItemStack a, ItemStack b) {
        return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    private void addOrMerge(List<ItemStack> list, ItemStack incoming) {
        for (ItemStack existing : list) {
            if (canStacksMerge(existing, incoming)) {
                existing.stackSize += incoming.stackSize;
                return;
            }
        }
        list.add(incoming.copy());
    }

    private void resetProcess() {
        currentRecipe = null;
        currentPlan = null;
        currentBatch = 0;
        progress = 0;
        pendingRecipeIndex = -1;
    }

    private void inventoryChanged(int slot) {
        if (internalInventoryMutation) return;
        if (slot < SLOT_OUTPUT_START) {
            resetProcess();
        }
        markDirty();
    }

    public MachineTier getTier() {
        if (worldObj == null) return MachineTier.MALACHITE;
        return MachineTier.fromMeta(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
    }

    public int getManaCapacity() {
        return getTier().getManaCapacity();
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return currentRecipe == null ? 0 : currentRecipe.getManaUsage();
    }

    public int getCurrentBatch() {
        return currentBatch;
    }

    // Client GUI synchronization only.
    public void setClientMana(int value) {
        if (worldObj != null && worldObj.isRemote) mana = Math.max(0, value);
    }

    public void setClientProgress(int value) {
        if (worldObj != null && worldObj.isRemote) progress = Math.max(0, value);
    }

    public void setClientMaxProgress(int value) {
        // The client has no RecipeRuneAltar object from container sync, so the GUI
        // stores this value separately in the container. Kept for API symmetry.
    }

    public void setClientBatch(int value) {
        if (worldObj != null && worldObj.isRemote) currentBatch = Math.max(0, value);
    }

    // ---------------------------------------------------------------------
    // Botania mana/spark API
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
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public void attachSpark(ISparkEntity spark) {
        // Botania 1.7.10 pools use spatial lookup instead of storing the spark.
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
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        inventoryChanged(slot);
    }

    @Override
    public String getInventoryName() {
        return "container.divinemachinerylegacy.runic_altar";
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

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot >= SLOT_LIVINGROCK_START && slot <= SLOT_LIVINGROCK_END) {
            int local = slot - SLOT_LIVINGROCK_START;
            return local < getTier().getLivingrockSlots() && isLivingrock(stack);
        }

        // Upgrade items are intentionally reserved until the catalyst item layer
        // is ported. This prevents arbitrary items from occupying future slots.
        if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) return false;

        if (slot >= SLOT_INPUT_START && slot <= SLOT_INPUT_END) {
            return stack != null && !isLivingrock(stack);
        }

        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int living = getTier().getLivingrockSlots();
        int total = living + (SLOT_INPUT_END - SLOT_INPUT_START + 1) + (SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1);
        int[] result = new int[total];
        int p = 0;
        for (int i = 0; i < living; i++) result[p++] = SLOT_LIVINGROCK_START + i;
        for (int slot = SLOT_INPUT_START; slot <= SLOT_INPUT_END; slot++) result[p++] = slot;
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) result[p++] = slot;
        return result;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot >= SLOT_OUTPUT_START && slot <= SLOT_OUTPUT_END;
    }

    // ---------------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------------

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Mana", mana);
        tag.setInteger("Progress", progress);
        tag.setInteger("Batch", currentBatch);

        int recipeIndex = currentRecipe == null ? -1 : BotaniaAPI.runeAltarRecipes.indexOf(currentRecipe);
        tag.setInteger("RecipeIndex", recipeIndex);

        NBTTagList items = new NBTTagList();
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory[slot];
            if (stack == null) continue;
            NBTTagCompound item = new NBTTagCompound();
            item.setByte("Slot", (byte) slot);
            stack.writeToNBT(item);
            items.appendTag(item);
        }
        tag.setTag("Items", items);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        mana = Math.max(0, tag.getInteger("Mana"));
        progress = Math.max(0, tag.getInteger("Progress"));
        currentBatch = Math.max(0, tag.getInteger("Batch"));
        pendingRecipeIndex = tag.getInteger("RecipeIndex");
        currentRecipe = null;
        currentPlan = null;

        for (int i = 0; i < inventory.length; i++) inventory[i] = null;
        NBTTagList items = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound item = items.getCompoundTagAt(i);
            int slot = item.getByte("Slot") & 255;
            if (slot >= 0 && slot < INVENTORY_SIZE) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(item);
            }
        }
    }

    private static final class MatchPlan {
        private final int[] consume;
        private final List<ItemStack> runeReturns;

        private MatchPlan(int[] consume, List<ItemStack> runeReturns) {
            this.consume = consume;
            this.runeReturns = runeReturns;
        }
    }
}
