package com.nosferatu.divinemachinerylegacy.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternData;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternDetails;
import com.nosferatu.divinemachinerylegacy.config.BloodMagicAddonConfig;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * AE2 network host for the Blood Altar Assembler's nine internal patterns.
 * The machine itself is a crafting provider, consumes AE while working, and
 * returns completed outputs directly to its connected ME network.
 */
public class TileBloodAltarAssemblerNetworked extends TileBloodAltarAssemblerExtended
        implements IGridProxyable, ICraftingProvider, IActionHost {

    private final AENetworkProxy gridProxy;
    private final MachineSource actionSource;
    private boolean proxyReady;

    public TileBloodAltarAssemblerNetworked() {
        gridProxy = new AENetworkProxy(this, "proxy",
                DivineMachineryLegacy.bloodAltarAssembler == null
                        ? null : new ItemStack(DivineMachineryLegacy.bloodAltarAssembler), true);
        gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
        gridProxy.setIdlePowerUsage(1.0D);
        actionSource = new MachineSource(this);
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

        if (worldObj != null && !worldObj.isRemote) {
            int activeCrafts = Math.max(0, getActiveBatch());
            if (activeCrafts > 0 && !consumeConfiguredAePower(activeCrafts)) {
                // Like bmaddon, an already-started craft pauses instead of being
                // destroyed if its channel or AE supply disappears.
                return;
            }
        }

        super.updateEntity();

        if (worldObj != null && !worldObj.isRemote && gridProxy.isActive()) {
            returnCompletedOutputsToMe();
        }
    }

    private boolean consumeConfiguredAePower(int activeCrafts) {
        if (!gridProxy.isActive()) return false;

        int accelerationCards = getAe2SpeedCardCount() + getBloodMagicSpeedCardCount() * 4;
        double perCraft = BloodMagicAddonConfig.bloodAltarAssemblerAePerTickBase
                + BloodMagicAddonConfig.bloodAltarAssemblerAePerTickPerAccelerationCard
                * accelerationCards;
        double required = Math.max(0.0D, perCraft) * Math.max(1, activeCrafts);
        if (required <= 0.0D) return true;

        try {
            IEnergyGrid energy = gridProxy.getGrid().getCache(IEnergyGrid.class);
            if (energy == null) return false;

            double simulated = energy.extractAEPower(required, Actionable.SIMULATE, PowerMultiplier.ONE);
            if (simulated + 1.0E-7D < required) return false;

            double extracted = energy.extractAEPower(required, Actionable.MODULATE, PowerMultiplier.ONE);
            return extracted + 1.0E-7D >= required;
        } catch (GridAccessException ignored) {
            return false;
        }
    }

    /**
     * The modern machine returns pattern outputs to AE2 itself. 1.7.10 does not
     * provide the newer pattern-provider helper, so inject the hidden output
     * buffer through the rv3 storage grid. Anything the network cannot accept
     * stays in the machine and can still be extracted by an Import Bus/pipe.
     */
    private void returnCompletedOutputsToMe() {
        try {
            IStorageGrid storageGrid = gridProxy.getGrid().getCache(IStorageGrid.class);
            if (storageGrid == null || storageGrid.getItemInventory() == null) return;

            for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) {
                ItemStack stack = getStackInSlot(slot);
                if (stack == null || stack.stackSize <= 0) continue;

                IAEItemStack offered = AEApi.instance().storage().createItemStack(stack.copy());
                if (offered == null) continue;
                long before = offered.getStackSize();

                IAEItemStack leftover = storageGrid.getItemInventory()
                        .injectItems(offered, Actionable.MODULATE, actionSource);
                long left = leftover == null ? 0L : Math.max(0L, leftover.getStackSize());
                long accepted = Math.max(0L, before - left);
                if (accepted <= 0L) continue;

                int remove = (int) Math.min((long) stack.stackSize, accepted);
                decrStackSize(slot, remove);
            }
        } catch (GridAccessException ignored) {
            // Keep the result buffered until the network is available again.
        }
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
    public IGridNode getActionableNode() {
        return gridProxy.getNode();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection direction) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.setBlockToAir(xCoord, yCoord, zCoord);
        }
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
        return !gridProxy.isActive() || getActiveBatch() >= getMaxParallelCrafts();
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        super.setInventorySlotContents(slot, stack);
        if ((slot >= SLOT_PATTERN_START && slot <= SLOT_PATTERN_END)
                || (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END)) {
            notifyPatternChange();
        }
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        ItemStack result = super.decrStackSize(slot, amount);
        if (result != null && ((slot >= SLOT_PATTERN_START && slot <= SLOT_PATTERN_END)
                || (slot >= SLOT_UPGRADE_START && slot <= SLOT_UPGRADE_END))) {
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
