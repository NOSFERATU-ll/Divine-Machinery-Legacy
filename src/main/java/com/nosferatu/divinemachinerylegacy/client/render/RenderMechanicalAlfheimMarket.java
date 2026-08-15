package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalAlfheimMarket;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Exact static cuboid geometry of Extra Reforked's Mechanical Alfheim Market. */
public class RenderMechanicalAlfheimMarket implements ISimpleBlockRenderingHandler {
    private static final RenderMachineModelUtil.UV FULL = RenderMachineModelUtil.FULL;
    private final int renderId;

    public RenderMechanicalAlfheimMarket(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockMechanicalAlfheimMarket)) return;
        Tessellator tess = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tess.startDrawingQuads();
        renderModel(tess, (BlockMechanicalAlfheimMarket) block, metadata, 0D, 0D, 0D, 15728880);
        tess.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
                                    int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockMechanicalAlfheimMarket)) return false;
        int brightness = block.getMixedBrightnessForBlock(world, x, y, z);
        renderModel(Tessellator.instance, (BlockMechanicalAlfheimMarket) block,
                world.getBlockMetadata(x, y, z), x, y, z, brightness);
        return true;
    }

    private static void renderModel(Tessellator t, BlockMechanicalAlfheimMarket block,
                                    int meta, double x, double y, double z, int brightness) {
        IIcon frame = block.getFrameIcon(meta);
        IIcon wood = block.getLivingwoodIcon();
        IIcon glimmer = block.getGlimmeringLivingwoodIcon();
        IIcon portal = block.getPortalIcon();

        RenderMachineModelUtil.frame(t, frame, x, y, z, brightness);

        // Miniature Alfheim portal, copied from the Reforked block model.
        solid(t, portal, x, y, z, 6.8, 1, 8.8, 9.2, 3.4, 11.2, brightness);
        solid(t, wood, x, y, z, 4.4, 1, 8.8, 6.8, 3.4, 11.2, brightness);
        solid(t, wood, x, y, z, 9.2, 1, 8.8, 11.6, 3.4, 11.2, brightness);
        solid(t, wood, x, y, z, 2, 3.4, 8.8, 4.4, 5.8, 11.2, brightness);
        solid(t, wood, x, y, z, 11.6, 3.4, 8.8, 14, 5.8, 11.2, brightness);
        solid(t, glimmer, x, y, z, 2, 5.8, 8.8, 4.4, 8.2, 11.2, brightness);
        solid(t, glimmer, x, y, z, 11.6, 5.8, 8.8, 14, 8.2, 11.2, brightness);
        solid(t, wood, x, y, z, 2, 8.2, 8.8, 4.4, 10.6, 11.2, brightness);
        solid(t, wood, x, y, z, 11.6, 8.2, 8.8, 14, 10.6, 11.2, brightness);
        solid(t, glimmer, x, y, z, 6.8, 10.6, 8.8, 9.2, 13, 11.2, brightness);
        solid(t, wood, x, y, z, 4.4, 10.6, 8.8, 6.8, 13, 11.2, brightness);
        solid(t, wood, x, y, z, 9.2, 10.6, 8.8, 11.6, 13, 11.2, brightness);
    }

    private static void solid(Tessellator t, IIcon icon, double x, double y, double z,
                              double x1, double y1, double z1, double x2, double y2, double z2,
                              int brightness) {
        RenderMachineModelUtil.box(t, x, y, z, x1, y1, z1, x2, y2, z2, brightness,
                icon, FULL, icon, FULL, icon, FULL, icon, FULL, icon, FULL, icon, FULL);
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }
}
