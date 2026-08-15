package com.nosferatu.divinemachinerylegacy.tile;

import WayofTime.alchemicalWizardry.api.alchemy.AlchemyRecipe;
import WayofTime.alchemicalWizardry.api.alchemy.AlchemyRecipeRegistry;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipe;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipeRegistry;
import appeng.api.networking.crafting.ICraftingPatternDetails;
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
 * Adds the custom Blood Pattern path from bmaddon without breaking the already
 * working plain AE2-processing fallback in TileBloodAltarAssembler.
 *
 * Blood Altar and Alchemy Table patterns are real AE2 rv3 processing patterns.
 * Their item inputs are supplied by AE2, LP is consumed from the machine tank,
 * and the finished item is returned through the normal output slots/ME Interface.
 */
public class TileBloodAltarAssemblerExtended extends TileBloodAltarAssembler {
    private final List<PatternJob> patternJobs = new ArrayList<PatternJob>();

    @Override
    public void updateEntity() {
        // Preserve the existing plain Blood Altar/manual automation path.
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote || patternJobs.isEmpty()) return;

        boolean dirty = false;
        Iterator<PatternJob> iterator = patternJobs.iterator();
        while (iterator.hasNext()) {
            PatternJob job = iterator.next();
            if (job.progress < job.craftTime) {
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

    @Override
    public boolean pushPattern(ICraftingPatternDetails pattern, InventoryCrafting table, ForgeDirection direction) {
        if (!(pattern instanceof BloodMagicPatternDetails)) {
            return super.pushPattern(pattern, table, direction);
        }
        if (worldObj == null || worldObj.isRemote || table == null) return false;

        BloodMagicPatternDetails details = (BloodMagicPatternDetails) pattern;
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
            ItemStack stack = getStackInSlot(SLOT_OUTPUT_START + i);
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
            ItemStack existing = getStackInSlot(slot);
            if (existing == null || !sameType(existing, incoming)) continue;
            int limit = Math.min(getInventoryStackLimit(), existing.getMaxStackSize());
            int room = limit - existing.stackSize;
            if (room <= 0) continue;
            int moved = Math.min(room, left);
            existing.stackSize += moved;
            left -= moved;
            setInventorySlotContents(slot, existing);
        }
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && left > 0; slot++) {
            if (getStackInSlot(slot) != null) continue;
            ItemStack placed = incoming.copy();
            placed.stackSize = Math.min(left, Math.min(getInventoryStackLimit(), placed.getMaxStackSize()));
            setInventorySlotContents(slot, placed);
            left -= placed.stackSize;
        }
    }

    private boolean sameType(ItemStack a, ItemStack b) {
        return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    private boolean sameStackAndCount(ItemStack a, ItemStack b) {
        return a != null && b != null && a.stackSize == b.stackSize && sameType(a, b);
    }

    @Override
    public boolean isCrafting() {
        return super.isCrafting() || !patternJobs.isEmpty();
    }

    @Override
    public int getProgressTicks() {
        if (super.isCrafting() || patternJobs.isEmpty()) return super.getProgressTicks();
        return patternJobs.get(0).progress;
    }

    @Override
    public int getCraftTimeTicks() {
        if (super.isCrafting() || patternJobs.isEmpty()) return super.getCraftTimeTicks();
        return patternJobs.get(0).craftTime;
    }

    @Override
    public int getActiveBatch() {
        if (super.isCrafting() || patternJobs.isEmpty()) return super.getActiveBatch();
        return patternJobs.size();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (PatternJob job : patternJobs) {
            NBTTagCompound jobTag = new NBTTagCompound();
            NBTTagCompound outputTag = new NBTTagCompound();
            job.output.writeToNBT(outputTag);
            jobTag.setTag("Output", outputTag);
            jobTag.setInteger("Progress", job.progress);
            jobTag.setInteger("CraftTime", job.craftTime);
            list.appendTag(jobTag);
        }
        tag.setTag("DMLBloodPatternJobs", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        patternJobs.clear();
        NBTTagList list = tag.getTagList("DMLBloodPatternJobs", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound jobTag = list.getCompoundTagAt(i);
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
