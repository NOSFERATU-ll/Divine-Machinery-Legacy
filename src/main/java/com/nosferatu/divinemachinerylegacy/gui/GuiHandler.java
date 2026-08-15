package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalManaPool;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (id == DivineMachineryLegacy.GUI_RUNIC_ALTAR && te instanceof TileMechanicalRunicAltar) {
            return new ContainerMechanicalRunicAltar(player.inventory, (TileMechanicalRunicAltar) te);
        }
        if (id == DivineMachineryLegacy.GUI_MANA_POOL && te instanceof TileMechanicalManaPool) {
            return new ContainerMechanicalManaPool(player.inventory, (TileMechanicalManaPool) te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return DivineMachineryLegacy.proxy.getClientGuiElement(id, player, world, x, y, z);
    }
}
