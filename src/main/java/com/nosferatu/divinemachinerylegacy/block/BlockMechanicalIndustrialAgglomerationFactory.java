package com.nosferatu.divinemachinerylegacy.block;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalIndustrialAgglomerationFactory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.ModFluffBlocks;

import java.util.List;

public class BlockMechanicalIndustrialAgglomerationFactory extends BlockContainer {
    private static int renderId;
    @SideOnly(Side.CLIENT) private IIcon[] tierIcons;

    public BlockMechanicalIndustrialAgglomerationFactory() {
        super(Material.rock);
        setBlockName(DivineMachineryLegacy.MODID + ".mechanical_industrial_agglomeration_factory");
        setHardness(4F);
        setResistance(10F);
        setLightOpacity(0);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
    }

    public static void setRenderId(int id) { renderId = id; }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileMechanicalIndustrialAgglomerationFactory();
    }

    @Override public int damageDropped(int meta) { return meta & 3; }
    @Override protected ItemStack createStackedBlock(int meta) { return new ItemStack(this, 1, meta & 3); }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, net.minecraft.creativetab.CreativeTabs tab, List list) {
        for (int i = 0; i < 4; i++) list.add(new ItemStack(item, 1, i));
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return renderId; }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return getFrameIcon(meta);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getFrameIcon(int meta) {
        int i = MachineTier.fromMeta(meta).ordinal();
        return tierIcons != null && tierIcons[i] != null ? tierIcons[i] : blockIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getInnerStoneIcon(int meta) {
        switch (MachineTier.fromMeta(meta)) {
            case SAFFRON:
                return ModBlocks.livingrock.getIcon(1, 0);
            case SHADOW:
                return ModFluffBlocks.biomeStoneA.getIcon(1, 7);
            case CRIMSON:
                return ModBlocks.shimmerrock.getIcon(1, 0);
            case MALACHITE:
            default:
                return ModFluffBlocks.biomeStoneA.getIcon(1, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getLapisIcon() {
        return Blocks.lapis_block.getIcon(1, 0);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getTerraTopIcon() {
        return ModBlocks.terraPlate.getIcon(1, 0);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getTerraSideIcon() {
        return ModBlocks.terraPlate.getIcon(2, 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        tierIcons = new IIcon[4];
        tierIcons[0] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/malachite_dragonstone_block");
        tierIcons[1] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/saffron_dragonstone_block");
        tierIcons[2] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/shadow_dragonstone_block");
        tierIcons[3] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/crimson_dragonstone_block");
        blockIcon = tierIcons[0];
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(DivineMachineryLegacy.INSTANCE,
                    DivineMachineryLegacy.GUI_INDUSTRIAL_AGGLOMERATION, world, x, y, z);
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
                EntityItem drop = new EntityItem(world, x + .5, y + .5, z + .5, stack.copy());
                drop.motionX = world.rand.nextGaussian() * .05;
                drop.motionY = world.rand.nextGaussian() * .05 + .2;
                drop.motionZ = world.rand.nextGaussian() * .05;
                world.spawnEntityInWorld(drop);
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
