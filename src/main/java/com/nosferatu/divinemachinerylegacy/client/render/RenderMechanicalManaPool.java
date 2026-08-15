package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalManaPool;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;

import static com.nosferatu.divinemachinerylegacy.client.render.RenderMachineModelUtil.*;

/** Exact cuboid geometry of Extra Reforked's Mechanical Mana Pool model. */
public class RenderMechanicalManaPool implements ISimpleBlockRenderingHandler {
    private final int renderId;
    private static final UV FLOOR = new UV(0,0,16,16);
    private static final UV WALL_LONG = new UV(0,8,16,15);
    private static final UV WALL_SHORT = new UV(0,8,1,15);
    private static final UV WALL_SHORT_R = new UV(15,8,16,15);
    private static final UV WALL_INNER = new UV(1,8,15,15);
    private static final UV EDGE_LONG = new UV(0,0,16,1);
    private static final UV EDGE_LONG_REV = new UV(0,15,16,16);
    private static final UV EDGE_SIDE = new UV(15,1,16,15);
    private static final UV EDGE_SIDE_L = new UV(0,1,1,15);

    public RenderMechanicalManaPool(int renderId){this.renderId=renderId;}

    @Override public void renderInventoryBlock(Block block,int metadata,int modelId,RenderBlocks renderer){
        if(!(block instanceof BlockMechanicalManaPool))return;
        Tessellator t=Tessellator.instance;
        GL11.glPushMatrix(); GL11.glTranslatef(-.5F,-.5F,-.5F);
        t.startDrawingQuads(); t.setBrightness(15728880);
        render(t,(BlockMechanicalManaPool)block,metadata,0,0,0,15728880);
        t.draw(); GL11.glPopMatrix();
    }

    @Override public boolean renderWorldBlock(IBlockAccess world,int x,int y,int z,Block block,int modelId,RenderBlocks renderer){
        if(!(block instanceof BlockMechanicalManaPool))return false;
        render(Tessellator.instance,(BlockMechanicalManaPool)block,world.getBlockMetadata(x,y,z),x,y,z,block.getMixedBrightnessForBlock(world,x,y,z));
        return true;
    }

    private static void render(Tessellator t,BlockMechanicalManaPool b,int meta,double ox,double oy,double oz,int br){
        IIcon f=b.getFrameIcon(meta), p=b.getPoolIcon();
        frame(t,f,ox,oy,oz,br);
        box(t,ox,oy,oz,2,1,2,14,1.1,14,br,p,FLOOR,p,FLOOR,p,WALL_SHORT,p,WALL_SHORT,p,WALL_SHORT,p,WALL_SHORT);
        box(t,ox,oy,oz,2,1,13,14,6,14,br,p,EDGE_LONG,p,EDGE_LONG_REV,p,WALL_LONG,p,WALL_LONG,p,WALL_SHORT_R,p,WALL_SHORT);
        box(t,ox,oy,oz,2,1,2,14,6,3,br,p,EDGE_LONG_REV,p,EDGE_LONG,p,WALL_LONG,p,WALL_LONG,p,WALL_SHORT,p,WALL_SHORT_R);
        box(t,ox,oy,oz,13,1,3,14,6,13,br,p,EDGE_SIDE,p,EDGE_SIDE,p,WALL_SHORT,p,WALL_SHORT_R,p,WALL_INNER,p,WALL_INNER);
        box(t,ox,oy,oz,2,1,3,3,6,13,br,p,EDGE_SIDE_L,p,EDGE_SIDE_L,p,WALL_SHORT_R,p,WALL_SHORT,p,WALL_INNER,p,WALL_INNER);
    }

    @Override public boolean shouldRender3DInInventory(int modelId){return true;}
    @Override public int getRenderId(){return renderId;}
}
