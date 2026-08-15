package com.nosferatu.divinemachinerylegacy.gaiarelics;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import java.util.List;

public final class ItemGaiaEchoBlade extends ItemSword {
    public static final String TAG_ECHO = "DMLGaiaEcho";

    public ItemGaiaEchoBlade() {
        super(Item.ToolMaterial.EMERALD);
        setUnlocalizedName(DivineMachineryLegacy.MODID + ".gaia_echo_blade");
        setTextureName("minecraft:diamond_sword");
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setMaxDamage(0);
    }

    public static int getEcho(ItemStack stack) {
        if (stack == null || stack.getTagCompound() == null) return 0;
        return Math.max(0, Math.min(100, stack.getTagCompound().getInteger(TAG_ECHO)));
    }

    public static void setEcho(ItemStack stack, int value) {
        if (stack == null) return;
        if (stack.getTagCompound() == null) stack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
        stack.getTagCompound().setInteger(TAG_ECHO, Math.max(0, Math.min(100, value)));
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, net.minecraft.entity.player.EntityPlayer player, List list, boolean advanced) {
        int echo = getEcho(stack);
        list.add("§bEcho: " + echo + " / 100");
        list.add("§7Main Gaia loot x2 chance: " + echo + "%");
        list.add("§8Echo resets after killing Gaia.");
    }
}
