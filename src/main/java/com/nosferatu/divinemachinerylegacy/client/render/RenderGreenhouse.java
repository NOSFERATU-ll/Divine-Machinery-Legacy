package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.block.BlockGreenhouse;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

/** Compact static 1.7.10 interpretation of Reforked's Greenhouse frame. */
public class RenderGreenhouse implements ISimpleBlockRenderingHandler {
    private final int renderId;
    public RenderGreenhouse(int renderId) { this.renderId = renderId; }

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
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        if (!(block instanceof BlockGreenhouse)) return false;
        render(Tessellator.instance, (BlockGreenhouse) block, x, y, z, block.getMixedBrightnessForBlock(world, x, y, z));
        return true;
    }

    private static void render(Tessellator t, BlockGreenhouse block, double x, double y, double z, int b) {
        IIcon i = block.getMachineIcon();
        RenderMachineModelUtil.UV f = RenderMachineModelUtil.FULL;
        box(t,x,y,z,0,0,0,16,2,16,b,i,f);
        box(t,x,y,z,3,2,3,13,4,13,b,i,f);
        // Four pylons.
        box(t,x,y,z,1,2,1,3,14,3,b,i,f); box(t,x,y,z,13,2,1,15,14,3,b,i,f);
        box(t,x,y,z,1,2,13,3,14,15,b,i,f); box(t,x,y,z,13,2,13,15,14,15,b,i,f);
        // Three hovering rectangular rings, visually matching the modern greenhouse cage.
        ring(t,x,y,z,4,1,15,b,i,f); ring(t,x,y,z,8,2,14,b,i,f); ring(t,x,y,z,12,3,13,b,i,f);
    }

    private static void ring(Tessellator t,double x,double y,double z,double py,double inset,double max,int b,IIcon i,RenderMachineModelUtil.UV f) {
        box(t,x,y,z,inset,py,inset,max,py+1,inset+1,b,i,f);
        box(t,x,y,z,inset,py,max-1,max,py+1,max,b,i,f);
        box(t,x,y,z,inset,py,inset+1,inset+1,py+1,max-1,b,i,f);
        box(t,x,y,z,max-1,py,inset+1,max,py+1,max-1,b,i,f);
    }

    private static void box(Tessellator t,double x,double y,double z,double x1,double y1,double z1,double x2,double y2,double z2,int b,IIcon i,RenderMachineModelUtil.UV f) {
        RenderMachineModelUtil.box(t,x,y,z,x1,y1,z1,x2,y2,z2,b,i,f,i,f,i,f,i,f,i,f,i,f);
    }

    @Override public boolean shouldRender3DInInventory(int modelId) { return true; }
    @Override public int getRenderId() { return renderId; }
}
