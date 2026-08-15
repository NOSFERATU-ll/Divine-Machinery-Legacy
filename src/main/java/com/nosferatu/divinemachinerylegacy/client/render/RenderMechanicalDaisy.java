package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalDaisy;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import vazkii.botania.common.block.ModBlocks;

import static com.nosferatu.divinemachinerylegacy.client.render.RenderMachineModelUtil.*;

/** Exact Reforked plate / enchanted-soil / Pure Daisy silhouette. */
public class RenderMechanicalDaisy implements ISimpleBlockRenderingHandler {
    private static final double FLOWER_MIN = 8D - 3D / Math.sqrt(2D);
    private static final double FLOWER_MAX = 8D + 3D / Math.sqrt(2D);
    private final int renderId;

    public RenderMechanicalDaisy(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockMechanicalDaisy)) return;
        Tessellator t = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        t.startDrawingQuads();
        draw(t, (BlockMechanicalDaisy) block, metadata, 0, 0, 0, 15728880);
        t.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
                                    int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockMechanicalDaisy)) return false;
        draw(Tessellator.instance, (BlockMechanicalDaisy) block,
                world.getBlockMetadata(x, y, z), x, y, z,
                block.getMixedBrightnessForBlock(world, x, y, z));
        return true;
    }

    private static void draw(Tessellator t, BlockMechanicalDaisy block, int meta,
                             double x, double y, double z, int brightness) {
        IIcon plate = block.getPlate(meta);
        IIcon soilTop = ModBlocks.enchantedSoil.getIcon(1, 0);
        IIcon soilSide = ModBlocks.enchantedSoil.getIcon(2, 0);

        box(t, x, y, z, 0, 0, 0, 16, 2, 16, brightness,
                plate, new UV(1, 1, 15, 15),
                plate, new UV(1, 1, 15, 15),
                plate, new UV(1, 7, 15, 9),
                plate, new UV(1, 7, 15, 9),
                plate, new UV(1, 7, 15, 9),
                plate, new UV(1, 7, 15, 9));

        box(t, x, y, z, 5, 2, 5, 11, 3, 11, brightness,
                plate, new UV(5, 5, 11, 11),
                soilTop, new UV(5, 5, 11, 11),
                soilSide, new UV(5, 0, 11, 1),
                soilSide, new UV(5, 0, 11, 1),
                soilSide, new UV(5, 0, 11, 1),
                soilSide, new UV(5, 0, 11, 1));

        // Reforked starts with two 6-wide planes and rotates them 45 degrees.
        // Their projected endpoints are 8 +/- 3/sqrt(2), not 5..11.
        crossedPlant(t, x, y, z, FLOWER_MIN, 3, FLOWER_MIN,
                FLOWER_MAX, 11.4, FLOWER_MAX, block.getFlower(), brightness);
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) { return true; }

    @Override
    public int getRenderId() { return renderId; }
}
