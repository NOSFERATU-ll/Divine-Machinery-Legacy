package com.nosferatu.divinemachinerylegacy.gaiarelics;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.List;

public final class ItemValkyrieFeather extends Item {
    public static final String TAG_FLIGHT_END = "DMLValkyrieFlightEnd";
    public static final String TAG_COOLDOWN_END = "DMLValkyrieCooldownEnd";

    public ItemValkyrieFeather() {
        setUnlocalizedName(DivineMachineryLegacy.MODID + ".valkyrie_feather");
        setTextureName("minecraft:feather");
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setMaxStackSize(1);
    }

    private static NBTTagCompound tag(ItemStack stack) {
        if (stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (world.isRemote) return stack;
        long now = world.getTotalWorldTime();
        NBTTagCompound nbt = tag(stack);
        if (now < nbt.getLong(TAG_COOLDOWN_END) || now < nbt.getLong(TAG_FLIGHT_END)) return stack;
        nbt.setLong(TAG_FLIGHT_END, now + 30L * 20L);
        player.capabilities.allowFlying = true;
        if (player instanceof EntityPlayerMP) ((EntityPlayerMP) player).sendPlayerAbilities();
        world.playSoundAtEntity(player, "random.orb", 0.8F, 1.5F);
        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        long now = player == null || player.worldObj == null ? 0 : player.worldObj.getTotalWorldTime();
        NBTTagCompound nbt = stack.getTagCompound();
        long flight = nbt == null ? 0 : nbt.getLong(TAG_FLIGHT_END);
        long cooldown = nbt == null ? 0 : nbt.getLong(TAG_COOLDOWN_END);
        list.add("§bRight click: 30 sec. flight");
        if (flight > now) list.add("§aFlight: " + ((flight - now + 19) / 20) + " sec.");
        else if (cooldown > now) list.add("§cCooldown: " + ((cooldown - now + 19) / 20) + " sec.");
        else list.add("§aReady");
    }
}
