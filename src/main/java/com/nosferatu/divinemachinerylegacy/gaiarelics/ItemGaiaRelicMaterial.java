package com.nosferatu.divinemachinerylegacy.gaiarelics;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.item.Item;

/** Six realm materials used by the Gaia relic progression. */
public final class ItemGaiaRelicMaterial extends Item {
    public enum Kind {
        YGGDRASIL_ESSENCE, MUSPEL_ESSENCE, NIFL_ESSENCE,
        YGGDRASIL_INGOT, MUSPEL_INGOT, NIFL_INGOT
    }

    private final Kind kind;

    public ItemGaiaRelicMaterial(String key, Kind kind) {
        this.kind = kind;
        setUnlocalizedName(DivineMachineryLegacy.MODID + "." + key);
        setTextureName("minecraft:nether_star"); // fallback; custom 3D renderer owns normal presentation
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setMaxStackSize(64);
    }

    public Kind getKind() {
        return kind;
    }
}
