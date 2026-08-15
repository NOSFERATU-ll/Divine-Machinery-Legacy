package com.nosferatu.divinemachinerylegacy.gaiarelics;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import vazkii.botania.common.entity.EntityDoppleganger;

import java.util.List;

public final class ItemGaiaBlade extends ItemSword {
    public static final String TAG_KILLS = "DMLGaiaKills";
    public static final String TAG_READY_TICK = "DMLGaiaBladeReady";

    public ItemGaiaBlade() {
        super(Item.ToolMaterial.EMERALD);
        setUnlocalizedName(DivineMachineryLegacy.MODID + ".gaia_blade");
        setTextureName("minecraft:diamond_sword");
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setMaxDamage(0);
    }

    public static int getKills(ItemStack stack) {
        return stack != null && stack.getTagCompound() != null ? Math.max(0, stack.getTagCompound().getInteger(TAG_KILLS)) : 0;
    }

    public static int getLevel(ItemStack stack) {
        return Math.min(10, getKills(stack) / 10 + 1);
    }

    public static void addKill(ItemStack stack) {
        tag(stack).setInteger(TAG_KILLS, getKills(stack) + 1);
    }

    public static double getBonusDropChance(ItemStack stack) {
        int level = getLevel(stack);
        return 0.01D + (level - 1) * (0.99D / 9D);
    }

    public static int getCooldownSeconds(ItemStack stack) {
        int level = getLevel(stack);
        return 50 - (level - 1) * 5;
    }

    private static NBTTagCompound tag(ItemStack stack) {
        if (stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) return stack;
        long now = world.getTotalWorldTime();
        long ready = tag(stack).getLong(TAG_READY_TICK);
        if (now < ready) return stack;

        AxisAlignedBB box = player.boundingBox.expand(24D, 12D, 24D);
        List list = world.getEntitiesWithinAABB(EntityDoppleganger.class, box);
        if (list.isEmpty()) return stack;

        Entity target = (Entity) list.get(0);
        float damage = 12F + getLevel(stack) * 2F;
        target.attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
        tag(stack).setLong(TAG_READY_TICK, now + getCooldownSeconds(stack) * 20L);
        world.playSoundAtEntity(target, "random.explode", 0.7F, 1.25F);
        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        int kills = getKills(stack);
        int level = getLevel(stack);
        list.add("§6Level: " + level + " / 10");
        list.add("§bGaia kills: " + kills);
        if (level < 10) list.add("§aKills until next level: " + (10 - kills % 10));
        list.add("§dBonus essence drop: " + Math.round(getBonusDropChance(stack) * 100D) + "%");
        long now = player == null || player.worldObj == null ? 0 : player.worldObj.getTotalWorldTime();
        long ready = stack.getTagCompound() == null ? 0 : stack.getTagCompound().getLong(TAG_READY_TICK);
        if (ready <= now) list.add("§aAbility: ready");
        else list.add("§cCooldown: " + ((ready - now + 19) / 20) + " sec.");
    }
}
