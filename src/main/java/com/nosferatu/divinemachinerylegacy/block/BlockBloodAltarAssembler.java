package com.nosferatu.divinemachinerylegacy.block;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.tile.TileBloodAltarAssemblerExtended;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockBloodAltarAssembler extends BlockContainer {
    public BlockBloodAltarAssembler() {
        super(Material.iron);
        setBlockName(DivineMachineryLegacy.MODID + ".blood_altar_assembler");
        setBlockTextureName(DivineMachineryLegacy.MODID + ":bloodmagic/blood_altar_assembler");
        setHardness(4.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileBloodAltarAssemblerExtended();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
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
