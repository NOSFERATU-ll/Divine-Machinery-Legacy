package com.nosferatu.divinemachinerylegacy.item;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.item.Item;

/**
 * Simple 1.7.10 representation of Extra Reforked machine catalysts.
 * Proper artwork will replace the temporary vanilla icon later.
 */
public class ItemMachineCatalyst extends Item {

    public ItemMachineCatalyst(String key, String placeholderTexture) {
        setUnlocalizedName(DivineMachineryLegacy.MODID + "." + key);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setMaxStackSize(1);
        setTextureName(placeholderTexture);
    }
}
