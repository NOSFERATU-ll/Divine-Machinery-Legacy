package com.nosferatu.divinemachinerylegacy.block;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/** Storage/Dragonstone block using the permitted Extra Reforked texture. */
public class BlockReforkedMaterial extends Block {

    public BlockReforkedMaterial(String key, boolean metal) {
        super(metal ? Material.iron : Material.rock);
        setBlockName(DivineMachineryLegacy.MODID + "." + key);
        setBlockTextureName(DivineMachineryLegacy.MODID + ":reforked/" + key);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setHardness(metal ? 5.0F : 4.0F);
        setResistance(10.0F);
        setStepSound(metal ? soundTypeMetal : soundTypeStone);
    }
}
