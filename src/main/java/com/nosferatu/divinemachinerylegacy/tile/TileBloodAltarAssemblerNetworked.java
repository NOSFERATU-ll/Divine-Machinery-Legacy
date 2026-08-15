package com.nosferatu.divinemachinerylegacy.tile;

import appeng.api.config.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternData;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternDetails;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * AE2 network host for the Blood Altar Assembler's nine internal patterns.
 * This turns the machine itself into a crafting provider, matching bmaddon,
 * instead of requiring an external ME Interface to hold the patterns.
 */
public class TileBloodAltarAssemblerNetworked extends TileBloodAltarAssemblerExtended
        implements IGridProxyable, ICraftingProvider {

    private final AENetworkProxy gridProxy;
    private boolean proxyReady;

    public TileBloodAltarAssemblerNetworked() {
        gridProxy = new AENetworkProxy(this, "proxy",
                DivineMachineryLegacy.bloodAltarAssembler == null
                        ? null : new ItemStack(DivineMachineryLegacy.bloodAltarAssembler), true);
        gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
        gridProxy.setIdlePowerUsage(1.0D);
    }

    @Override
    public void updateEntity() {
        if (!proxyReady && worldObj != null && !worldObj.isRemote) {
            gridProxy.setVisualRepresentation(DivineMachineryLegacy.bloodAltarAssembler == null
                    ? null : new ItemStack(DivineMachineryLegacy.bloodAltarAssembler));
            gridProxy.onReady();
            proxyReady = true;
            notifyPatternChange();
        }
        super.updateEntity();
    }

    @Override
    public void invalidate() {
        gridProxy.invalidate();
        proxyReady = false;
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        gridProxy.onChunkUnload();
        proxyReady = false;
        super.onChunkUnload();
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        gridProxy.writeToNBT(tag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        gridProxy.readFromNBT(tag);
    }

    @Override
    public IGridNode getGridNode(ForgeDirection direction) {
        return gridProxy.getNode();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection direction) {
        return AECableType.SMART;
    }

    @Override
    public AENetworkProxy getProxy() {
        return gridProxy;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public void gridChanged() {
        notifyPatternChange();
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper helper) {
        if (helper == null || worldObj == null || !gridProxy.isActive()) return;

        for (int i = 0; i < PATTERN_SLOT_COUNT; i++) {
            ItemStack patternStack = getBloodPattern(i);
            if (!BloodMagicPatternData.isEncoded(patternStack)) continue;

            try {
                BloodMagicPatternDetails details = new BloodMagicPatternDetails(patternStack);
                if (details.getRequiredTier() <= getAltarTier()) {
                    helper.addCraftingOption(this, details);
                }
            } catch (Throwable ignored) {
                // Invalid/stale patterns simply do not become crafting options.
            }
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        return super.pushPattern(patternDetails, table, ForgeDirection.UNKNOWN);
    }

    @Override
    public boolean isBusy() {
        // pushPattern performs the exact parallel-capacity check. Returning false
        // lets AE2 fill all available parallel lanes instead of serializing jobs.
        return !gridProxy.isActive();
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        super.setInventorySlotContents(slot, stack);
        if (slot >= SLOT_PATTERN_START && slot <= SLOT_PATTERN_END
                || slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END) {
            notifyPatternChange();
        }
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack result = super.decrStackSize(slot, amount);
        if (result != null && (slot >= SLOT_PATTERN_START && slot <= SLOT_PATTERN_END
                || slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END)) {
            notifyPatternChange();
        }
        return result;
    }

    @Override
    public boolean setBloodPatternIfEmpty(int index, ItemStack pattern) {
        boolean changed = super.setBloodPatternIfEmpty(index, pattern);
        if (changed) notifyPatternChange();
        return changed;
    }

    @Override
    public void setBloodPattern(int index, ItemStack pattern) {
        super.setBloodPattern(index, pattern);
        notifyPatternChange();
    }

    public void notifyPatternChange() {
        if (!proxyReady || worldObj == null || worldObj.isRemote) return;
        IGridNode node = gridProxy.getNode();
        if (node == null) return;
        try {
            gridProxy.getGrid().postEvent(new MENetworkCraftingPatternChange(this, node));
        } catch (GridAccessException ignored) {
            // The cable/grid may still be joining this tick.
        }
    }
}
