package com.nosferatu.divinemachinerylegacy.block;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalManaPool;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class BlockMechanicalManaPool extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon[] tierIcons;

    public BlockMechanicalManaPool() {
        super(Material.rock);
        setBlockName(DivineMachineryLegacy.MODID + ".mechanical_mana_pool");
        setHardness(4.0F);
        setResistance(10.0F);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileMechanicalManaPool();
    }

    @Override
    public int damageDropped(int meta) {
        return meta & 3;
    }

    @Override
    protected ItemStack createStackedBlock(int meta) {
        return new ItemStack(this, 1, meta & 3);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, net.minecraft.creativetab.CreativeTabs tab, List list) {
        for (int meta = 0; meta < 4; meta++) list.add(new ItemStack(item, 1, meta));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        int index = MachineTier.fromMeta(meta).ordinal();
        if (tierIcons != null && index >= 0 && index < tierIcons.length && tierIcons[index] != null) return tierIcons[index];
        return blockIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        tierIcons = new IIcon[4];
        tierIcons[MachineTier.MALACHITE.ordinal()] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/malachite_dragonstone_block");
        tierIcons[MachineTier.SAFFRON.ordinal()] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/saffron_dragonstone_block");
        tierIcons[MachineTier.SHADOW.ordinal()] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/shadow_dragonstone_block");
        tierIcons[MachineTier.CRIMSON.ordinal()] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/crimson_dragonstone_block");
        blockIcon = tierIcons[0];
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(DivineMachineryLegacy.INSTANCE, DivineMachineryLegacy.GUI_MANA_POOL, world, x, y, z);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof IInventory) {
            IInventory inv = (IInventory) te;
            for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
                ItemStack stack = inv.getStackInSlot(slot);
                if (stack == null) continue;
                EntityItem drop = new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, stack.copy());
                drop.motionX = world.rand.nextGaussian() * 0.05D;
                drop.motionY = world.rand.nextGaussian() * 0.05D + 0.2D;
                drop.motionZ = world.rand.nextGaussian() * 0.05D;
                world.spawnEntityInWorld(drop);
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
