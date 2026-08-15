package com.nosferatu.divinemachinerylegacy.block;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
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

public class BlockMechanicalRunicAltar extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon malachiteIcon;
    @SideOnly(Side.CLIENT)
    private IIcon saffronIcon;
    @SideOnly(Side.CLIENT)
    private IIcon shadowIcon;
    @SideOnly(Side.CLIENT)
    private IIcon crimsonIcon;

    public BlockMechanicalRunicAltar() {
        super(Material.rock);
        setBlockName(DivineMachineryLegacy.MODID + ".mechanical_runic_altar");
        setHardness(4.0F);
        setResistance(10.0F);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileMechanicalRunicAltar();
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
        for (int meta = 0; meta < 4; meta++) {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        switch (MachineTier.fromMeta(meta)) {
            case SAFFRON:
                return saffronIcon;
            case SHADOW:
                return shadowIcon;
            case CRIMSON:
                return crimsonIcon;
            case MALACHITE:
            default:
                return malachiteIcon;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        // Extra Reforked's machine models are modern JSON models rather than
        // single cube textures. For the native 1.7.10 block representation we
        // use the permitted tier Dragonstone artwork until the dedicated TESR
        // model pass is complete.
        malachiteIcon = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/malachite_dragonstone_block");
        saffronIcon = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/saffron_dragonstone_block");
        shadowIcon = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/shadow_dragonstone_block");
        crimsonIcon = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/crimson_dragonstone_block");
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(DivineMachineryLegacy.INSTANCE, DivineMachineryLegacy.GUI_RUNIC_ALTAR, world, x, y, z);
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

                float ox = world.rand.nextFloat() * 0.8F + 0.1F;
                float oy = world.rand.nextFloat() * 0.8F + 0.1F;
                float oz = world.rand.nextFloat() * 0.8F + 0.1F;

                while (stack.stackSize > 0) {
                    int amount = world.rand.nextInt(21) + 10;
                    if (amount > stack.stackSize) amount = stack.stackSize;
                    stack.stackSize -= amount;

                    ItemStack drop = new ItemStack(stack.getItem(), amount, stack.getItemDamage());
                    if (stack.hasTagCompound()) drop.setTagCompound((net.minecraft.nbt.NBTTagCompound) stack.getTagCompound().copy());
                    EntityItem entity = new EntityItem(world, x + ox, y + oy, z + oz, drop);
                    entity.motionX = world.rand.nextGaussian() * 0.05D;
                    entity.motionY = world.rand.nextGaussian() * 0.05D + 0.2D;
                    entity.motionZ = world.rand.nextGaussian() * 0.05D;
                    world.spawnEntityInWorld(entity);
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
