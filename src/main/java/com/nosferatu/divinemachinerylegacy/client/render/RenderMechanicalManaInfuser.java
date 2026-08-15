package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalManaInfuser;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Reforked Mana Infuser frame/plate geometry with a native Botania mana-glass core. */
public class RenderMechanicalManaInfuser implements ISimpleBlockRenderingHandler {
    private static final RenderMachineModelUtil.UV FULL = RenderMachineModelUtil.FULL;
    private final int renderId;

    public RenderMechanicalManaInfuser(int renderId) { this.renderId = renderId; }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockMechanicalManaInfuser)) return;
        Tessellator tess = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-.5F, -.5F, -.5F);
        tess.startDrawingQuads();
        renderModel(tess, (BlockMechanicalManaInfuser) block, metadata, 0, 0, 0, 15728880);
        tess.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
                                    int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockMechanicalManaInfuser)) return false;
        renderModel(Tessellator.instance, (BlockMechanicalManaInfuser) block,
                world.getBlockMetadata(x, y, z), x, y, z,
                block.getMixedBrightnessForBlock(world, x, y, z));
        return true;
    }

    private static void renderModel(Tessellator t, BlockMechanicalManaInfuser block, int meta,
                                    double x, double y, double z, int brightness) {
        IIcon frame = block.getFrameIcon(meta);
        IIcon stone = block.getInnerStoneIcon(meta);
        IIcon lapis = block.getLapisIcon();
        IIcon core = block.getCoreIcon();
        RenderMachineModelUtil.frame(t, frame, x, y, z, brightness);

        solid(t, stone, x,y,z, 2.6,1,2.6, 6.2,4.6,6.2, brightness);
        solid(t, lapis, x,y,z, 6.2,1,2.6, 9.8,4.6,6.2, brightness);
        solid(t, stone, x,y,z, 9.8,1,2.6, 13.4,4.6,6.2, brightness);
        solid(t, lapis, x,y,z, 2.6,1,6.2, 6.2,4.6,9.8, brightness);
        solid(t, lapis, x,y,z, 9.8,1,6.2, 13.4,4.6,9.8, brightness);
        solid(t, stone, x,y,z, 2.6,1,9.8, 6.2,4.6,13.4, brightness);
        solid(t, lapis, x,y,z, 6.2,1,9.8, 9.8,4.6,13.4, brightness);
        solid(t, stone, x,y,z, 9.8,1,9.8, 13.4,4.6,13.4, brightness);

        // MythicBotany has no 1.7.10 equivalent, so the center is explicitly
        // DML/Botania-native instead of borrowing MythicBotany artwork.
        solid(t, core, x,y,z, 6.2,4.6,6.2, 9.8,5.5,9.8, brightness);
    }

    private static void solid(Tessellator t, IIcon icon, double x,double y,double z,
                              double x1,double y1,double z1,double x2,double y2,double z2,int brightness) {
        RenderMachineModelUtil.box(t,x,y,z,x1,y1,z1,x2,y2,z2,brightness,
                icon,FULL,icon,FULL,icon,FULL,icon,FULL,icon,FULL,icon,FULL);
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }
}
