package com.nosferatu.divinemachinerylegacy.item;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

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

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        lines.add(EnumChatFormatting.DARK_RED + StatCollector.translateToLocalFormatted(
                "tooltip.divinemachinerylegacy.blood_altar_tier_card.tier", tier));
        lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal(
                "tooltip.divinemachinerylegacy.blood_altar_tier_card.description"));
    }
}
