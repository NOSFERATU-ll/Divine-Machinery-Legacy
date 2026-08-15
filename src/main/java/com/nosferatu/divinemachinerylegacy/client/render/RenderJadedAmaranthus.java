package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.block.BlockJadedAmaranthus;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Exact static geometry of Extra Reforked's Jaded Amaranthus model. */
public class RenderJadedAmaranthus implements ISimpleBlockRenderingHandler {
    private static final RenderMachineModelUtil.UV PLATE_TOP = new RenderMachineModelUtil.UV(1, 1, 15, 15);
    private static final RenderMachineModelUtil.UV PLATE_SIDE = new RenderMachineModelUtil.UV(1, 7, 15, 9);
    private static final RenderMachineModelUtil.UV SOIL_TOP = new RenderMachineModelUtil.UV(5, 5, 11, 11);
    private static final RenderMachineModelUtil.UV SOIL_SIDE = new RenderMachineModelUtil.UV(5, 0, 11, 1);

    private final int renderId;

    public RenderJadedAmaranthus(int renderId) { this.renderId = renderId; }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockJadedAmaranthus)) return;
        Tessellator tess = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tess.startDrawingQuads();
        renderModel(tess, (BlockJadedAmaranthus) block, 0D, 0D, 0D, 15728880);
        tess.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
                                    int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockJadedAmaranthus)) return false;
        renderModel(Tessellator.instance, (BlockJadedAmaranthus) block,
                x, y, z, block.getMixedBrightnessForBlock(world, x, y, z));
        return true;
    }

    private static void renderModel(Tessellator t, BlockJadedAmaranthus block,
                                    double x, double y, double z, int brightness) {
        IIcon plate = block.getPlateIcon();
        IIcon soilTop = block.getSoilTopIcon();
        IIcon soilSide = block.getSoilSideIcon();

        RenderMachineModelUtil.box(t, x, y, z, 0, 0, 0, 16, 2, 16, brightness,
                plate, PLATE_TOP, plate, PLATE_TOP,
                plate, PLATE_SIDE, plate, PLATE_SIDE, plate, PLATE_SIDE, plate, PLATE_SIDE);

        RenderMachineModelUtil.box(t, x, y, z, 5, 2, 5, 11, 3, 11, brightness,
                plate, SOIL_TOP, soilTop, SOIL_TOP,
                soilSide, SOIL_SIDE, soilSide, SOIL_SIDE,
                soilSide, SOIL_SIDE, soilSide, SOIL_SIDE);

        RenderMachineModelUtil.crossedPlant(t, x, y, z,
                5, 3, 5, 11, 11.4, 11, block.getFlowerIcon(), brightness);
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }
}
