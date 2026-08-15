package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.tile.*;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (id == DivineMachineryLegacy.GUI_RUNIC_ALTAR && te instanceof TileMechanicalRunicAltar)
            return new ContainerMechanicalRunicAltar(player.inventory, (TileMechanicalRunicAltar) te);
        if (id == DivineMachineryLegacy.GUI_MANA_POOL && te instanceof TileMechanicalManaPool)
            return new ContainerMechanicalManaPool(player.inventory, (TileMechanicalManaPool) te);
        if (id == DivineMachineryLegacy.GUI_APOTHECARY && te instanceof TileMechanicalApothecary)
            return new ContainerMechanicalApothecary(player.inventory, (TileMechanicalApothecary) te);
        if (id == DivineMachineryLegacy.GUI_DAISY && te instanceof TileMechanicalDaisy)
            return new ContainerMechanicalDaisy(player.inventory, (TileMechanicalDaisy) te);
        if (id == DivineMachineryLegacy.GUI_INDUSTRIAL_AGGLOMERATION
                && te instanceof TileMechanicalIndustrialAgglomerationFactory)
            return new ContainerMechanicalIndustrialAgglomerationFactory(player.inventory,
                    (TileMechanicalIndustrialAgglomerationFactory) te);
        if (id == DivineMachineryLegacy.GUI_ALFHEIM_MARKET && te instanceof TileMechanicalAlfheimMarket)
            return new ContainerMechanicalAlfheimMarket(player.inventory, (TileMechanicalAlfheimMarket) te);
        if (id == DivineMachineryLegacy.GUI_ORECHID && te instanceof TileMechanicalOrechid)
            return new ContainerMechanicalOrechid(player.inventory, (TileMechanicalOrechid) te);
        if (id == DivineMachineryLegacy.GUI_JADED_AMARANTHUS && te instanceof TileJadedAmaranthus)
            return new ContainerJadedAmaranthus(player.inventory, (TileJadedAmaranthus) te);
        if (id == DivineMachineryLegacy.GUI_GREENHOUSE && te instanceof TileGreenhouse)
            return new ContainerGreenhouse(player.inventory, (TileGreenhouse) te);
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return DivineMachineryLegacy.proxy.getClientGuiElement(id, player, world, x, y, z);
    }
}
