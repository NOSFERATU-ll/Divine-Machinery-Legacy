package com.nosferatu.divinemachinerylegacy.tile;

import WayofTime.alchemicalWizardry.api.alchemy.AlchemyRecipe;
import WayofTime.alchemicalWizardry.api.alchemy.AlchemyRecipeRegistry;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipe;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipeRegistry;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicContent;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternData;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternDetails;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternKind;
import com.nosferatu.divinemachinerylegacy.config.BloodMagicAddonConfig;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Pattern-aware Blood Altar Assembler matching BloodMagic Additions 1.0.4.
 *
 * Unlike the early port prototype this machine is intentionally pattern-only:
 * it does not expose a hidden manual input queue and it refuses ordinary AE2
 * processing patterns. The nine installed Blood Patterns are the authoritative
 * crafting list, just like in bmaddon.
 */
public class TileBloodAltarAssemblerExtended extends TileBloodAltarAssembler {
    public static final int SLOT_PATTERN_START = TileBloodAltarAssembler.INVENTORY_SIZE;
    public static final int PATTERN_SLOT_COUNT = 9;
    public static final int SLOT_PATTERN_END = SLOT_PATTERN_START + PATTERN_SLOT_COUNT - 1;
    public static final int EXTENDED_INVENTORY_SIZE = SLOT_PATTERN_END + 1;

