package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalRunicAltar;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/**
 * Native 1.7.10 renderer for the Extra Reforked Mechanical Runic Altar model.
 *
 * Geometry is ported from the original 1.20.1 block model: a Dragonstone
 * frame surrounding Botania's Runic Altar body. This intentionally does not
 * render the machine as a textured cube.
 */
public class RenderMechanicalRunicAltar implements ISimpleBlockRenderingHandler {

    private static final double U = 1D / 16D;

    private static final UV FULL = new UV(0, 0, 16, 16);
    private static final UV UV_16_1 = new UV(0, 0, 16, 1);
    private static final UV UV_1_1 = new UV(0, 0, 1, 1);
    private static final UV UV_1_15 = new UV(0, 1, 1, 15);
    private static final UV UV_1_15_HORIZONTAL = new UV(0, 1, 1, 15);
    private static final UV UV_15_1 = new UV(1, 0, 15, 1);
    private static final UV ALTAR_BODY_SIDE = new UV(0, 4, 16, 10);
    private static final UV ALTAR_STEM_SIDE = new UV(4, 10, 12, 12);
    private static final UV ALTAR_BASE_TOP = new UV(2, 2, 14, 14);
    private static final UV ALTAR_BASE_SIDE = new UV(2, 12, 14, 16);

    private final int renderId;

