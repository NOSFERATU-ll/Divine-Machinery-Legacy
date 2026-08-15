package com.nosferatu.divinemachinerylegacy.tile;

import WayofTime.alchemicalWizardry.AlchemicalWizardry;
import cofh.api.energy.IEnergyReceiver;
import com.nosferatu.divinemachinerylegacy.config.BloodMagicAddonConfig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * Blood Generator from BloodMagic Additions, translated to 1.7.10 RF + Forge Fluids.
 *
 * It accepts RF, converts it into Life Essence and exposes the tank as output-only.
 * Defaults and partial-generation behaviour match bmaddon 1.0.4.
 */
public class TileBloodGenerator extends TileEntity implements IEnergyReceiver, IFluidHandler {
    private int energyStored;
    private int lifeEssence;

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) return;

        int interval = Math.max(1, BloodMagicAddonConfig.bloodGeneratorWorkIntervalTicks);
        if (worldObj.getTotalWorldTime() % interval == 0L) {
            tryGenerateLifeEssence();
        }

        if (BloodMagicAddonConfig.bloodGeneratorAutoOutput) {
            tryAutoOutputFluid();
        }
    }

    private void tryGenerateLifeEssence() {
        int configuredEnergy = BloodMagicAddonConfig.bloodGeneratorEnergyPerOperation;
        int configuredLife = BloodMagicAddonConfig.bloodGeneratorLifeEssencePerOperation;
        if (configuredEnergy <= 0 || configuredLife <= 0 || energyStored <= 0) return;

        int capacity = getLifeEssenceCapacity();
        int room = capacity - lifeEssence;
        if (room <= 0) return;

        int generated = Math.min(configuredLife, room);
        int energyCost = calculateEnergyCostForGeneratedLife(configuredEnergy, configuredLife, generated);
        if (energyStored < energyCost) return;

        energyStored -= energyCost;
        lifeEssence += generated;
        markDirty();
        syncBlock();
    }

    /** bmaddon charges proportionally when only part of an operation fits. */
    private int calculateEnergyCostForGeneratedLife(int fullEnergyCost, int fullLifeAmount, int generatedLifeAmount) {
        if (generatedLifeAmount >= fullLifeAmount) return fullEnergyCost;
        double ratio = (double) generatedLifeAmount / (double) fullLifeAmount;
        return Math.max(1, (int) Math.ceil(fullEnergyCost * ratio));
    }

    private void tryAutoOutputFluid() {
        if (lifeEssence <= 0) return;

        int perSideLimit = Math.max(1, BloodMagicAddonConfig.bloodGeneratorMaxFluidOutputPerTick);
        for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
            if (lifeEssence <= 0) return;

            TileEntity neighbor = worldObj.getTileEntity(
                    xCoord + side.offsetX,
                    yCoord + side.offsetY,
                    zCoord + side.offsetZ);
            if (!(neighbor instanceof IFluidHandler)) continue;

            IFluidHandler handler = (IFluidHandler) neighbor;
            int offered = Math.min(perSideLimit, lifeEssence);
            FluidStack stack = new FluidStack(AlchemicalWizardry.lifeEssenceFluid, offered);
            int accepted = handler.fill(side.getOpposite(), stack, false);
            if (accepted <= 0) continue;

            accepted = Math.min(accepted, offered);
            FluidStack actual = new FluidStack(AlchemicalWizardry.lifeEssenceFluid, accepted);
            int filled = handler.fill(side.getOpposite(), actual, true);
            if (filled <= 0) continue;

            lifeEssence -= Math.min(filled, lifeEssence);
            markDirty();
            syncBlock();
        }
    }

    private void syncBlock() {
        if (worldObj != null) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public int getEnergyStored() {
        return energyStored;
    }

    public int getEnergyCapacity() {
        return Math.max(1, BloodMagicAddonConfig.bloodGeneratorEnergyCapacity);
    }

    public int getLifeEssence() {
        return lifeEssence;
    }

    public int getLifeEssenceCapacity() {
        return Math.max(1, BloodMagicAddonConfig.bloodGeneratorLifeTankCapacity);
    }

    // ---------------------------------------------------------------------
    // Redstone Flux input (CoFH 1.7.10)
    // ---------------------------------------------------------------------

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return true;
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        if (maxReceive <= 0) return 0;
        int cappedInput = Math.min(maxReceive, Math.max(1, BloodMagicAddonConfig.bloodGeneratorMaxEnergyInput));
        int accepted = Math.min(cappedInput, getEnergyCapacity() - energyStored);
        if (accepted > 0 && !simulate) {
            energyStored += accepted;
            markDirty();
            syncBlock();
        }
        return Math.max(0, accepted);
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return getEnergyCapacity();
    }

    // ---------------------------------------------------------------------
    // Output-only Life Essence fluid handler, matching bmaddon.
    // ---------------------------------------------------------------------

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || resource.amount <= 0 || resource.getFluid() != AlchemicalWizardry.lifeEssenceFluid) {
            return null;
        }
        return drain(from, resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0 || lifeEssence <= 0) return null;
        int amount = Math.min(maxDrain, lifeEssence);
        FluidStack result = new FluidStack(AlchemicalWizardry.lifeEssenceFluid, amount);
        if (doDrain) {
            lifeEssence -= amount;
            markDirty();
            syncBlock();
        }
        return result;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return fluid == null || fluid == AlchemicalWizardry.lifeEssenceFluid;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        FluidStack stack = lifeEssence <= 0
                ? null
                : new FluidStack(AlchemicalWizardry.lifeEssenceFluid, lifeEssence);
        return new FluidTankInfo[]{new FluidTankInfo(stack, getLifeEssenceCapacity())};
    }

    // ---------------------------------------------------------------------
    // Persistence / client sync
    // ---------------------------------------------------------------------

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Energy", energyStored);
        tag.setInteger("LifeEssence", lifeEssence);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        energyStored = Math.max(0, Math.min(getEnergyCapacity(), tag.getInteger("Energy")));
        lifeEssence = Math.max(0, Math.min(getLifeEssenceCapacity(), tag.getInteger("LifeEssence")));
    }

    @Override
    public net.minecraft.network.Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
    }

    public void onDataPacket(net.minecraft.network.NetworkManager net,
                             net.minecraft.network.play.server.S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
    }
}
