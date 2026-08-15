package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.block.BlockBloodAltarAssembler;
import com.nosferatu.divinemachinerylegacy.tile.TileBloodAltarAssemblerNetworked;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

import static com.nosferatu.divinemachinerylegacy.client.render.RenderMachineModelUtil.FULL;
import static com.nosferatu.divinemachinerylegacy.client.render.RenderMachineModelUtil.box;

/**
 * 1.7.10 recreation of bmaddon's Molecular Assembler based model.
 * The dimensions are copied from AE2 rv3's RenderBlockAssembler; the outer
 * material and animated powered-light texture are the permitted bmaddon assets.
 */
public final class RenderBloodAltarAssembler implements ISimpleBlockRenderingHandler {
    private static final int FULL_BRIGHT = 15728880;
    private final int renderId;

    public RenderBloodAltarAssembler(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockBloodAltarAssembler)) return;
        BlockBloodAltarAssembler assembler = (BlockBloodAltarAssembler) block;

        Tessellator tessellator = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tessellator.startDrawingQuads();
        renderAssembler(tessellator, assembler.getBaseIcon(), 0, 0, 0, FULL_BRIGHT);
        tessellator.draw();
        GL11.glPopMatrix();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockBloodAltarAssembler)) return false;
        BlockBloodAltarAssembler assembler = (BlockBloodAltarAssembler) block;
        Tessellator tessellator = Tessellator.instance;

        int brightness = block.getMixedBrightnessForBlock(world, x, y, z);
        renderAssembler(tessellator, assembler.getBaseIcon(), x, y, z, brightness);

        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileBloodAltarAssemblerNetworked
                && ((TileBloodAltarAssemblerNetworked) tile).isNetworkPowered()
                && assembler.getLightsIcon() != null) {
            // bmaddon only draws this layer while the AE2 node is powered. A
            // tiny 1/1024 block expansion prevents z-fighting with the inner shell.
            IIcon lights = assembler.getLightsIcon();
            box(tessellator, x, y, z,
                    0.984375D, 0.984375D, 0.984375D,
                    15.015625D, 15.015625D, 15.015625D,
                    FULL_BRIGHT,
                    lights, FULL, lights, FULL, lights, FULL,
                    lights, FULL, lights, FULL, lights, FULL);
        }

        return true;
    }

    private static void renderAssembler(Tessellator t, IIcon icon,
                                        double x, double y, double z, int brightness) {
        if (icon == null) return;

        // Exact static cuboids used by AE2 rv3's Molecular Assembler renderer.
        cuboid(t, icon, x, y, z, 2, 14, 0, 14, 16, 2, brightness);
        cuboid(t, icon, x, y, z, 0, 14, 2, 2, 16, 14, brightness);
        cuboid(t, icon, x, y, z, 2, 0, 14, 14, 2, 16, brightness);
        cuboid(t, icon, x, y, z, 14, 0, 2, 16, 2, 14, brightness);
        cuboid(t, icon, x, y, z, 0, 0, 0, 16, 2, 2, brightness);
        cuboid(t, icon, x, y, z, 0, 2, 0, 2, 16, 2, brightness);
        cuboid(t, icon, x, y, z, 0, 0, 2, 2, 2, 16, brightness);
        cuboid(t, icon, x, y, z, 0, 14, 14, 16, 16, 16, brightness);
        cuboid(t, icon, x, y, z, 14, 0, 14, 16, 14, 16, brightness);
        cuboid(t, icon, x, y, z, 14, 14, 0, 16, 16, 14, brightness);
        cuboid(t, icon, x, y, z, 14, 2, 0, 16, 14, 2, brightness);
        cuboid(t, icon, x, y, z, 0, 2, 14, 2, 14, 16, brightness);
        cuboid(t, icon, x, y, z, 1, 1, 1, 15, 15, 15, brightness);
    }

    private static void cuboid(Tessellator t, IIcon icon,
                               double ox, double oy, double oz,
                               double x1, double y1, double z1,
                               double x2, double y2, double z2, int brightness) {
        box(t, ox, oy, oz, x1, y1, z1, x2, y2, z2, brightness,
                icon, FULL, icon, FULL, icon, FULL,
                icon, FULL, icon, FULL, icon, FULL);
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }
}