    public RenderMechanicalRunicAltar(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockMechanicalRunicAltar)) return;

        BlockMechanicalRunicAltar altar = (BlockMechanicalRunicAltar) block;
        Tessellator tess = Tessellator.instance;

        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tess.startDrawingQuads();
        tess.setBrightness(15728880);
        renderModel(tess, altar, metadata, 0D, 0D, 0D, 15728880);
        tess.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
                                    int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockMechanicalRunicAltar)) return false;

        BlockMechanicalRunicAltar altar = (BlockMechanicalRunicAltar) block;
        Tessellator tess = Tessellator.instance;
        int brightness = block.getMixedBrightnessForBlock(world, x, y, z);
        renderModel(tess, altar, world.getBlockMetadata(x, y, z), x, y, z, brightness);
        return true;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }

    private static void renderModel(Tessellator t, BlockMechanicalRunicAltar block, int meta,
                                    double ox, double oy, double oz, int brightness) {
        IIcon frame = block.getFrameIcon(meta);
        IIcon bottom = block.getRuneBottomIcon();
        IIcon top = block.getRuneTopIcon();
        IIcon side = block.getRuneSideIcon();

        // Extra Reforked model element 0: full bottom frame plate.
        box(t, ox, oy, oz, 0, 0, 0, 16, 1, 16, brightness,
                frame, FULL, frame, FULL,
                frame, UV_16_1, frame, UV_16_1,
                frame, UV_16_1, frame, UV_16_1);

        // Elements 1-4: four vertical frame posts.
        framePost(t, ox, oy, oz, 0, 1, 0, 1, 15, 1, frame, brightness);
        framePost(t, ox, oy, oz, 15, 1, 0, 16, 15, 1, frame, brightness);
        framePost(t, ox, oy, oz, 0, 1, 15, 1, 15, 16, frame, brightness);
        framePost(t, ox, oy, oz, 15, 1, 15, 16, 15, 16, frame, brightness);

        // Elements 5-6: left/right upper rails running north-south.
        box(t, ox, oy, oz, 0, 15, 1, 1, 16, 15, brightness,
                frame, UV_1_15_HORIZONTAL, frame, UV_1_15_HORIZONTAL,
                frame, UV_1_1, frame, UV_1_1,
                frame, UV_15_1, frame, UV_15_1);
        box(t, ox, oy, oz, 15, 15, 1, 16, 16, 15, brightness,
                frame, UV_1_15_HORIZONTAL, frame, UV_1_15_HORIZONTAL,
                frame, UV_1_1, frame, UV_1_1,
                frame, UV_15_1, frame, UV_15_1);

        // Elements 7-8: front/back upper rails running east-west.
        box(t, ox, oy, oz, 0, 15, 0, 16, 16, 1, brightness,
                frame, UV_16_1, frame, UV_16_1,
                frame, UV_16_1, frame, UV_16_1,
                frame, UV_1_1, frame, UV_1_1);
        box(t, ox, oy, oz, 0, 15, 15, 16, 16, 16, brightness,
                frame, UV_16_1, frame, UV_16_1,
                frame, UV_16_1, frame, UV_16_1,
                frame, UV_1_1, frame, UV_1_1);

        // Elements 9-11: the actual Botania Runic Altar inside the frame.
        box(t, ox, oy, oz, 2, 5, 2, 14, 9, 14, brightness,
                bottom, FULL, top, FULL,
                side, ALTAR_BODY_SIDE, side, ALTAR_BODY_SIDE,
                side, ALTAR_BODY_SIDE, side, ALTAR_BODY_SIDE);

        box(t, ox, oy, oz, 6, 3, 6, 10, 5, 10, brightness,
                null, null, null, null,
                side, ALTAR_STEM_SIDE, side, ALTAR_STEM_SIDE,
                side, ALTAR_STEM_SIDE, side, ALTAR_STEM_SIDE);

        box(t, ox, oy, oz, 4, 1, 4, 12, 3, 12, brightness,
                bottom, ALTAR_BASE_TOP, bottom, ALTAR_BASE_TOP,
                side, ALTAR_BASE_SIDE, side, ALTAR_BASE_SIDE,
                side, ALTAR_BASE_SIDE, side, ALTAR_BASE_SIDE);
    }

    private static void framePost(Tessellator t, double ox, double oy, double oz,
                                  int x1, int y1, int z1, int x2, int y2, int z2,
                                  IIcon frame, int brightness) {
        box(t, ox, oy, oz, x1, y1, z1, x2, y2, z2, brightness,
                frame, UV_1_1, frame, UV_1_1,
                frame, UV_1_15, frame, UV_1_15,
                frame, UV_1_15, frame, UV_1_15);
    }

    private static void box(Tessellator t, double ox, double oy, double oz,
                            int px1, int py1, int pz1, int px2, int py2, int pz2,
                            int brightness,
                            IIcon down, UV downUv, IIcon up, UV upUv,
                            IIcon north, UV northUv, IIcon south, UV southUv,
                            IIcon west, UV westUv, IIcon east, UV eastUv) {
        double x1 = ox + px1 * U;
        double y1 = oy + py1 * U;
        double z1 = oz + pz1 * U;
        double x2 = ox + px2 * U;
        double y2 = oy + py2 * U;
        double z2 = oz + pz2 * U;

        if (down != null) faceDown(t, x1, y1, z1, x2, z2, down, downUv, brightness);
        if (up != null) faceUp(t, x1, y2, z1, x2, z2, up, upUv, brightness);
        if (north != null) faceNorth(t, x1, y1, z1, x2, y2, north, northUv, brightness);
        if (south != null) faceSouth(t, x1, y1, z2, x2, y2, south, southUv, brightness);
        if (west != null) faceWest(t, x1, y1, z1, y2, z2, west, westUv, brightness);
        if (east != null) faceEast(t, x2, y1, z1, y2, z2, east, eastUv, brightness);
    }

    private static void faceDown(Tessellator t, double x1, double y, double z1, double x2, double z2,
                                 IIcon icon, UV uv, int brightness) {
        prepare(t, brightness, 0.5F, 0F, -1F, 0F);
        double u0 = icon.getInterpolatedU(uv.u0), u1 = icon.getInterpolatedU(uv.u1);
        double v0 = icon.getInterpolatedV(uv.v0), v1 = icon.getInterpolatedV(uv.v1);
        t.addVertexWithUV(x1, y, z2, u0, v1);
        t.addVertexWithUV(x1, y, z1, u0, v0);
        t.addVertexWithUV(x2, y, z1, u1, v0);
        t.addVertexWithUV(x2, y, z2, u1, v1);
    }

    private static void faceUp(Tessellator t, double x1, double y, double z1, double x2, double z2,
                               IIcon icon, UV uv, int brightness) {
        prepare(t, brightness, 1F, 0F, 1F, 0F);
        double u0 = icon.getInterpolatedU(uv.u0), u1 = icon.getInterpolatedU(uv.u1);
        double v0 = icon.getInterpolatedV(uv.v0), v1 = icon.getInterpolatedV(uv.v1);
        t.addVertexWithUV(x1, y, z1, u0, v0);
        t.addVertexWithUV(x1, y, z2, u0, v1);
        t.addVertexWithUV(x2, y, z2, u1, v1);
        t.addVertexWithUV(x2, y, z1, u1, v0);
    }

    private static void faceNorth(Tessellator t, double x1, double y1, double z, double x2, double y2,
                                  IIcon icon, UV uv, int brightness) {
        prepare(t, brightness, 0.8F, 0F, 0F, -1F);
        double u0 = icon.getInterpolatedU(uv.u0), u1 = icon.getInterpolatedU(uv.u1);
        double v0 = icon.getInterpolatedV(uv.v0), v1 = icon.getInterpolatedV(uv.v1);
        t.addVertexWithUV(x2, y2, z, u0, v0);
        t.addVertexWithUV(x2, y1, z, u0, v1);
        t.addVertexWithUV(x1, y1, z, u1, v1);
        t.addVertexWithUV(x1, y2, z, u1, v0);
    }

    private static void faceSouth(Tessellator t, double x1, double y1, double z, double x2, double y2,
                                  IIcon icon, UV uv, int brightness) {
        prepare(t, brightness, 0.8F, 0F, 0F, 1F);
        double u0 = icon.getInterpolatedU(uv.u0), u1 = icon.getInterpolatedU(uv.u1);
        double v0 = icon.getInterpolatedV(uv.v0), v1 = icon.getInterpolatedV(uv.v1);
        t.addVertexWithUV(x1, y2, z, u0, v0);
        t.addVertexWithUV(x1, y1, z, u0, v1);
        t.addVertexWithUV(x2, y1, z, u1, v1);
        t.addVertexWithUV(x2, y2, z, u1, v0);
    }

    private static void faceWest(Tessellator t, double x, double y1, double z1, double y2, double z2,
                                 IIcon icon, UV uv, int brightness) {
        prepare(t, brightness, 0.6F, -1F, 0F, 0F);
        double u0 = icon.getInterpolatedU(uv.u0), u1 = icon.getInterpolatedU(uv.u1);
        double v0 = icon.getInterpolatedV(uv.v0), v1 = icon.getInterpolatedV(uv.v1);
        t.addVertexWithUV(x, y2, z1, u0, v0);
        t.addVertexWithUV(x, y1, z1, u0, v1);
        t.addVertexWithUV(x, y1, z2, u1, v1);
        t.addVertexWithUV(x, y2, z2, u1, v0);
    }

    private static void faceEast(Tessellator t, double x, double y1, double z1, double y2, double z2,
                                 IIcon icon, UV uv, int brightness) {
        prepare(t, brightness, 0.6F, 1F, 0F, 0F);
        double u0 = icon.getInterpolatedU(uv.u0), u1 = icon.getInterpolatedU(uv.u1);
        double v0 = icon.getInterpolatedV(uv.v0), v1 = icon.getInterpolatedV(uv.v1);
        t.addVertexWithUV(x, y2, z2, u0, v0);
        t.addVertexWithUV(x, y1, z2, u0, v1);
        t.addVertexWithUV(x, y1, z1, u1, v1);
        t.addVertexWithUV(x, y2, z1, u1, v0);
    }

    private static void prepare(Tessellator t, int brightness, float shade, float nx, float ny, float nz) {
        t.setBrightness(brightness);
        t.setColorOpaque_F(shade, shade, shade);
        t.setNormal(nx, ny, nz);
    }

    private static final class UV {
        final double u0;
        final double v0;
        final double u1;
        final double v1;

        UV(double u0, double v0, double u1, double v1) {
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
        }
    }
}
