package com.nosferatu.divinemachinerylegacy.proxy;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.client.render.RenderMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.client.render.RenderTieredManaSpark;
import com.nosferatu.divinemachinerylegacy.entity.EntityTieredManaSpark;
import com.nosferatu.divinemachinerylegacy.gui.GuiMechanicalManaPool;
import com.nosferatu.divinemachinerylegacy.gui.GuiMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalManaPool;
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
        RenderingRegistry.registerEntityRenderingHandler(EntityTieredManaSpark.class, new RenderTieredManaSpark());
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (id == DivineMachineryLegacy.GUI_RUNIC_ALTAR && te instanceof TileMechanicalRunicAltar) {
            return new GuiMechanicalRunicAltar(player.inventory, (TileMechanicalRunicAltar) te);
        }
        if (id == DivineMachineryLegacy.GUI_MANA_POOL && te instanceof TileMechanicalManaPool) {
            return new GuiMechanicalManaPool(player.inventory, (TileMechanicalManaPool) te);
        }
        return null;
    }
}
