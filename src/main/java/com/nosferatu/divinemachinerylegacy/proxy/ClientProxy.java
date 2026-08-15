package com.nosferatu.divinemachinerylegacy.proxy;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.block.*;
import com.nosferatu.divinemachinerylegacy.client.render.*;
import com.nosferatu.divinemachinerylegacy.entity.EntityTieredManaSpark;
import com.nosferatu.divinemachinerylegacy.gui.*;
import com.nosferatu.divinemachinerylegacy.tile.*;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void registerRenderers() {
        int runic = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalRunicAltar.setRenderId(runic);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalRunicAltar(runic));

        int pool = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalManaPool.setRenderId(pool);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalManaPool(pool));

        int apothecary = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalApothecary.setRenderId(apothecary);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalApothecary(apothecary));

        int daisy = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalDaisy.setRenderId(daisy);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalDaisy(daisy));

        int agglomeration = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalIndustrialAgglomerationFactory.setRenderId(agglomeration);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalIndustrialAgglomerationFactory(agglomeration));

        RenderingRegistry.registerEntityRenderingHandler(EntityTieredManaSpark.class, new RenderTieredManaSpark());
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (id == DivineMachineryLegacy.GUI_RUNIC_ALTAR && te instanceof TileMechanicalRunicAltar)
            return new GuiMechanicalRunicAltar(player.inventory, (TileMechanicalRunicAltar) te);
        if (id == DivineMachineryLegacy.GUI_MANA_POOL && te instanceof TileMechanicalManaPool)
            return new GuiMechanicalManaPool(player.inventory, (TileMechanicalManaPool) te);
        if (id == DivineMachineryLegacy.GUI_APOTHECARY && te instanceof TileMechanicalApothecary)
            return new GuiMechanicalApothecary(player.inventory, (TileMechanicalApothecary) te);
        if (id == DivineMachineryLegacy.GUI_DAISY && te instanceof TileMechanicalDaisy)
            return new GuiMechanicalDaisy(player.inventory, (TileMechanicalDaisy) te);
        if (id == DivineMachineryLegacy.GUI_INDUSTRIAL_AGGLOMERATION
                && te instanceof TileMechanicalIndustrialAgglomerationFactory)
            return new GuiMechanicalIndustrialAgglomerationFactory(player.inventory,
                    (TileMechanicalIndustrialAgglomerationFactory) te);
        return null;
    }
}
