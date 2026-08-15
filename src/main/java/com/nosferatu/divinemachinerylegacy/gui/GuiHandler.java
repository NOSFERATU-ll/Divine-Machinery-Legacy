package com.nosferatu.divinemachinerylegacy.gui;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicContent;
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
        if (id == DivineMachineryLegacy.GUI_MANA_INFUSER && te instanceof TileMechanicalManaInfuser)
            return new ContainerMechanicalManaInfuser(player.inventory, (TileMechanicalManaInfuser) te);
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
        if (id == DivineMachineryLegacy.GUI_BLOOD_ALTAR_ASSEMBLER && te instanceof TileBloodAltarAssembler)
            return new ContainerBloodAltarAssembler(player.inventory, (TileBloodAltarAssembler) te);
        if (id == BloodMagicContent.GUI_BLOOD_GENERATOR && te instanceof TileBloodGenerator)
            return new ContainerBloodGenerator(player.inventory, (TileBloodGenerator) te);
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return DivineMachineryLegacy.proxy.getClientGuiElement(id, player, world, x, y, z);
    }
}
