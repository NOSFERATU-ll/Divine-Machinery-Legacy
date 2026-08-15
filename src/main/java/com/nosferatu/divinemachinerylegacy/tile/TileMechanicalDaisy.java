package com.nosferatu.divinemachinerylegacy.tile;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipePureDaisy;

import java.util.Arrays;

/** Eight-slot internal Pure Daisy processor matching Extra Reforked's machine form. */
public class TileMechanicalDaisy extends TileEntity implements ISidedInventory, IFluidHandler, ICraftingMachine {
    public static final int SLOT_UPGRADE = 0;
    public static final int SLOT_WORK_START = 1;
    public static final int SLOT_WORK_END = 8;
    public static final int INVENTORY_SIZE = 9;

    private final ItemStack[] inv = new ItemStack[INVENTORY_SIZE];
    private final int[] ticks = new int[8];
    private final boolean[] done = new boolean[8];
    private int water;
    private int waterTicks;
    private boolean internal;

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) return;
        refillInfinite();
        for (int i = 0; i < 8; i++) tickSlot(i);
        tickSnow();
    }

    private void tickSlot(int i) {
        int slot = SLOT_WORK_START + i;
        ItemStack s = inv[slot];
        if (s == null) {
            ticks[i] = 0;
            done[i] = false;
            return;
        }
        if (done[i]) return;
        RecipePureDaisy r = find(s);
        if (r == null) {
            ticks[i] = 0;
            return;
        }
        ticks[i]++;
        if (ticks[i] >= 1200) {
            int count = s.stackSize;
            inv[slot] = new ItemStack(r.getOutput(), count, r.getOutputMeta());
            done[i] = true;
            ticks[i] = 0;
            markDirty();
        }
    }

    private RecipePureDaisy find(ItemStack s) {
        Block block = Block.getBlockFromItem(s.getItem());
        if (block == Blocks.air) return null;
        for (RecipePureDaisy r : BotaniaAPI.pureDaisyRecipes) {
            if (r.matches(worldObj, xCoord, yCoord, zCoord, null, block, s.getItemDamage())) return r;
        }
        return null;
    }

    private void refillInfinite() {
        if (getTier() != MachineTier.CRIMSON) return;
        ItemStack upgrade = inv[SLOT_UPGRADE];
        if (upgrade == null) return;
        Block source = null;
        if (upgrade.getItem() == DivineMachineryLegacy.catalystStoneInfinity) source = Blocks.stone;
        else if (upgrade.getItem() == DivineMachineryLegacy.catalystWoodInfinity) source = Blocks.log;
        if (source == null) return;

        for (int i = 0; i < 8; i++) {
            int slot = SLOT_WORK_START + i;
            if (inv[slot] == null) {
                inv[slot] = new ItemStack(source, getSlotLimit(), 0);
                done[i] = false;
                ticks[i] = 0;
            }
        }
    }

    private void tickSnow() {
        if (water < 1000) return;
        if (++waterTicks < 200) return;
        waterTicks = 0;

        int mult = getTier() == MachineTier.MALACHITE ? 2
                : getTier() == MachineTier.SAFFRON ? 4
                : getTier() == MachineTier.SHADOW ? 8 : 16;

        for (int i = 0; i < 8 && water >= 1000; i++) {
            int slot = SLOT_WORK_START + i;
            ItemStack s = inv[slot];
            if (s == null) {
                int amount = Math.min(mult, getSlotLimit());
                amount = Math.min(amount, water / 1000);
                if (amount > 0) {
                    inv[slot] = new ItemStack(Blocks.snow, amount);
                    done[i] = true;
                    water -= 1000 * amount;
                }
            } else if (s.getItem() == Item.getItemFromBlock(Blocks.snow)) {
                int amount = Math.min(mult, getSlotLimit() - s.stackSize);
                amount = Math.min(amount, water / 1000);
                if (amount > 0) {
                    s.stackSize += amount;
                    water -= 1000 * amount;
                    done[i] = true;
                }
            }
        }
        markDirty();
    }

    public MachineTier getTier() {
        return worldObj == null ? MachineTier.MALACHITE : MachineTier.fromMeta(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
    }

    public int getSlotLimit() {
        return getTier() == MachineTier.MALACHITE ? 4
                : getTier() == MachineTier.SAFFRON ? 8
                : getTier() == MachineTier.SHADOW ? 16 : 32;
    }

    public int getWaterCapacity() {
        return getTier() == MachineTier.MALACHITE ? 16000
                : getTier() == MachineTier.SAFFRON ? 32000
                : getTier() == MachineTier.SHADOW ? 64000 : 128000;
    }

    public int getWater() {
        return water;
    }

    public int getProgress(int i) {
        return i >= 0 && i < 8 ? ticks[i] : 0;
    }

    public boolean hasUpgradeSlot() {
        return getTier() == MachineTier.CRIMSON;
    }

    private boolean isUpgrade(ItemStack s) {
        return s != null && (s.getItem() == DivineMachineryLegacy.catalystStoneInfinity
                || s.getItem() == DivineMachineryLegacy.catalystWoodInfinity);
    }

    private boolean validBlock(ItemStack s) {
        return find(s) != null || s != null && s.getItem() == Item.getItemFromBlock(Blocks.snow);
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
            if (!validBlock(s) || !insert(cp, s.copy())) return false;
        }
        internal = true;
        try {
            for (int i = 0; i < table.getSizeInventory(); i++) {
                ItemStack s = table.getStackInSlot(i);
                if (s != null) insert(inv, s.copy());
            }
        } finally {
            internal = false;
        }
        markDirty();
        return true;
    }

    private ItemStack[] copy() {
        ItemStack[] c = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < c.length; i++) c[i] = inv[i] == null ? null : inv[i].copy();
        return c;
    }

    private boolean insert(ItemStack[] target, ItemStack x) {
        int left = x.stackSize;
        for (int i = SLOT_WORK_START; i <= SLOT_WORK_END && left > 0; i++) {
            ItemStack existing = target[i];
            if (existing != null && !done[i - 1] && existing.isItemEqual(x)) {
                int room = getSlotLimit() - existing.stackSize;
                int moved = Math.min(room, left);
                existing.stackSize += moved;
                left -= moved;
            }
        }
        for (int i = SLOT_WORK_START; i <= SLOT_WORK_END && left > 0; i++) {
            if (target[i] == null) {
                ItemStack placed = x.copy();
                placed.stackSize = Math.min(left, getSlotLimit());
                target[i] = placed;
                left -= placed.stackSize;
            }
        }
        return left == 0;
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
        return resource != null && resource.getFluid() == FluidRegistry.WATER
                ? drain(from, resource.amount, doDrain) : null;
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

        boolean completedRemainder = i >= SLOT_WORK_START && i <= SLOT_WORK_END && done[i - SLOT_WORK_START];
        ItemStack removed;
        if (s.stackSize <= amount) {
            removed = s;
            inv[i] = null;
            changed(i);
        } else {
            removed = s.splitStack(amount);
            if (completedRemainder) {
                // AE2/hoppers may extract a finished stack in pieces. Keep the remainder exportable.
                markDirty();
            } else {
                changed(i);
            }
        }
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
        if (s != null && i > 0 && s.stackSize > getSlotLimit()) s.stackSize = getSlotLimit();
        changed(i);
    }

    @Override
    public String getInventoryName() {
        return "container.divinemachinerylegacy.daisy";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 32;
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
        if (i == SLOT_UPGRADE) return hasUpgradeSlot() && isUpgrade(s);
        return i >= SLOT_WORK_START && i <= SLOT_WORK_END && !done[i - 1] && validBlock(s);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int[] slots = new int[hasUpgradeSlot() ? 9 : 8];
        int p = 0;
        if (hasUpgradeSlot()) slots[p++] = SLOT_UPGRADE;
        for (int i = SLOT_WORK_START; i <= SLOT_WORK_END; i++) slots[p++] = i;
        return slots;
    }

    @Override
    public boolean canInsertItem(int i, ItemStack s, int side) {
        return getStackInSlot(i) == null && isItemValidForSlot(i, s);
    }

    @Override
    public boolean canExtractItem(int i, ItemStack s, int side) {
        return i >= SLOT_WORK_START && i <= SLOT_WORK_END && done[i - SLOT_WORK_START];
    }

    private void changed(int i) {
        if (!internal && i >= SLOT_WORK_START && i <= SLOT_WORK_END) {
            ticks[i - SLOT_WORK_START] = 0;
            done[i - SLOT_WORK_START] = false;
        }
        markDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound n) {
        super.writeToNBT(n);
        n.setInteger("water", water);
        n.setIntArray("ticks", ticks);
        int mask = 0;
        for (int i = 0; i < 8; i++) if (done[i]) mask |= 1 << i;
        n.setInteger("done", mask);

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
        int[] savedTicks = n.getIntArray("ticks");
        System.arraycopy(savedTicks, 0, ticks, 0, Math.min(8, savedTicks.length));
        int mask = n.getInteger("done");
        for (int i = 0; i < 8; i++) done[i] = (mask & (1 << i)) != 0;

        Arrays.fill(inv, null);
        NBTTagList list = n.getTagList("inv", Constants.NBT.TAG_COMPOUND);
        for (int j = 0; j < list.tagCount(); j++) {
            NBTTagCompound c = list.getCompoundTagAt(j);
            int i = c.getByte("slot") & 255;
            if (i < inv.length) inv[i] = ItemStack.loadItemStackFromNBT(c);
        }
    }

    public void setClientWater(int value) {
        if (worldObj != null && worldObj.isRemote) water = value;
    }
}
