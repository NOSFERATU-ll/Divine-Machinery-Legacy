package com.nosferatu.divinemachinerylegacy.block;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import com.nosferatu.divinemachinerylegacy.botania.RunicAltarItemHelper;
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

    private static int renderId;

    @SideOnly(Side.CLIENT)
    private IIcon[] tierIcons;
    @SideOnly(Side.CLIENT)
    private IIcon runeBottomIcon;
    @SideOnly(Side.CLIENT)
    private IIcon runeTopIcon;
    @SideOnly(Side.CLIENT)
    private IIcon runeSideIcon;

    public BlockMechanicalRunicAltar() {
        super(Material.rock);
        setBlockName(DivineMachineryLegacy.MODID + ".mechanical_runic_altar");
        setHardness(4.0F);
        setResistance(10.0F);
        setLightOpacity(0);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
    }

    public static void setRenderId(int id) {
        renderId = id;
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
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return renderId;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return getFrameIcon(meta);
    }

    @SideOnly(Side.CLIENT)
    public IIcon getFrameIcon(int meta) {
        int index = MachineTier.fromMeta(meta).ordinal();
        if (tierIcons != null && index >= 0 && index < tierIcons.length && tierIcons[index] != null) {
            return tierIcons[index];
        }
        return blockIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getRuneBottomIcon() {
        return runeBottomIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getRuneTopIcon() {
        return runeTopIcon;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getRuneSideIcon() {
        return runeSideIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        tierIcons = new IIcon[4];
        tierIcons[MachineTier.MALACHITE.ordinal()] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/malachite_dragonstone_block");
        tierIcons[MachineTier.SAFFRON.ordinal()] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/saffron_dragonstone_block");
        tierIcons[MachineTier.SHADOW.ordinal()] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/shadow_dragonstone_block");
        tierIcons[MachineTier.CRIMSON.ordinal()] = register.registerIcon(DivineMachineryLegacy.MODID + ":reforked/crimson_dragonstone_block");
        blockIcon = tierIcons[MachineTier.MALACHITE.ordinal()];

        // These are the three native 1.7.10 Botania Runic Altar textures. The
        // Reforked model uses the equivalent bottom/top/side textures inside
        // its tier-coloured frame.
        runeBottomIcon = register.registerIcon("Botania:runeAltar0");
        runeTopIcon = register.registerIcon("Botania:runeAltar1");
        runeSideIcon = register.registerIcon("Botania:runeAltar2");
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileMechanicalRunicAltar)) return true;

        TileMechanicalRunicAltar altar = (TileMechanicalRunicAltar) te;
        ItemStack held = player.getCurrentEquippedItem();

        // Preserve the familiar Botania altar interaction: right-click a valid
        // rune ingredient or Livingrock onto the machine. Sneak-right-click (or
        // an empty hand) opens the full automation GUI instead.
        if (!player.isSneaking() && held != null && RunicAltarItemHelper.canInsertAsAltarItem(held)) {
            if (!world.isRemote) {
                int moved = RunicAltarItemHelper.insert(altar, held, 1);
                if (moved > 0) {
                    if (!player.capabilities.isCreativeMode) {
                        held.stackSize -= moved;
                        if (held.stackSize <= 0) {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                        }
                    }
                    world.playSoundEffect(x + 0.5D, y + 0.9D, z + 0.5D,
                            "random.pop", 0.2F, 1.3F + world.rand.nextFloat() * 0.2F);
                    player.inventory.markDirty();
                }
            }
            return true;
        }

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
                    if (stack.hasTagCompound()) {
                        drop.setTagCompound((net.minecraft.nbt.NBTTagCompound) stack.getTagCompound().copy());
                    }
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
