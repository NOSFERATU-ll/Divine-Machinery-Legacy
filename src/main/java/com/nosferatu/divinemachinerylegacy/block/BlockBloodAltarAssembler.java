package com.nosferatu.divinemachinerylegacy.block;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.tile.TileBloodAltarAssemblerNetworked;
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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockBloodAltarAssembler extends BlockContainer {
    private static int renderId = -1;

    @SideOnly(Side.CLIENT)
    private IIcon lightsIcon;

    public BlockBloodAltarAssembler() {
        super(Material.iron);
        setBlockName(DivineMachineryLegacy.MODID + ".blood_altar_assembler");
        setBlockTextureName(DivineMachineryLegacy.MODID + ":bloodmagic/blood_altar_assembler");
        setHardness(4.0F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 2);
        setStepSound(soundTypeMetal);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
    }

    public static void setRenderId(int id) {
        renderId = id;
    }

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        super.registerBlockIcons(register);
        lightsIcon = register.registerIcon(
                DivineMachineryLegacy.MODID + ":bloodmagic/blood_altar_assembler_lights");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getBaseIcon() {
        return blockIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getLightsIcon() {
        return lightsIcon;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileBloodAltarAssemblerNetworked();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        // AE2's modern machine deliberately passes alternate-use (sneak) clicks
        // through so wrenches/memory-card style interactions can own the click.
        if (player.isSneaking()) return false;

        if (!world.isRemote) {
            player.openGui(DivineMachineryLegacy.INSTANCE, DivineMachineryLegacy.GUI_BLOOD_ALTAR_ASSEMBLER,
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
                dropStack(world, x, y, z, stack.copy());
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    private void dropStack(World world, int x, int y, int z, ItemStack stack) {
        while (stack.stackSize > 0) {
            int amount = Math.min(stack.stackSize, world.rand.nextInt(21) + 10);
            stack.stackSize -= amount;
            ItemStack drop = new ItemStack(stack.getItem(), amount, stack.getItemDamage());
            if (stack.hasTagCompound()) {
                drop.setTagCompound((NBTTagCompound) stack.getTagCompound().copy());
            }
            EntityItem entity = new EntityItem(world,
                    x + 0.15D + world.rand.nextDouble() * 0.7D,
                    y + 0.15D + world.rand.nextDouble() * 0.7D,
                    z + 0.15D + world.rand.nextDouble() * 0.7D,
                    drop);
            entity.motionX = world.rand.nextGaussian() * 0.05D;
            entity.motionY = world.rand.nextGaussian() * 0.05D + 0.2D;
            entity.motionZ = world.rand.nextGaussian() * 0.05D;
            world.spawnEntityInWorld(entity);
        }
    }
}
