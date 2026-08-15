package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.block.BlockGreenhouse;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Exact three-element geometry and UV layout from Reforked's greenhouse.json. */
public class RenderGreenhouse implements ISimpleBlockRenderingHandler {
    private final int renderId;

    public RenderGreenhouse(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockGreenhouse)) return;
        Tessellator t = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        t.startDrawingQuads();
        render(t, (BlockGreenhouse) block, 0, 0, 0, 15728880);
        t.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block,
                                    int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockGreenhouse)) return false;
        render(Tessellator.instance, (BlockGreenhouse) block, x, y, z,
                block.getMixedBrightnessForBlock(world, x, y, z));
        return true;
    }

    private static void render(Tessellator t, BlockGreenhouse block,
                               double x, double y, double z, int brightness) {
        IIcon icon = block.getMachineIcon();

        RenderMachineModelUtil.box(t, x, y, z, 0, 0, 0, 16, 1, 16, brightness,
                icon, uv(4, 4, 0, 8),
                icon, uv(4, 4, 0, 0),
                icon, uv(8, 2.5, 12, 2.75),
                icon, uv(8, 3, 12, 3.25),
                icon, uv(8, 3.25, 12, 3.5),
                icon, uv(8, 2.75, 12, 3));

        RenderMachineModelUtil.box(t, x, y, z, 3, 1, 3, 13, 2, 13, brightness,
                icon, uv(9, 0, 6.5, 2.5),
                icon, uv(6.5, 7.5, 4, 5),
                icon, uv(8.5, 7.5, 11, 7.75),
                icon, uv(9, 1.25, 11.5, 1.5),
                icon, uv(9, 1.5, 11.5, 1.75),
                icon, uv(9, 1, 11.5, 1.25));

        rotatedBoxY(t, icon, x, y, z, 3, 1, 3, 13, 1.5, 13,
                8, 8, Math.toRadians(45), brightness,
                uv(6.5, 2.5, 4, 5),
                uv(6.5, 2.5, 4, 0),
                uv(10, 10.25, 12.5, 10.375),
                uv(10.5, 0.25, 13, 0.375),
                uv(10.5, 0.5, 13, 0.625),
                uv(10.5, 0, 13, 0.125));
    }

    private static RenderMachineModelUtil.UV uv(double u0, double v0, double u1, double v1) {
        return new RenderMachineModelUtil.UV(u0, v0, u1, v1);
    }

    private static void rotatedBoxY(Tessellator t, IIcon icon,
                                    double ox, double oy, double oz,
                                    double x1, double y1, double z1,
                                    double x2, double y2, double z2,
                                    double cx, double cz, double angle, int brightness,
                                    RenderMachineModelUtil.UV down,
                                    RenderMachineModelUtil.UV up,
                                    RenderMachineModelUtil.UV north,
                                    RenderMachineModelUtil.UV south,
                                    RenderMachineModelUtil.UV west,
                                    RenderMachineModelUtil.UV east) {
        quad(t, icon, down, true, brightness, .5F, 0, -1, 0, angle, cx, cz, ox, oy, oz,
                p(x1,y1,z2), p(x1,y1,z1), p(x2,y1,z1), p(x2,y1,z2));
        quad(t, icon, up, false, brightness, 1F, 0, 1, 0, angle, cx, cz, ox, oy, oz,
                p(x1,y2,z1), p(x1,y2,z2), p(x2,y2,z2), p(x2,y2,z1));
        quad(t, icon, north, false, brightness, .8F, 0, 0, -1, angle, cx, cz, ox, oy, oz,
                p(x2,y2,z1), p(x2,y1,z1), p(x1,y1,z1), p(x1,y2,z1));
        quad(t, icon, south, false, brightness, .8F, 0, 0, 1, angle, cx, cz, ox, oy, oz,
                p(x1,y2,z2), p(x1,y1,z2), p(x2,y1,z2), p(x2,y2,z2));
        quad(t, icon, west, false, brightness, .6F, -1, 0, 0, angle, cx, cz, ox, oy, oz,
                p(x1,y2,z1), p(x1,y1,z1), p(x1,y1,z2), p(x1,y2,z2));
        quad(t, icon, east, false, brightness, .6F, 1, 0, 0, angle, cx, cz, ox, oy, oz,
                p(x2,y2,z2), p(x2,y1,z2), p(x2,y1,z1), p(x2,y2,z1));
    }

    private static double[] p(double x, double y, double z) {
        return new double[] {x, y, z};
    }

    private static void quad(Tessellator t, IIcon icon, RenderMachineModelUtil.UV uv,
                             boolean downOrder, int brightness, float shade,
                             double nx, double ny, double nz,
                             double angle, double cx, double cz,
                             double ox, double oy, double oz,
                             double[] a, double[] b, double[] c, double[] d) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double rnx = nx * cos - nz * sin;
        double rnz = nx * sin + nz * cos;
        t.setBrightness(brightness);
        t.setColorOpaque_F(shade, shade, shade);
        t.setNormal((float) rnx, (float) ny, (float) rnz);

        double u0 = icon.getInterpolatedU(uv.u0);
        double u1 = icon.getInterpolatedU(uv.u1);
        double v0 = icon.getInterpolatedV(uv.v0);
        double v1 = icon.getInterpolatedV(uv.v1);
        if (downOrder) {
            vertex(t, a, ox, oy, oz, cx, cz, cos, sin, u0, v1);
            vertex(t, b, ox, oy, oz, cx, cz, cos, sin, u0, v0);
            vertex(t, c, ox, oy, oz, cx, cz, cos, sin, u1, v0);
            vertex(t, d, ox, oy, oz, cx, cz, cos, sin, u1, v1);
        } else {
            vertex(t, a, ox, oy, oz, cx, cz, cos, sin, u0, v0);
            vertex(t, b, ox, oy, oz, cx, cz, cos, sin, u0, v1);
            vertex(t, c, ox, oy, oz, cx, cz, cos, sin, u1, v1);
            vertex(t, d, ox, oy, oz, cx, cz, cos, sin, u1, v0);
        }
    }

    private static void vertex(Tessellator t, double[] p,
                               double ox, double oy, double oz,
                               double cx, double cz, double cos, double sin,
                               double u, double v) {
        double dx = p[0] - cx;
        double dz = p[2] - cz;
        double rx = cx + dx * cos - dz * sin;
        double rz = cz + dx * sin + dz * cos;
        t.addVertexWithUV(ox + rx / 16D, oy + p[1] / 16D, oz + rz / 16D, u, v);
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }
}
