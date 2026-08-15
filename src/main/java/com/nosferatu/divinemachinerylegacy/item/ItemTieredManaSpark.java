package com.nosferatu.divinemachinerylegacy.item;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.SparkTier;
import com.nosferatu.divinemachinerylegacy.entity.EntityTieredManaSpark;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IManaGivingItem;
import vazkii.botania.api.mana.spark.ISparkAttachable;

import java.util.List;

/** Reforked-style Spark item adapted to Botania 1.7.10. */
public class ItemTieredManaSpark extends Item implements IManaGivingItem {

    private final SparkTier tier;

    public ItemTieredManaSpark(SparkTier tier) {
        this.tier = tier;
        setUnlocalizedName(DivineMachineryLegacy.MODID + "." + tier.getKey() + "_spark");
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setMaxStackSize(64);
    }

    public SparkTier getTier() {
        return tier;
    }

    @Override
    public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/" + tier.getKey() + "_spark");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof ISparkAttachable)) return false;

        ISparkAttachable attachable = (ISparkAttachable) te;
        if (!attachable.canAttachSpark(stack) || attachable.getAttachedSpark() != null) return false;

        if (!world.isRemote) {
            EntityTieredManaSpark spark = new EntityTieredManaSpark(world);
            spark.setSparkTier(tier);
            spark.setPosition(x + 0.5D, y + 1.5D, z + 0.5D);
            world.spawnEntityInWorld(spark);
            attachable.attachSpark(spark);
            if (!player.capabilities.isCreativeMode) stack.stackSize--;
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, x, y, z);
        }
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        super.addInformation(stack, player, list, advanced);
        list.add(EnumChatFormatting.AQUA + StatCollector.translateToLocalFormatted(
                "tooltip.divinemachinerylegacy.spark_transfer", tier.getTransferRate()));
    }
}
