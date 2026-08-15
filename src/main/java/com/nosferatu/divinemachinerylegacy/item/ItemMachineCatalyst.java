package com.nosferatu.divinemachinerylegacy.item;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.item.Item;

/**
 * 1.7.10 representation of Extra Reforked machine catalysts.
 * Artwork is used with explicit upstream permission; see docs/.
 */
public class ItemMachineCatalyst extends Item {

    public ItemMachineCatalyst(String key, String textureKey) {
        setUnlocalizedName(DivineMachineryLegacy.MODID + "." + key);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setMaxStackSize(1);
        setTextureName(DivineMachineryLegacy.MODID + ":reforked/" + textureKey);
    }
}
