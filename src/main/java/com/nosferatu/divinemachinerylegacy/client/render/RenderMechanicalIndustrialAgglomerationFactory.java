package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalIndustrialAgglomerationFactory;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Exact cuboid geometry of Extra Reforked's Industrial Agglomeration Factory. */
public class RenderMechanicalIndustrialAgglomerationFactory implements ISimpleBlockRenderingHandler {
    private static final RenderMachineModelUtil.UV FULL = RenderMachineModelUtil.FULL;
    private static final RenderMachineModelUtil.UV TERRA_SIDE = new RenderMachineModelUtil.UV(0, 13, 16, 16);

    private final int renderId;

    public RenderMechanicalIndustrialAgglomerationFactory(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockMechanicalIndustrialAgglomerationFactory)) return;
        Tessellator tess = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tess.startDrawingQuads();
        renderModel(tess, (BlockMechanicalIndustrialAgglomerationFactory) block,
                metadata, 0D, 0D, 0D, 15728880);
        tess.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
                                    int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockMechanicalIndustrialAgglomerationFactory)) return false;
        int brightness = block.getMixedBrightnessForBlock(world, x, y, z);
        renderModel(Tessellator.instance, (BlockMechanicalIndustrialAgglomerationFactory) block,
                world.getBlockMetadata(x, y, z), x, y, z, brightness);
        return true;
    }

    private static void renderModel(Tessellator t, BlockMechanicalIndustrialAgglomerationFactory block,
                                    int meta, double x, double y, double z, int brightness) {
        IIcon frame = block.getFrameIcon(meta);
        IIcon stone = block.getInnerStoneIcon(meta);
        IIcon lapis = block.getLapisIcon();
        IIcon terraTop = block.getTerraTopIcon();
        IIcon terraSide = block.getTerraSideIcon();

        RenderMachineModelUtil.frame(t, frame, x, y, z, brightness);

        // Miniature 3x3 Terra Plate platform from the original Reforked JSON.
        solid(t, stone, x, y, z, 2.6, 1, 2.6, 6.2, 4.6, 6.2, brightness);
        solid(t, lapis, x, y, z, 6.2, 1, 2.6, 9.8, 4.6, 6.2, brightness);
        solid(t, stone, x, y, z, 9.8, 1, 2.6, 13.4, 4.6, 6.2, brightness);

        solid(t, lapis, x, y, z, 2.6, 1, 6.2, 6.2, 4.6, 9.8, brightness);
        solid(t, lapis, x, y, z, 9.8, 1, 6.2, 13.4, 4.6, 9.8, brightness);

        solid(t, stone, x, y, z, 2.6, 1, 9.8, 6.2, 4.6, 13.4, brightness);
        solid(t, lapis, x, y, z, 6.2, 1, 9.8, 9.8, 4.6, 13.4, brightness);
        solid(t, stone, x, y, z, 9.8, 1, 9.8, 13.4, 4.6, 13.4, brightness);

        RenderMachineModelUtil.box(t, x, y, z,
                6.2, 4.6, 6.2, 9.8, 5.3, 9.8, brightness,
                terraTop, FULL, terraTop, FULL,
                terraSide, TERRA_SIDE, terraSide, TERRA_SIDE,
                terraSide, TERRA_SIDE, terraSide, TERRA_SIDE);
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
