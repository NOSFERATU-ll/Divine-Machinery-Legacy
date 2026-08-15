package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

/** Renders bmaddon's original Blood Generator OBJ on the 1.7.10 client. */
public class RenderBloodGenerator extends TileEntitySpecialRenderer implements IItemRenderer {
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(
            DivineMachineryLegacy.MODID, "models/block/blood_generator.obj");
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(
            DivineMachineryLegacy.MODID, "textures/blocks/bloodmagic/blood_generator.png");

    private final IModelCustom model = AdvancedModelLoader.loadModel(MODEL_LOCATION);

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        bindTexture(TEXTURE_LOCATION);
        model.renderAll();
        GL11.glPopMatrix();
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        GL11.glPushMatrix();
        GL11.glColor4f(1F, 1F, 1F, 1F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE_LOCATION);

        switch (type) {
            case INVENTORY:
                // Approximate the original modern GUI transform: 30/225 degrees,
                // scaled to sit comfortably inside the 16x16 inventory cell.
                GL11.glTranslatef(8F, 8F, 0F);
                GL11.glScalef(10F, 10F, 10F);
                GL11.glRotatef(30F, 1F, 0F, 0F);
                GL11.glRotatef(225F, 0F, 1F, 0F);
                GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
                break;
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                GL11.glScalef(0.75F, 0.75F, 0.75F);
                GL11.glRotatef(45F, 0F, 1F, 0F);
                GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
                break;
            case ENTITY:
            default:
                GL11.glTranslatef(-0.5F, 0F, -0.5F);
                GL11.glScalef(0.5F, 0.5F, 0.5F);
                break;
        }

        model.renderAll();
        GL11.glPopMatrix();
    }
}
