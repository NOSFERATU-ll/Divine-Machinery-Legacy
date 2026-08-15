package com.nosferatu.divinemachinerylegacy.proxy;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.client.render.RenderMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.gui.GuiMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
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
        int renderId = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalRunicAltar.setRenderId(renderId);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalRunicAltar(renderId));
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id != DivineMachineryLegacy.GUI_RUNIC_ALTAR) return null;
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileMechanicalRunicAltar)) return null;
        return new GuiMechanicalRunicAltar(player.inventory, (TileMechanicalRunicAltar) te);
    }
}
