package com.nosferatu.divinemachinerylegacy.item;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.item.Item;

/** Speed/parallel upgrade card used by the Blood Altar Assembler. */
public class ItemBloodMachineUpgrade extends Item {
    public enum Type {
        PARALLEL,
        SPEED
    }

    private final Type type;

    public ItemBloodMachineUpgrade(String key, Type type) {
        this.type = type;
        setUnlocalizedName(DivineMachineryLegacy.MODID + "." + key);
        setTextureName(DivineMachineryLegacy.MODID + ":bloodmagic/" + key);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setMaxStackSize(64);
    }

    public Type getType() {
        return type;
    }
}
