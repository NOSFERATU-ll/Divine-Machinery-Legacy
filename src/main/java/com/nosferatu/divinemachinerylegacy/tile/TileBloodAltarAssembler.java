package com.nosferatu.divinemachinerylegacy.tile;

import WayofTime.alchemicalWizardry.AlchemicalWizardry;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipe;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipeRegistry;
import appeng.api.AEApi;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.nosferatu.divinemachinerylegacy.item.ItemBloodAltarTierCard;
import com.nosferatu.divinemachinerylegacy.item.ItemBloodMachineUpgrade;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * Blood Altar Assembler backport for Blood Magic 1.7.10 + AE2 rv3.
 *
 * The modern bmaddon machine is pattern driven, starts at altar tier I,
 * accepts tier II-V cards, has nine upgrade slots, AE2/special speed cards
 * and parallel cards. This implementation keeps that progression while using
 * the real 1.7.10 AltarRecipeRegistry and Life Essence fluid.
 */
public class TileBloodAltarAssembler extends TileEntity
        implements ISidedInventory, IFluidHandler, ICraftingMachine {

    public static final int SLOT_UPGRADE_START = 0;
    public static final int SLOT_UPGRADE_END = 8;
    public static final int SLOT_INPUT_START = 9;
    public static final int SLOT_INPUT_END = 16;
    public static final int SLOT_OUTPUT_START = 17;
    public static final int SLOT_OUTPUT_END = 24;
    private static final int SLOT_PENDING_OUTPUT = 25;
    public static final int INVENTORY_SIZE = 26;

    public static final int LIFE_ESSENCE_CAPACITY = 1_000_000;
    public static final int BASE_CRAFT_TIME_TICKS = 200;
    public static final int NORMAL_MIN_CRAFT_TIME_TICKS = 20;
    public static final int MAX_PARALLEL_CRAFTS = 8;
    public static final int PARALLEL_CRAFTS_PER_CARD = 2;
    public static final int MAX_PARALLEL_CARDS = 4;
    public static final int MAX_AE2_SPEED_CARDS = 4;
    public static final int MAX_BLOOD_MAGIC_SPEED_CARDS = 9;

    private final ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
    private int lifeEssence;
    private int progressTicks;
    private int craftTimeTicks;
    private int activeBatch;
    private boolean internalInventoryMutation;

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) return;

        if (inventory[SLOT_PENDING_OUTPUT] != null) {
            if (progressTicks < craftTimeTicks) {
                progressTicks++;
                markDirty();
            }
            if (progressTicks >= craftTimeTicks) {
                finishCraftIfPossible();
            }
            return;
        }

        progressTicks = 0;
        craftTimeTicks = 0;
        activeBatch = 0;
        findAndStartCraft();
    }

    private void findAndStartCraft() {
        int tier = getAltarTier();
        int parallel = getMaxParallelCrafts();

        for (int slot = SLOT_INPUT_START; slot <= SLOT_INPUT_END; slot++) {
            ItemStack input = inventory[slot];
            if (input == null || input.stackSize <= 0) continue;

            AltarRecipe recipe = AltarRecipeRegistry.getAltarRecipeForItemAndTier(input, tier);
            if (!isMachineRecipe(recipe)) continue;

            int maxBatch = Math.min(input.stackSize, parallel);
            for (int batch = maxBatch; batch >= 1; batch--) {
                long lpCostLong = (long) recipe.getLiquidRequired() * batch;
                if (lpCostLong > Integer.MAX_VALUE) continue;
                int lpCost = (int) lpCostLong;
                if (lifeEssence < lpCost) continue;

                ItemStack output = recipe.getResult().copy();
                long outputSize = (long) output.stackSize * batch;
                if (outputSize > Integer.MAX_VALUE) continue;
                output.stackSize = (int) outputSize;
                if (!canFitOutput(output)) continue;

                internalInventoryMutation = true;
                try {
                    input.stackSize -= batch;
                    if (input.stackSize <= 0) inventory[slot] = null;
                    lifeEssence -= lpCost;
                    inventory[SLOT_PENDING_OUTPUT] = output;
                    activeBatch = batch;
                    progressTicks = 0;
                    craftTimeTicks = calculateCraftTimeTicks();
                } finally {
                    internalInventoryMutation = false;
                }

                markDirty();
                return;
            }
        }
    }

    private boolean isMachineRecipe(AltarRecipe recipe) {
        return recipe != null
                && !recipe.getCanBeFilled()
                && recipe.getResult() != null;
    }

    private int calculateCraftTimeTicks() {
        int bloodSpeedCards = getBloodMagicSpeedCardCount();
        int accelerationCards = getAe2SpeedCardCount() + bloodSpeedCards * 4;

        // bmaddon formula: ceil(base * 1.5 / (1 + acceleration cards)).
        int calculated = (int) Math.ceil((BASE_CRAFT_TIME_TICKS * 1.5D)
                / Math.max(1, 1 + accelerationCards));

        // The dedicated Blood Magic speed card is intentionally allowed to
        // pass the ordinary 20 tick AE2 speed floor, matching bmaddon.
        int minimum = bloodSpeedCards > 0 ? 1 : NORMAL_MIN_CRAFT_TIME_TICKS;
        return Math.max(minimum, calculated);
    }

    private void finishCraftIfPossible() {
        ItemStack pending = inventory[SLOT_PENDING_OUTPUT];
        if (pending == null) return;
        if (!canFitOutput(pending)) return;

        internalInventoryMutation = true;
        try {
            insertOutput(pending.copy());
            inventory[SLOT_PENDING_OUTPUT] = null;
            progressTicks = 0;
            craftTimeTicks = 0;
            activeBatch = 0;
        } finally {
            internalInventoryMutation = false;
        }
        markDirty();
    }

    public int getAltarTier() {
        int tier = 1;
        for (int slot = SLOT_UPGRADE_START; slot <= SLOT_UPGRADE_END; slot++) {
            ItemStack stack = inventory[slot];
            if (stack == null || !(stack.getItem() instanceof ItemBloodAltarTierCard)) continue;
            tier = Math.max(tier, ((ItemBloodAltarTierCard) stack.getItem()).getTier());
        }
        return tier;
    }

    public int getBloodMagicSpeedCardCount() {
        return Math.min(MAX_BLOOD_MAGIC_SPEED_CARDS, countUpgrade(ItemBloodMachineUpgrade.Type.SPEED));
    }

    public int getAe2SpeedCardCount() {
        int count = 0;
        for (int slot = SLOT_UPGRADE_START; slot <= SLOT_UPGRADE_END; slot++) {
            ItemStack stack = inventory[slot];
            if (isAe2SpeedCard(stack)) count += stack.stackSize;
        }
        return Math.min(MAX_AE2_SPEED_CARDS, count);
    }

    public int getParallelCardCount() {
        return Math.min(MAX_PARALLEL_CARDS, countUpgrade(ItemBloodMachineUpgrade.Type.PARALLEL));
    }

    private int countUpgrade(ItemBloodMachineUpgrade.Type type) {
        int count = 0;
        for (int slot = SLOT_UPGRADE_START; slot <= SLOT_UPGRADE_END; slot++) {
            ItemStack stack = inventory[slot];
            if (stack == null || !(stack.getItem() instanceof ItemBloodMachineUpgrade)) continue;
            ItemBloodMachineUpgrade upgrade = (ItemBloodMachineUpgrade) stack.getItem();
            if (upgrade.getType() == type) count += stack.stackSize;
        }
        return count;
    }

    public int getMaxParallelCrafts() {
        int cards = getParallelCardCount();
        if (cards <= 0) return 1;
        return Math.max(1, Math.min(MAX_PARALLEL_CRAFTS, cards * PARALLEL_CRAFTS_PER_CARD));
    }

    public int getLifeEssence() {
        return lifeEssence;
    }

    public int getLifeEssenceCapacity() {
        return LIFE_ESSENCE_CAPACITY;
    }

    public int getProgressTicks() {
        return progressTicks;
    }

    public int getCraftTimeTicks() {
        return craftTimeTicks;
    }

    public int getActiveBatch() {
        return activeBatch;
    }

    public boolean isCrafting() {
        return inventory[SLOT_PENDING_OUTPUT] != null;
    }

    private boolean canFitOutput(ItemStack incoming) {
        ItemStack[] simulated = new ItemStack[SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1];
        for (int i = 0; i < simulated.length; i++) {
            ItemStack real = inventory[SLOT_OUTPUT_START + i];
            simulated[i] = real == null ? null : real.copy();
        }
        return insertIntoArray(simulated, incoming.copy());
    }

    private void insertOutput(ItemStack incoming) {
        int left = incoming.stackSize;
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && left > 0; slot++) {
            ItemStack existing = inventory[slot];
            if (existing == null || !canStacksMerge(existing, incoming)) continue;
            int limit = Math.min(existing.getMaxStackSize(), getInventoryStackLimit());
            int room = limit - existing.stackSize;
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
    }

    private boolean insertIntoArray(ItemStack[] target, ItemStack incoming) {
        int left = incoming.stackSize;
        for (int i = 0; i < target.length && left > 0; i++) {
            ItemStack existing = target[i];
            if (existing == null || !canStacksMerge(existing, incoming)) continue;
            int limit = Math.min(existing.getMaxStackSize(), getInventoryStackLimit());
            int room = limit - existing.stackSize;
            if (room <= 0) continue;
            int moved = Math.min(room, left);
            existing.stackSize += moved;
            left -= moved;
        }
        for (int i = 0; i < target.length && left > 0; i++) {
            if (target[i] != null) continue;
            ItemStack placed = incoming.copy();
            placed.stackSize = Math.min(left, Math.min(placed.getMaxStackSize(), getInventoryStackLimit()));
            target[i] = placed;
            left -= placed.stackSize;
        }
        return left == 0;
    }

    private boolean insertInput(ItemStack incoming) {
        int left = incoming.stackSize;
        for (int slot = SLOT_INPUT_START; slot <= SLOT_INPUT_END && left > 0; slot++) {
            ItemStack existing = inventory[slot];
            if (existing == null || !canStacksMerge(existing, incoming)) continue;
            int limit = Math.min(existing.getMaxStackSize(), getInventoryStackLimit());
            int room = limit - existing.stackSize;
            if (room <= 0) continue;
            int moved = Math.min(room, left);
            existing.stackSize += moved;
            left -= moved;
        }
        for (int slot = SLOT_INPUT_START; slot <= SLOT_INPUT_END && left > 0; slot++) {
            if (inventory[slot] != null) continue;
            ItemStack placed = incoming.copy();
            placed.stackSize = Math.min(left, Math.min(placed.getMaxStackSize(), getInventoryStackLimit()));
            inventory[slot] = placed;
            left -= placed.stackSize;
        }
        return left == 0;
    }

    private boolean canInsertInput(ItemStack incoming) {
        ItemStack[] simulated = new ItemStack[SLOT_INPUT_END - SLOT_INPUT_START + 1];
        for (int i = 0; i < simulated.length; i++) {
            ItemStack real = inventory[SLOT_INPUT_START + i];
            simulated[i] = real == null ? null : real.copy();
        }
        return insertIntoArray(simulated, incoming.copy());
    }

    private boolean canStacksMerge(ItemStack a, ItemStack b) {
        return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    private boolean isUpgradeItem(ItemStack stack) {
        if (stack == null) return false;
        Item item = stack.getItem();
        return item instanceof ItemBloodAltarTierCard
                || item instanceof ItemBloodMachineUpgrade
                || isAe2SpeedCard(stack);
    }

    private boolean isAe2SpeedCard(ItemStack stack) {
        if (stack == null) return false;
        Item speedCard = AEApi.instance().definitions().materials().cardSpeed().maybeItem().orNull();
        return speedCard != null && stack.getItem() == speedCard;
    }

    // ---------------------------------------------------------------------
    // AE2 rv3 processing-pattern input
    // ---------------------------------------------------------------------

    @Override
    public boolean acceptsPlans() {
        return true;
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails pattern, InventoryCrafting table, ForgeDirection direction) {
        if (worldObj == null || worldObj.isRemote || pattern == null || table == null) return false;
        if (pattern.isCraftable()) return false;

        ItemStack incoming = null;
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) continue;
            if (incoming != null) return false; // Blood Altar recipes have one item input in 1.7.10.
            incoming = stack.copy();
        }
        if (incoming == null) return false;

        AltarRecipe recipe = AltarRecipeRegistry.getAltarRecipeForItemAndTier(incoming, getAltarTier());
        if (!isMachineRecipe(recipe)) return false;
        if (!canInsertInput(incoming)) return false;

        internalInventoryMutation = true;
        try {
            if (!insertInput(incoming)) return false;
        } finally {
            internalInventoryMutation = false;
        }
        markDirty();
        return true;
    }

    // ---------------------------------------------------------------------
    // Life Essence fluid IO
    // ---------------------------------------------------------------------

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null || resource.getFluid() != AlchemicalWizardry.lifeEssenceFluid) return 0;
        int accepted = Math.min(resource.amount, LIFE_ESSENCE_CAPACITY - lifeEssence);
        if (accepted <= 0) return 0;
        if (doFill) {
            lifeEssence += accepted;
            markDirty();
        }
        return accepted;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || resource.getFluid() != AlchemicalWizardry.lifeEssenceFluid) return null;
        return drain(from, resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0 || lifeEssence <= 0) return null;
        int drained = Math.min(maxDrain, lifeEssence);
        FluidStack result = new FluidStack(AlchemicalWizardry.lifeEssenceFluid, drained);
        if (doDrain) {
            lifeEssence -= drained;
            markDirty();
        }
        return result;
    }

    @Override
    public boolean canFill(ForgeDirection from, net.minecraftforge.fluids.Fluid fluid) {
        return fluid == AlchemicalWizardry.lifeEssenceFluid;
    }

    @Override
    public boolean canDrain(ForgeDirection from, net.minecraftforge.fluids.Fluid fluid) {
        return fluid == null || fluid == AlchemicalWizardry.lifeEssenceFluid;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        FluidStack fluid = lifeEssence <= 0 ? null : new FluidStack(AlchemicalWizardry.lifeEssenceFluid, lifeEssence);
        return new FluidTankInfo[]{new FluidTankInfo(fluid, LIFE_ESSENCE_CAPACITY)};
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
        if (internalInventoryMutation) return;
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.divinemachinerylegacy.blood_altar_assembler";
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
    public void openInventory() { }

    @Override
    public void closeInventory() { }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) return isUpgradeItem(stack);
        if (slot >= SLOT_INPUT_START && slot <= SLOT_INPUT_END) return stack != null;
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int size = (SLOT_UPGRADE_END - SLOT_UPGRADE_START + 1)
                + (SLOT_INPUT_END - SLOT_INPUT_START + 1)
                + (SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1);
        int[] result = new int[size];
        int p = 0;
        for (int i = SLOT_UPGRADE_START; i <= SLOT_UPGRADE_END; i++) result[p++] = i;
        for (int i = SLOT_INPUT_START; i <= SLOT_INPUT_END; i++) result[p++] = i;
        for (int i = SLOT_OUTPUT_START; i <= SLOT_OUTPUT_END; i++) result[p++] = i;
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
        tag.setInteger("LifeEssence", lifeEssence);
        tag.setInteger("ProgressTicks", progressTicks);
        tag.setInteger("CraftTimeTicks", craftTimeTicks);
        tag.setInteger("ActiveBatch", activeBatch);

        NBTTagList items = new NBTTagList();
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack stack = inventory[slot];
            if (stack == null) continue;
            NBTTagCompound itemTag = new NBTTagCompound();
            itemTag.setByte("Slot", (byte) slot);
            stack.writeToNBT(itemTag);
            items.appendTag(itemTag);
        }
        tag.setTag("Items", items);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        lifeEssence = Math.max(0, Math.min(LIFE_ESSENCE_CAPACITY, tag.getInteger("LifeEssence")));
        progressTicks = Math.max(0, tag.getInteger("ProgressTicks"));
        craftTimeTicks = Math.max(0, tag.getInteger("CraftTimeTicks"));
        activeBatch = Math.max(0, tag.getInteger("ActiveBatch"));

        for (int i = 0; i < inventory.length; i++) inventory[i] = null;
        NBTTagList items = tag.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < INVENTORY_SIZE) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }

        if (inventory[SLOT_PENDING_OUTPUT] == null) {
            progressTicks = 0;
            craftTimeTicks = 0;
            activeBatch = 0;
        } else if (craftTimeTicks <= 0) {
            craftTimeTicks = calculateCraftTimeTicks();
        }
    }
}
