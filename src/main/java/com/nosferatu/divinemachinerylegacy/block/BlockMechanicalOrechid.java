package com.nosferatu.divinemachinerylegacy.block;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalOrechid;
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
import vazkii.botania.common.block.ModBlocks;

import java.util.List;

public class BlockMechanicalOrechid extends BlockContainer {
    private static int renderId;
    @SideOnly(Side.CLIENT) private IIcon[] tierIcons;
    @SideOnly(Side.CLIENT) private IIcon flowerIcon;

    public BlockMechanicalOrechid() {
        super(Material.rock);
        setBlockName(DivineMachineryLegacy.MODID + ".mechanical_orechid");
        setHardness(4F);
        setResistance(10F);
        setLightOpacity(0);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
    }

    public static void setRenderId(int id) { renderId = id; }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileMechanicalOrechid();
    }

    @Override public int damageDropped(int meta) { return meta & 3; }
    @Override protected ItemStack createStackedBlock(int meta) { return new ItemStack(this, 1, meta & 3); }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return renderId; }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, net.minecraft.creativetab.CreativeTabs tab, List list) {
        for (int i = 0; i < 4; i++) list.add(new ItemStack(item, 1, i));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) { return getPlateIcon(meta); }

    @SideOnly(Side.CLIENT)
    public IIcon getPlateIcon(int meta) {
        int i = MachineTier.fromMeta(meta).ordinal();
        return tierIcons != null && tierIcons[i] != null ? tierIcons[i] : blockIcon;
    }

    @SideOnly(Side.CLIENT) public IIcon getSoilTopIcon() { return ModBlocks.enchantedSoil.getIcon(1, 0); }
    @SideOnly(Side.CLIENT) public IIcon getSoilSideIcon() { return ModBlocks.enchantedSoil.getIcon(2, 0); }
    @SideOnly(Side.CLIENT) public IIcon getFlowerIcon() { return flowerIcon; }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        tierIcons = new IIcon[4];
        tierIcons[0] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/malachite_dragonstone_block");
        tierIcons[1] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/saffron_dragonstone_block");
        tierIcons[2] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/shadow_dragonstone_block");
        tierIcons[3] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/crimson_dragonstone_block");
        flowerIcon = register.registerIcon("botania:orechid");
        blockIcon = tierIcons[0];
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(DivineMachineryLegacy.INSTANCE, DivineMachineryLegacy.GUI_ORECHID, world, x, y, z);
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
                EntityItem drop = new EntityItem(world, x + .5D, y + .5D, z + .5D, stack.copy());
                drop.motionX = world.rand.nextGaussian() * .05D;
                drop.motionY = world.rand.nextGaussian() * .05D + .2D;
                drop.motionZ = world.rand.nextGaussian() * .05D;
                world.spawnEntityInWorld(drop);
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
