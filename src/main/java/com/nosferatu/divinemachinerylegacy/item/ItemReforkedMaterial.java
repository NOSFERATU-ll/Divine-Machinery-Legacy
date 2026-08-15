package com.nosferatu.divinemachinerylegacy.item;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.item.Item;

/** Simple Reforked material item backed by the permitted original artwork. */
public class ItemReforkedMaterial extends Item {

    public ItemReforkedMaterial(String key) {
        setUnlocalizedName(DivineMachineryLegacy.MODID + "." + key);
        setTextureName(DivineMachineryLegacy.MODID + ":reforked/" + key);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
    }
}
