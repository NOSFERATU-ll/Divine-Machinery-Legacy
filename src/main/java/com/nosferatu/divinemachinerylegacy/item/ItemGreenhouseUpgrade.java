package com.nosferatu.divinemachinerylegacy.item;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.item.Item;

/** Fixed-category Greenhouse upgrade matching Extra Reforked's upgrade slots. */
public class ItemGreenhouseUpgrade extends Item {
    private final int upgradeSlot;
    private final int value;

    public ItemGreenhouseUpgrade(String key, int upgradeSlot, int value) {
        this.upgradeSlot = upgradeSlot;
        this.value = value;
        setUnlocalizedName(DivineMachineryLegacy.MODID + "." + key);
        setTextureName(DivineMachineryLegacy.MODID + ":reforked/" + key);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setMaxStackSize(1);
    }

    public int getUpgradeSlot() { return upgradeSlot; }
    public int getValue() { return value; }
}
