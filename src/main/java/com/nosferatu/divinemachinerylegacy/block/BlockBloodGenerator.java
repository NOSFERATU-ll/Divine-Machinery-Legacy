package com.nosferatu.divinemachinerylegacy.block;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicConfigSync;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicContent;
import com.nosferatu.divinemachinerylegacy.tile.TileBloodGenerator;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockBloodGenerator extends BlockContainer {
    public BlockBloodGenerator() {
        super(Material.iron);
        setBlockName(DivineMachineryLegacy.MODID + ".blood_generator");
        setBlockTextureName(DivineMachineryLegacy.MODID + ":bloodmagic/blood_generator");
        setHardness(4.0F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 2);
        setStepSound(soundTypeMetal);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileBloodGenerator();
    }

    @Override
    public int getRenderType() {
        return -1;
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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            // bmaddon refreshes its common generator config immediately before
            // opening this screen in addition to the normal login/dimension sync.
            BloodMagicConfigSync.sendToPlayer(player);
            player.openGui(DivineMachineryLegacy.INSTANCE, BloodMagicContent.GUI_BLOOD_GENERATOR,
                    world, x, y, z);
        }
        return true;
    }
}
