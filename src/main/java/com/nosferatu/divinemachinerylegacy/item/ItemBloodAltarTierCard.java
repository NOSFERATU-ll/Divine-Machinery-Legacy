package com.nosferatu.divinemachinerylegacy.item;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.item.Item;

/** Blood Altar tier card backported from bmaddon. */
public class ItemBloodAltarTierCard extends Item {
    private final int tier;

    public ItemBloodAltarTierCard(int tier) {
        this.tier = tier;
        String key = "blood_altar_tier_card_" + tier;
        setUnlocalizedName(DivineMachineryLegacy.MODID + "." + key);
        setTextureName(DivineMachineryLegacy.MODID + ":bloodmagic/" + key);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setMaxStackSize(1);
    }

    public int getTier() {
        return tier;
    }
}
