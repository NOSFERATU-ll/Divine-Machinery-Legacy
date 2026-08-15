package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.gaiarelics.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

/**
 * Original low-poly 3D presentation for the Gaia Relics backport.
 * Uses only geometry/colors, so no Gaia Relics textures are redistributed.
 */
public final class RenderGaiaRelicItem implements IItemRenderer {
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return item != null && (item.getItem() instanceof ItemGaiaRelicMaterial
                || item.getItem() instanceof ItemGaiaBlade
                || item.getItem() instanceof ItemGaiaEchoBlade
                || item.getItem() instanceof ItemValkyrieFeather);
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        switch (type) {
            case INVENTORY:
                GL11.glTranslatef(8F, 8F, 0F);
                GL11.glScalef(9F, 9F, 9F);
                GL11.glRotatef(25F, 1F, 0F, 0F);
                GL11.glRotatef(45F, 0F, 1F, 0F);
                break;
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:
                GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                GL11.glScalef(0.9F, 0.9F, 0.9F);
                GL11.glRotatef(35F, 0F, 1F, 0F);
                break;
            case ENTITY:
            default:
                GL11.glScalef(0.8F, 0.8F, 0.8F);
                break;
        }

        if (stack.getItem() instanceof ItemGaiaRelicMaterial) {
            renderMaterial((ItemGaiaRelicMaterial) stack.getItem());
        } else if (stack.getItem() instanceof ItemGaiaBlade) {
            renderBlade(0.25F, 0.95F, 0.55F, true);
        } else if (stack.getItem() instanceof ItemGaiaEchoBlade) {
            renderBlade(0.45F, 0.75F, 1.0F, false);
        } else if (stack.getItem() instanceof ItemValkyrieFeather) {
            renderFeather();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private static void renderMaterial(ItemGaiaRelicMaterial item) {
        ItemGaiaRelicMaterial.Kind kind = item.getKind();
        boolean ingot = kind.name().endsWith("INGOT");
        float r, g, b;
        if (kind.name().startsWith("YGGDRASIL")) { r = 0.25F; g = 0.95F; b = 0.45F; }
        else if (kind.name().startsWith("MUSPEL")) { r = 1.0F; g = 0.32F; b = 0.08F; }
        else { r = 0.28F; g = 0.72F; b = 1.0F; }

        if (ingot) {
            cuboid(-0.55F, -0.20F, -0.22F, 0.55F, 0.20F, 0.22F, r * 0.7F, g * 0.7F, b * 0.7F, 1F);
            cuboid(-0.42F, 0.18F, -0.18F, 0.42F, 0.32F, 0.18F, r, g, b, 1F);
            cuboid(-0.30F, -0.31F, -0.16F, 0.30F, -0.18F, 0.16F, r * 0.55F, g * 0.55F, b * 0.55F, 1F);
        } else {
            crystal(r, g, b);
            GL11.glScalef(0.66F, 0.66F, 0.66F);
            GL11.glRotatef(45F, 0F, 1F, 0F);
            crystal(Math.min(1F, r + 0.25F), Math.min(1F, g + 0.25F), Math.min(1F, b + 0.25F));
        }
    }

    private static void crystal(float r, float g, float b) {
        GL11.glColor4f(r, g, b, 0.92F);
        GL11.glBegin(GL11.GL_TRIANGLES);
        tri(0, .72F, 0, -.38F, 0, -.28F, .38F, 0, -.28F);
        tri(0, .72F, 0, .38F, 0, -.28F, .38F, 0, .28F);
        tri(0, .72F, 0, .38F, 0, .28F, -.38F, 0, .28F);
        tri(0, .72F, 0, -.38F, 0, .28F, -.38F, 0, -.28F);
        tri(0, -.72F, 0, .38F, 0, -.28F, -.38F, 0, -.28F);
        tri(0, -.72F, 0, .38F, 0, .28F, .38F, 0, -.28F);
        tri(0, -.72F, 0, -.38F, 0, .28F, .38F, 0, .28F);
        tri(0, -.72F, 0, -.38F, 0, -.28F, -.38F, 0, .28F);
        GL11.glEnd();
    }

    private static void renderBlade(float r, float g, float b, boolean ornate) {
        GL11.glRotatef(-42F, 0F, 0F, 1F);
        cuboid(-0.08F, -0.80F, -0.06F, 0.08F, -0.22F, 0.06F, 0.28F, 0.18F, 0.10F, 1F);
        cuboid(-0.32F, -0.24F, -0.08F, 0.32F, -0.10F, 0.08F, 0.85F, 0.72F, 0.22F, 1F);
        cuboid(-0.13F, -0.10F, -0.045F, 0.13F, 0.76F, 0.045F, r, g, b, 1F);
        GL11.glColor4f(Math.min(1F, r + .25F), Math.min(1F, g + .25F), Math.min(1F, b + .25F), .85F);
        GL11.glBegin(GL11.GL_TRIANGLES);
        tri(-.13F, .76F, -.045F, .13F, .76F, -.045F, 0F, 1.12F, 0F);
        tri(-.13F, .76F, .045F, 0F, 1.12F, 0F, .13F, .76F, .045F);
        GL11.glEnd();
        if (ornate) {
            cuboid(-0.22F, 0.18F, -0.075F, -0.12F, 0.55F, 0.075F, .75F, .95F, .40F, .85F);
            cuboid(0.12F, 0.18F, -0.075F, 0.22F, 0.55F, 0.075F, .75F, .95F, .40F, .85F);
        }
    }

    private static void renderFeather() {
        GL11.glRotatef(-28F, 0F, 0F, 1F);
        cuboid(-0.035F, -0.78F, -0.03F, 0.035F, 0.72F, 0.03F, .90F, .72F, .25F, 1F);
        GL11.glColor4f(.82F, .94F, 1F, .92F);
        GL11.glBegin(GL11.GL_TRIANGLES);
        for (int i = 0; i < 6; i++) {
            float y = -0.48F + i * 0.20F;
            float w = 0.46F - i * 0.035F;
            tri(0F, y, 0F, -w, y + .12F, 0F, 0F, y + .22F, 0F);
            tri(0F, y + .02F, 0F, 0F, y + .22F, 0F, w, y + .13F, 0F);
        }
        GL11.glEnd();
    }

    private static void cuboid(float x1, float y1, float z1, float x2, float y2, float z2,
                               float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        quad(x1,y1,z1, x2,y1,z1, x2,y2,z1, x1,y2,z1);
        quad(x2,y1,z2, x1,y1,z2, x1,y2,z2, x2,y2,z2);
        quad(x1,y1,z2, x1,y1,z1, x1,y2,z1, x1,y2,z2);
        quad(x2,y1,z1, x2,y1,z2, x2,y2,z2, x2,y2,z1);
        quad(x1,y2,z1, x2,y2,z1, x2,y2,z2, x1,y2,z2);
        quad(x1,y1,z2, x2,y1,z2, x2,y1,z1, x1,y1,z1);
        GL11.glEnd();
    }

    private static void quad(float... v) {
        for (int i = 0; i < 12; i += 3) GL11.glVertex3f(v[i], v[i+1], v[i+2]);
    }

    private static void tri(float x1,float y1,float z1,float x2,float y2,float z2,float x3,float y3,float z3) {
        GL11.glVertex3f(x1,y1,z1); GL11.glVertex3f(x2,y2,z2); GL11.glVertex3f(x3,y3,z3);
    }
}