    private final ItemStack[] patternInventory = new ItemStack[PATTERN_SLOT_COUNT];
    private final List<PatternJob> patternJobs = new ArrayList<PatternJob>();

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote || patternJobs.isEmpty()) return;

        boolean dirty = false;
        Iterator<PatternJob> iterator = patternJobs.iterator();
        while (iterator.hasNext()) {
            PatternJob job = iterator.next();

            // bmaddon charges active processing jobs individually. A job that
            // has finished processing and is merely waiting for output space
            // consumes no additional AE and can still flush once space returns.
            if (job.progress < job.craftTime && canAdvancePatternCraft()) {
                job.progress++;
                dirty = true;
            }

            if (job.progress >= job.craftTime && canFitPatternOutput(job.output)) {
                insertPatternOutput(job.output.copy());
                iterator.remove();
                dirty = true;
            }
        }

        if (dirty) markDirty();
    }

    /** Hook used by the AE2 networked tile to charge exactly one active job. */
    protected boolean canAdvancePatternCraft() {
        return true;
    }

    // ---------------------------------------------------------------------
    // Nine Blood Pattern slots, matching bmaddon 1.0.4.
    // ---------------------------------------------------------------------

    @Override
    public int getSizeInventory() {
        return EXTENDED_INVENTORY_SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (isPatternSlot(slot)) return patternInventory[slot - SLOT_PATTERN_START];
        return super.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (!isPatternSlot(slot)) return super.decrStackSize(slot, amount);
        int index = slot - SLOT_PATTERN_START;
        ItemStack stack = patternInventory[index];
        if (stack == null) return null;

        ItemStack result;
        if (stack.stackSize <= amount) {
            result = stack;
            patternInventory[index] = null;
        } else {
            result = stack.splitStack(amount);
            if (stack.stackSize <= 0) patternInventory[index] = null;
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (!isPatternSlot(slot)) return super.getStackInSlotOnClosing(slot);
        int index = slot - SLOT_PATTERN_START;
        ItemStack stack = patternInventory[index];
        patternInventory[index] = null;
        if (stack != null) markDirty();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (!isPatternSlot(slot)) {
            super.setInventorySlotContents(slot, stack);
            return;
        }

        if (stack != null) {
            if (!isValidBloodPattern(stack)) return;
            stack.stackSize = 1;
        }
        patternInventory[slot - SLOT_PATTERN_START] = stack;
        markDirty();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (isPatternSlot(slot)) return isValidBloodPattern(stack);
        if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
            return super.isItemValidForSlot(slot, stack);
        }
        // The inherited 1.7 implementation has legacy manual input slots.
        // bmaddon does not: all recipe items arrive from AE2 pattern execution.
        return false;
    }

    private boolean isPatternSlot(int slot) {
        return slot >= SLOT_PATTERN_START && slot <= SLOT_PATTERN_END;
    }

    private boolean isValidBloodPattern(ItemStack stack) {
        return stack != null
                && BloodMagicContent.bloodAltarPattern != null
                && stack.getItem() == BloodMagicContent.bloodAltarPattern
                && BloodMagicPatternData.isEncoded(stack);
    }

    public ItemStack getBloodPattern(int index) {
        if (index < 0 || index >= PATTERN_SLOT_COUNT) return null;
        return patternInventory[index];
    }

    public boolean setBloodPatternIfEmpty(int index, ItemStack pattern) {
        if (index < 0 || index >= PATTERN_SLOT_COUNT || patternInventory[index] != null
                || !isValidBloodPattern(pattern)) return false;
        ItemStack copy = pattern.copy();
        copy.stackSize = 1;
        patternInventory[index] = copy;
        markDirty();
        return true;
    }

    public void setBloodPattern(int index, ItemStack pattern) {
        if (index < 0 || index >= PATTERN_SLOT_COUNT) return;
        if (pattern != null && !isValidBloodPattern(pattern)) return;
        patternInventory[index] = pattern == null ? null : pattern.copy();
        if (patternInventory[index] != null) patternInventory[index].stackSize = 1;
        markDirty();
    }

    private boolean hasInstalledPattern(BloodMagicPatternDetails details) {
        ItemStack requested = details == null ? null : details.getPattern();
        if (requested == null) return false;

        for (ItemStack installed : patternInventory) {
            if (installed == null) continue;
            if (installed.isItemEqual(requested)
                    && ItemStack.areItemStackTagsEqual(installed, requested)) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------
    // AE2 processing execution.
    // ---------------------------------------------------------------------

    @Override
    public boolean pushPattern(ICraftingPatternDetails pattern, InventoryCrafting table, ForgeDirection direction) {
        // Ordinary AE2 processing patterns must not turn the machine into a
        // generic hidden Blood Altar. Only installed Blood Patterns are valid.
        if (!(pattern instanceof BloodMagicPatternDetails)) return false;
        if (worldObj == null || worldObj.isRemote || table == null) return false;

        BloodMagicPatternDetails details = (BloodMagicPatternDetails) pattern;
        if (!hasInstalledPattern(details)) return false;
        if (details.getRequiredTier() > getAltarTier()) return false;
        if (patternJobs.size() >= getMaxParallelCrafts()) return false;

        List<ItemStack> actualInputs = expandCraftingTable(table);
        ItemStack output = firstOutput(details);
        if (actualInputs.isEmpty() || output == null) return false;
        if (!recipeStillMatches(details.getKind(), actualInputs, output)) return false;

        int lpCost = Math.max(0, details.getLifeEssenceCost());
        if (getLifeEssence() < lpCost) return false;
        if (!canFitPatternOutputIncludingQueued(output)) return false;

        if (lpCost > 0 && drain(ForgeDirection.UNKNOWN, lpCost, true) == null) return false;

        patternJobs.add(new PatternJob(output.copy(), calculatePatternCraftTime()));
        markDirty();
        return true;
    }

    @Override
    public int getMaxParallelCrafts() {
        int base = Math.max(1, BloodMagicAddonConfig.bloodAltarAssemblerBaseParallelCrafts);
        int perCard = Math.max(0, BloodMagicAddonConfig.bloodAltarAssemblerParallelCraftsPerCard);
        int cap = Math.max(1, BloodMagicAddonConfig.bloodAltarAssemblerMaxParallelCrafts);
        int cards = getParallelCardCount();
        long desired = cards > 0 ? (long) cards * perCard : base;
        return Math.max(1, (int) Math.min((long) cap, desired));
    }

    private int calculatePatternCraftTime() {
        int bloodSpeedCards = getBloodMagicSpeedCardCount();
        int accelerationCards = getAe2SpeedCardCount() + bloodSpeedCards * 4;
        int base = Math.max(1, BloodMagicAddonConfig.bloodAltarAssemblerBaseCraftTimeTicks);
        int calculated = (int) Math.ceil((base * 1.5D) / Math.max(1, 1 + accelerationCards));
        int normalMinimum = Math.max(1, BloodMagicAddonConfig.bloodAltarAssemblerMinCraftTimeTicks);
        return Math.max(bloodSpeedCards > 0 ? 1 : normalMinimum, calculated);
    }

    private ItemStack firstOutput(BloodMagicPatternDetails details) {
        if (details.getCondensedOutputs() == null || details.getCondensedOutputs().length != 1
                || details.getCondensedOutputs()[0] == null) return null;
        ItemStack stack = details.getCondensedOutputs()[0].getItemStack();
        if (stack == null) return null;
        long amount = details.getCondensedOutputs()[0].getStackSize();
        if (amount <= 0L || amount > Integer.MAX_VALUE) return null;
        stack.stackSize = (int) amount;
        return stack;
    }

    private List<ItemStack> expandCraftingTable(InventoryCrafting table) {
        List<ItemStack> result = new ArrayList<ItemStack>();
        for (int slot = 0; slot < table.getSizeInventory(); slot++) {
            ItemStack stack = table.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;
            if (stack.stackSize > 64) return new ArrayList<ItemStack>();
            for (int n = 0; n < stack.stackSize; n++) {
                ItemStack one = stack.copy();
                one.stackSize = 1;
                result.add(one);
                if (result.size() > 5) return result;
            }
        }
        return result;
    }

    private boolean recipeStillMatches(BloodMagicPatternKind kind, List<ItemStack> inputs, ItemStack output) {
        if (kind == BloodMagicPatternKind.BLOOD_ALTAR) {
            if (inputs.size() != 1) return false;
            AltarRecipe recipe = AltarRecipeRegistry.getAltarRecipeForItemAndTier(inputs.get(0), getAltarTier());
            return recipe != null && !recipe.getCanBeFilled() && recipe.getResult() != null
                    && sameStackAndCount(recipe.getResult(), output);
        }

        if (kind == BloodMagicPatternKind.ALCHEMY_TABLE) {
            if (inputs.isEmpty() || inputs.size() > 5) return false;
            ItemStack[] slots = new ItemStack[5];
            for (int i = 0; i < inputs.size(); i++) slots[i] = inputs.get(i);

            for (AlchemyRecipe recipe : AlchemyRecipeRegistry.recipes) {
                if (recipe == null || recipe.getResult() == null) continue;
                if (ingredientCount(recipe.getRecipe()) != inputs.size()) continue;
                if (recipe.getOrbLevel() > getAltarTier()) continue;
                if (recipe.doesRecipeMatch(slots, getAltarTier())
                        && sameStackAndCount(recipe.getResult(), output)) return true;
            }
        }

        return false;
    }

    private int ingredientCount(ItemStack[] recipe) {
        int count = 0;
        if (recipe != null) for (ItemStack stack : recipe) if (stack != null) count++;
        return count;
    }

    private boolean canFitPatternOutputIncludingQueued(ItemStack incoming) {
        ItemStack[] simulated = copyOutputInventory();
        for (PatternJob queued : patternJobs) {
            if (!insertIntoSimulation(simulated, queued.output.copy())) return false;
        }
        return insertIntoSimulation(simulated, incoming.copy());
    }

    private boolean canFitPatternOutput(ItemStack incoming) {
        return insertIntoSimulation(copyOutputInventory(), incoming.copy());
    }

    private ItemStack[] copyOutputInventory() {
        ItemStack[] result = new ItemStack[SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1];
        for (int i = 0; i < result.length; i++) {
            ItemStack stack = super.getStackInSlot(SLOT_OUTPUT_START + i);
            result[i] = stack == null ? null : stack.copy();
        }
        return result;
    }

    private boolean insertIntoSimulation(ItemStack[] target, ItemStack incoming) {
        int left = incoming.stackSize;
        for (int i = 0; i < target.length && left > 0; i++) {
            ItemStack existing = target[i];
            if (existing == null || !sameType(existing, incoming)) continue;
            int limit = Math.min(getInventoryStackLimit(), existing.getMaxStackSize());
            int room = limit - existing.stackSize;
            if (room <= 0) continue;
            int moved = Math.min(room, left);
            existing.stackSize += moved;
            left -= moved;
        }
        for (int i = 0; i < target.length && left > 0; i++) {
            if (target[i] != null) continue;
            ItemStack placed = incoming.copy();
            placed.stackSize = Math.min(left, Math.min(getInventoryStackLimit(), placed.getMaxStackSize()));
            target[i] = placed;
            left -= placed.stackSize;
        }
        return left == 0;
    }

    private void insertPatternOutput(ItemStack incoming) {
        int left = incoming.stackSize;
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && left > 0; slot++) {
            ItemStack existing = super.getStackInSlot(slot);
            if (existing == null || !sameType(existing, incoming)) continue;
            int limit = Math.min(getInventoryStackLimit(), existing.getMaxStackSize());
            int room = limit - existing.stackSize;
            if (room <= 0) continue;
            int moved = Math.min(room, left);
            existing.stackSize += moved;
            left -= moved;
            super.setInventorySlotContents(slot, existing);
        }
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && left > 0; slot++) {
            if (super.getStackInSlot(slot) != null) continue;
            ItemStack placed = incoming.copy();
            placed.stackSize = Math.min(left, Math.min(getInventoryStackLimit(), placed.getMaxStackSize()));
            super.setInventorySlotContents(slot, placed);
            left -= placed.stackSize;
        }
    }

    private boolean sameType(ItemStack a, ItemStack b) {
        return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    private boolean sameStackAndCount(ItemStack a, ItemStack b) {
        return a != null && b != null && a.stackSize == b.stackSize && sameType(a, b);
    }

    // The inherited machine exposes its old manual input queue through sided
    // inventory. Keep only the output buffer visible to external automation.
    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int[] slots = new int[SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1];
        for (int i = 0; i < slots.length; i++) slots[i] = SLOT_OUTPUT_START + i;
        return slots;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot >= SLOT_OUTPUT_START && slot <= SLOT_OUTPUT_END;
    }

    @Override
    public boolean isCrafting() {
        return !patternJobs.isEmpty();
    }

    @Override
    public int getProgressTicks() {
        return patternJobs.isEmpty() ? 0 : patternJobs.get(0).progress;
    }

    @Override
    public int getCraftTimeTicks() {
        return patternJobs.isEmpty() ? 0 : patternJobs.get(0).craftTime;
    }

    @Override
    public int getActiveBatch() {
        return patternJobs.size();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);

        NBTTagList patterns = new NBTTagList();
        for (int i = 0; i < PATTERN_SLOT_COUNT; i++) {
            ItemStack pattern = patternInventory[i];
            if (pattern == null) continue;
            NBTTagCompound patternTag = new NBTTagCompound();
            patternTag.setByte("Slot", (byte) i);
            pattern.writeToNBT(patternTag);
            patterns.appendTag(patternTag);
        }
        tag.setTag("DMLBloodPatterns", patterns);

        NBTTagList jobs = new NBTTagList();
        for (PatternJob job : patternJobs) {
            NBTTagCompound jobTag = new NBTTagCompound();
            NBTTagCompound outputTag = new NBTTagCompound();
            job.output.writeToNBT(outputTag);
            jobTag.setTag("Output", outputTag);
            jobTag.setInteger("Progress", job.progress);
            jobTag.setInteger("CraftTime", job.craftTime);
            jobs.appendTag(jobTag);
        }
        tag.setTag("DMLBloodPatternJobs", jobs);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        for (int i = 0; i < patternInventory.length; i++) patternInventory[i] = null;
        NBTTagList patterns = tag.getTagList("DMLBloodPatterns", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < patterns.tagCount(); i++) {
            NBTTagCompound patternTag = patterns.getCompoundTagAt(i);
            int slot = patternTag.getByte("Slot") & 255;
            if (slot < 0 || slot >= PATTERN_SLOT_COUNT) continue;
            ItemStack pattern = ItemStack.loadItemStackFromNBT(patternTag);
            if (isValidBloodPattern(pattern)) {
                pattern.stackSize = 1;
                patternInventory[slot] = pattern;
            }
        }

        patternJobs.clear();
        NBTTagList jobs = tag.getTagList("DMLBloodPatternJobs", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < jobs.tagCount(); i++) {
            NBTTagCompound jobTag = jobs.getCompoundTagAt(i);
            ItemStack output = ItemStack.loadItemStackFromNBT(jobTag.getCompoundTag("Output"));
            if (output == null) continue;
            PatternJob job = new PatternJob(output, Math.max(1, jobTag.getInteger("CraftTime")));
            job.progress = Math.max(0, Math.min(job.craftTime, jobTag.getInteger("Progress")));
            patternJobs.add(job);
        }
    }

    private static final class PatternJob {
        final ItemStack output;
        final int craftTime;
        int progress;

        PatternJob(ItemStack output, int craftTime) {
            this.output = output;
            this.craftTime = Math.max(1, craftTime);
        }
    }
}
