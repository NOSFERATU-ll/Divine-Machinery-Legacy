package com.nosferatu.divinemachinerylegacy.tile;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipePetals;

import java.util.Arrays;
import java.util.Locale;

/** Inventory/fluid automation port of Extra Reforked's Mechanical Apothecary. */
public class TileMechanicalApothecary extends TileEntity implements ISidedInventory, IFluidHandler, ICraftingMachine {
    public static final int SLOT_SEEDS = 0;
    public static final int SLOT_UPGRADE_START = 1;
    public static final int SLOT_UPGRADE_END = 2;
    public static final int SLOT_INPUT_START = 3;
    public static final int SLOT_INPUT_END = 18;
    public static final int SLOT_OUTPUT_START = 19;
    public static final int SLOT_OUTPUT_END = 34;
    public static final int INVENTORY_SIZE = 35;

    private final ItemStack[] inv = new ItemStack[INVENTORY_SIZE];
    private int water;
    private int progress;
    private int batch;
    private RecipePetals recipe;
    private boolean internal;

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) return;
        if (recipe == null) findRecipe();
        if (recipe == null) return;
        if (!requirementsStillPresent()) {
            reset();
            return;
        }
        if (++progress >= 20) craft();
        markDirty();
    }

    private void findRecipe() {
        for (RecipePetals r : BotaniaAPI.petalRecipes) {
            for (int b = getTier().getParallelCrafts(); b >= 1; b--) {
                Plan p = plan(r, b);
                if (p == null) continue;
                if (!hasInfiniteSeeds() && seedCount() < b) continue;
                if (!hasInfiniteWater() && water < 1000 * b) continue;
                ItemStack out = r.getOutput();
                if (out == null || !canFit(out, b)) continue;
                recipe = r;
                batch = b;
                progress = 0;
                return;
            }
        }
    }

    private boolean requirementsStillPresent() {
        return recipe != null
                && batch > 0
                && plan(recipe, batch) != null
                && (hasInfiniteSeeds() || seedCount() >= batch)
                && (hasInfiniteWater() || water >= 1000 * batch)
                && canFit(recipe.getOutput(), batch);
    }

    private Plan plan(RecipePetals r, int b) {
        int n = getInputCount();
        int first = SLOT_INPUT_START;
        int[] avail = new int[n];
        int[] use = new int[n];
        for (int i = 0; i < n; i++) {
            ItemStack s = inv[first + i];
            avail[i] = s == null ? 0 : s.stackSize;
        }
        for (int c = 0; c < b; c++) {
            for (Object req : r.getInputs()) {
                int found = -1;
                for (int i = 0; i < n; i++) {
                    if (avail[i] > 0 && matches(req, inv[first + i])) {
                        found = i;
                        break;
                    }
                }
                if (found < 0) return null;
                avail[found]--;
                use[found]++;
            }
        }
        return new Plan(use);
    }

    private boolean matches(Object req, ItemStack s) {
        if (s == null) return false;
        if (req instanceof ItemStack) {
            ItemStack wanted = (ItemStack) req;
            return wanted.getItem() == s.getItem()
                    && (wanted.getItemDamage() == s.getItemDamage()
                    || wanted.getItemDamage() == OreDictionary.WILDCARD_VALUE);
        }
        if (req instanceof String) {
            for (ItemStack ore : OreDictionary.getOres((String) req)) {
                if (OreDictionary.itemMatches(ore, s, false)) return true;
            }
        }
        return false;
    }

    private void craft() {
        Plan p = plan(recipe, batch);
        if (p == null) {
            reset();
            return;
        }
        internal = true;
        try {
            for (int i = 0; i < p.use.length; i++) {
                int amount = p.use[i];
                if (amount <= 0) continue;
                ItemStack s = inv[SLOT_INPUT_START + i];
                s.stackSize -= amount;
                if (s.stackSize <= 0) inv[SLOT_INPUT_START + i] = null;
            }
            if (!hasInfiniteSeeds()) consumeSeeds(batch);
            if (!hasInfiniteWater()) water = Math.max(0, water - 1000 * batch);
            ItemStack out = recipe.getOutput().copy();
            out.stackSize *= batch;
            insertOutput(out);
        } finally {
            internal = false;
        }
        worldObj.playSoundEffect(xCoord + .5, yCoord + .5, zCoord + .5, "botania:altarCraft", 1F, 1F);
        reset();
        markDirty();
    }

    private void consumeSeeds(int amount) {
        ItemStack s = inv[SLOT_SEEDS];
        if (s != null) {
            s.stackSize -= amount;
            if (s.stackSize <= 0) inv[SLOT_SEEDS] = null;
        }
    }

    private int seedCount() {
        ItemStack s = inv[SLOT_SEEDS];
        return s == null ? 0 : s.stackSize;
    }

    private boolean canFit(ItemStack out, int b) {
        ItemStack[] cp = copy();
        ItemStack x = out.copy();
        x.stackSize *= b;
        return insert(cp, x, SLOT_OUTPUT_START, getLastOutput());
    }

    private void insertOutput(ItemStack x) {
        insert(inv, x, SLOT_OUTPUT_START, getLastOutput());
    }

    private boolean insert(ItemStack[] target, ItemStack x, int first, int last) {
        int left = x.stackSize;
        for (int i = first; i <= last && left > 0; i++) {
            ItemStack existing = target[i];
            if (existing != null && existing.isItemEqual(x) && ItemStack.areItemStackTagsEqual(existing, x)) {
                int room = Math.min(existing.getMaxStackSize(), 64) - existing.stackSize;
                int moved = Math.min(room, left);
                existing.stackSize += moved;
                left -= moved;
            }
        }
        for (int i = first; i <= last && left > 0; i++) {
            if (target[i] == null) {
                ItemStack placed = x.copy();
                placed.stackSize = Math.min(left, placed.getMaxStackSize());
                target[i] = placed;
                left -= placed.stackSize;
            }
        }
        return left == 0;
    }

    private ItemStack[] copy() {
        ItemStack[] c = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < c.length; i++) c[i] = inv[i] == null ? null : inv[i].copy();
        return c;
    }

    private void reset() {
        recipe = null;
        batch = 0;
        progress = 0;
    }

    public MachineTier getTier() {
        return worldObj == null ? MachineTier.MALACHITE : MachineTier.fromMeta(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
    }

    public int getInputCount() {
        switch (getTier()) {
            case SAFFRON:
                return 12;
            case SHADOW:
            case CRIMSON:
                return 16;
            default:
                return 9;
        }
    }

    public int getOutputCount() {
        return getInputCount();
    }

    private int getLastOutput() {
        return SLOT_OUTPUT_START + getOutputCount() - 1;
    }

    public int getWaterCapacity() {
        switch (getTier()) {
            case SAFFRON:
                return 32000;
            case SHADOW:
                return 64000;
            case CRIMSON:
                return 128000;
            default:
                return 16000;
        }
    }

    public int getWater() {
        return water;
    }

    public int getProgress() {
        return progress;
    }

    public int getBatch() {
        return batch;
    }

    public int getUpgradeSlots() {
        return getTier().getUpgradeSlots();
    }

    private boolean hasUpgrade(Item item) {
        for (int i = 0; i < getUpgradeSlots(); i++) {
            ItemStack s = inv[SLOT_UPGRADE_START + i];
            if (s != null && s.getItem() == item) return true;
        }
        return false;
    }

    public boolean hasInfiniteSeeds() {
        return hasUpgrade(DivineMachineryLegacy.catalystSeedInfinity);
    }

    public boolean hasInfiniteWater() {
        return hasUpgrade(DivineMachineryLegacy.catalystWaterInfinity);
    }

    private boolean isUpgrade(ItemStack s) {
        return s != null && (s.getItem() == DivineMachineryLegacy.catalystSeedInfinity
                || s.getItem() == DivineMachineryLegacy.catalystWaterInfinity);
    }

    private boolean isSeed(ItemStack s) {
        if (s == null) return false;
        for (int id : OreDictionary.getOreIDs(s)) {
            if (OreDictionary.getOreName(id).toLowerCase(Locale.ENGLISH).startsWith("seed")) return true;
        }
        String n = s.getUnlocalizedName();
        return n != null && n.toLowerCase(Locale.ENGLISH).contains("seed");
    }

    private boolean validInput(ItemStack s) {
        for (RecipePetals r : BotaniaAPI.petalRecipes) {
            for (Object o : r.getInputs()) {
                if (matches(o, s)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptsPlans() {
        return true;
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails p, InventoryCrafting table, ForgeDirection direction) {
        if (worldObj == null || worldObj.isRemote || p == null || p.isCraftable()) return false;
        ItemStack[] cp = copy();
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack s = table.getStackInSlot(i);
            if (s == null) continue;
            if (!validInput(s) || !insert(cp, s.copy(), SLOT_INPUT_START, SLOT_INPUT_START + getInputCount() - 1)) return false;
        }
        internal = true;
        try {
            for (int i = 0; i < table.getSizeInventory(); i++) {
                ItemStack s = table.getStackInSlot(i);
                if (s != null) insert(inv, s.copy(), SLOT_INPUT_START, SLOT_INPUT_START + getInputCount() - 1);
            }
        } finally {
            internal = false;
        }
        reset();
        markDirty();
        return true;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null || resource.getFluid() != FluidRegistry.WATER) return 0;
        int amount = Math.min(resource.amount, getWaterCapacity() - water);
        if (doFill && amount > 0) {
            water += amount;
            markDirty();
        }
        return amount;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || resource.getFluid() != FluidRegistry.WATER) return null;
        return drain(from, resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        int amount = Math.min(maxDrain, water);
        if (amount <= 0) return null;
        if (doDrain) {
            water -= amount;
            markDirty();
        }
        return new FluidStack(FluidRegistry.WATER, amount);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return fluid == FluidRegistry.WATER;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return fluid == FluidRegistry.WATER;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{new FluidTankInfo(new FluidStack(FluidRegistry.WATER, water), getWaterCapacity())};
    }

    @Override
    public int getSizeInventory() {
        return INVENTORY_SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return i >= 0 && i < INVENTORY_SIZE ? inv[i] : null;
    }

    @Override
    public ItemStack decrStackSize(int i, int amount) {
        ItemStack s = getStackInSlot(i);
        if (s == null) return null;

        ItemStack removed;
        if (s.stackSize <= amount) {
            removed = s;
            inv[i] = null;
        } else {
            removed = s.splitStack(amount);
            if (s.stackSize <= 0) inv[i] = null;
        }
        changed(i);
        return removed;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        ItemStack s = getStackInSlot(i);
        inv[i] = null;
        changed(i);
        return s;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack s) {
        inv[i] = s;
        if (s != null && s.stackSize > 64) s.stackSize = 64;
        changed(i);
    }

    @Override
    public String getInventoryName() {
        return "container.divinemachinerylegacy.apothecary";
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
    public boolean isUseableByPlayer(EntityPlayer p) {
        return worldObj != null
                && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
                && p.getDistanceSq(xCoord + .5, yCoord + .5, zCoord + .5) <= 64;
    }

    @Override public void openInventory() {}
    @Override public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int i, ItemStack s) {
        if (i == SLOT_SEEDS) return isSeed(s);
        if (i >= SLOT_UPGRADE_START && i <= SLOT_UPGRADE_END) {
            return i - SLOT_UPGRADE_START < getUpgradeSlots() && isUpgrade(s);
        }
        if (i >= SLOT_INPUT_START && i < SLOT_INPUT_START + getInputCount()) return validInput(s);
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int[] a = new int[1 + getUpgradeSlots() + getInputCount() + getOutputCount()];
        int p = 0;
        a[p++] = SLOT_SEEDS;
        for (int i = 0; i < getUpgradeSlots(); i++) a[p++] = SLOT_UPGRADE_START + i;
        for (int i = 0; i < getInputCount(); i++) a[p++] = SLOT_INPUT_START + i;
        for (int i = 0; i < getOutputCount(); i++) a[p++] = SLOT_OUTPUT_START + i;
        return a;
    }

    @Override
    public boolean canInsertItem(int i, ItemStack s, int side) {
        return isItemValidForSlot(i, s);
    }

    @Override
    public boolean canExtractItem(int i, ItemStack s, int side) {
        return i >= SLOT_OUTPUT_START && i <= getLastOutput();
    }

    private void changed(int i) {
        if (!internal && i < SLOT_OUTPUT_START) reset();
        markDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound n) {
        super.writeToNBT(n);
        n.setInteger("water", water);
        n.setInteger("progress", progress);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inv.length; i++) {
            if (inv[i] != null) {
                NBTTagCompound c = new NBTTagCompound();
                c.setByte("slot", (byte) i);
                inv[i].writeToNBT(c);
                list.appendTag(c);
            }
        }
        n.setTag("inv", list);
    }

    @Override
    public void readFromNBT(NBTTagCompound n) {
        super.readFromNBT(n);
        water = n.getInteger("water");
        progress = n.getInteger("progress");
        Arrays.fill(inv, null);
        NBTTagList list = n.getTagList("inv", Constants.NBT.TAG_COMPOUND);
        for (int j = 0; j < list.tagCount(); j++) {
            NBTTagCompound c = list.getCompoundTagAt(j);
            int i = c.getByte("slot") & 255;
            if (i < inv.length) inv[i] = ItemStack.loadItemStackFromNBT(c);
        }
        reset();
    }

    public void setClientWater(int value) {
        if (worldObj != null && worldObj.isRemote) water = value;
    }

    public void setClientProgress(int value) {
        if (worldObj != null && worldObj.isRemote) progress = value;
    }

    public void setClientBatch(int value) {
        if (worldObj != null && worldObj.isRemote) batch = value;
    }

    private static final class Plan {
        final int[] use;
        Plan(int[] use) {
            this.use = use;
        }
    }
}
