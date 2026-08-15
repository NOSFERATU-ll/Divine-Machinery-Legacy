package com.nosferatu.divinemachinerylegacy;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

/** Dedicated creative/NEI category for all ported machinery and upgrades. */
public class CreativeTabDivineMachinery extends CreativeTabs {

    public CreativeTabDivineMachinery() {
        super(DivineMachineryLegacy.MODID);
    }

    @Override
    public Item getTabIconItem() {
        if (DivineMachineryLegacy.mechanicalRunicAltar != null) {
            return Item.getItemFromBlock(DivineMachineryLegacy.mechanicalRunicAltar);
        }
        return Items.nether_star;
    }
}
