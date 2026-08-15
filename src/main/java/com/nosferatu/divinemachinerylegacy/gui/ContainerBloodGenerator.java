package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.tile.TileBloodGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

public class ContainerBloodGenerator extends Container {
    private final TileBloodGenerator tile;

    private int lastEnergy = Integer.MIN_VALUE;
    private int lastMaxEnergy = Integer.MIN_VALUE;
    private int lastBlood = Integer.MIN_VALUE;
    private int lastMaxBlood = Integer.MIN_VALUE;

    private int clientEnergy;
    private int clientMaxEnergy;
    private int clientBlood;
    private int clientMaxBlood;

    public ContainerBloodGenerator(InventoryPlayer playerInventory, TileBloodGenerator tile) {
        this.tile = tile;

        // Coordinates translated directly from bmaddon's 212x186 screen.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9,
                        26 + col * 18, 104 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 26 + col * 18, 162));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.getWorldObj() != null
                && tile.getWorldObj().getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == tile
                && player.getDistanceSq(tile.xCoord + 0.5D, tile.yCoord + 0.5D, tile.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int energy = tile.getEnergyStored();
        int maxEnergy = tile.getEnergyCapacity();
        int blood = tile.getLifeEssence();
        int maxBlood = tile.getLifeEssenceCapacity();

        for (Object object : crafters) {
            ICrafting crafter = (ICrafting) object;
            if (energy != lastEnergy) sendInt(crafter, 0, 1, energy);
            if (maxEnergy != lastMaxEnergy) sendInt(crafter, 2, 3, maxEnergy);
            if (blood != lastBlood) sendInt(crafter, 4, 5, blood);
            if (maxBlood != lastMaxBlood) sendInt(crafter, 6, 7, maxBlood);
        }

        lastEnergy = energy;
        lastMaxEnergy = maxEnergy;
        lastBlood = blood;
        lastMaxBlood = maxBlood;
    }

    private void sendInt(ICrafting crafter, int lowId, int highId, int value) {
        crafter.sendProgressBarUpdate(this, lowId, value & 0xFFFF);
        crafter.sendProgressBarUpdate(this, highId, (value >>> 16) & 0xFFFF);
    }

    @Override
    public void updateProgressBar(int id, int value) {
        int unsigned = value & 0xFFFF;
        switch (id) {
            case 0: clientEnergy = (clientEnergy & 0xFFFF0000) | unsigned; break;
            case 1: clientEnergy = (clientEnergy & 0x0000FFFF) | (unsigned << 16); break;
            case 2: clientMaxEnergy = (clientMaxEnergy & 0xFFFF0000) | unsigned; break;
            case 3: clientMaxEnergy = (clientMaxEnergy & 0x0000FFFF) | (unsigned << 16); break;
            case 4: clientBlood = (clientBlood & 0xFFFF0000) | unsigned; break;
            case 5: clientBlood = (clientBlood & 0x0000FFFF) | (unsigned << 16); break;
            case 6: clientMaxBlood = (clientMaxBlood & 0xFFFF0000) | unsigned; break;
            case 7: clientMaxBlood = (clientMaxBlood & 0x0000FFFF) | (unsigned << 16); break;
            default: break;
        }
    }

    public int getEnergyStored() { return clientEnergy; }
    public int getMaxEnergyStored() { return clientMaxEnergy; }
    public int getBloodAmount() { return clientBlood; }
    public int getBloodCapacity() { return clientMaxBlood; }
}
