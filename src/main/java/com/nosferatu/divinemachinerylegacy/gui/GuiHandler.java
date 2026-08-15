package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id != DivineMachineryLegacy.GUI_RUNIC_ALTAR) return null;
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileMechanicalRunicAltar)) return null;
        return new ContainerMechanicalRunicAltar(player.inventory, (TileMechanicalRunicAltar) te);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return DivineMachineryLegacy.proxy.getClientGuiElement(id, player, world, x, y, z);
    }
}
