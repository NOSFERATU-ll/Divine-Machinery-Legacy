package com.nosferatu.divinemachinerylegacy.block;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.tile.TileJadedAmaranthus;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import vazkii.botania.common.block.ModBlocks;

public class BlockJadedAmaranthus extends BlockContainer {
    private static int renderId;
    @SideOnly(Side.CLIENT) private IIcon plateIcon;
    @SideOnly(Side.CLIENT) private IIcon flowerIcon;

    public BlockJadedAmaranthus() {
        super(Material.rock);
        setBlockName(DivineMachineryLegacy.MODID + ".jaded_amaranthus");
        setHardness(4F);
        setResistance(10F);
        setLightOpacity(0);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
    }

    public static void setRenderId(int id) { renderId = id; }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileJadedAmaranthus();
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return renderId; }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) { return plateIcon != null ? plateIcon : blockIcon; }

    @SideOnly(Side.CLIENT) public IIcon getPlateIcon() { return plateIcon != null ? plateIcon : blockIcon; }
    @SideOnly(Side.CLIENT) public IIcon getSoilTopIcon() { return ModBlocks.enchantedSoil.getIcon(1, 0); }
    @SideOnly(Side.CLIENT) public IIcon getSoilSideIcon() { return ModBlocks.enchantedSoil.getIcon(2, 0); }
    @SideOnly(Side.CLIENT) public IIcon getFlowerIcon() { return flowerIcon; }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        plateIcon = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/crystal_dragonstone_block");
        flowerIcon = register.registerIcon("botania:jadedAmaranthus");
        blockIcon = plateIcon;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(DivineMachineryLegacy.INSTANCE, DivineMachineryLegacy.GUI_JADED_AMARANTHUS,
                    world, x, y, z);
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
