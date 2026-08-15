package com.nosferatu.divinemachinerylegacy.gaiarelics;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.StatCollector;

import java.util.List;

public final class ItemGaiaEchoBlade extends ItemSword {
    public static final String TAG_ECHO = "DMLGaiaEcho";

    public ItemGaiaEchoBlade() {
        super(Item.ToolMaterial.EMERALD);
        setUnlocalizedName(DivineMachineryLegacy.MODID + ".gaia_echo_blade");
        setTextureName(DivineMachineryLegacy.MODID + ":gaiarelics/gaia_echo_blade");
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
        list.add("§b" + StatCollector.translateToLocalFormatted("tooltip.divinemachinerylegacy.gaia_echo.echo", echo));
        list.add("§7" + StatCollector.translateToLocalFormatted("tooltip.divinemachinerylegacy.gaia_echo.chance", echo));
        list.add("§8" + StatCollector.translateToLocal("tooltip.divinemachinerylegacy.gaia_echo.reset"));
    }
}
