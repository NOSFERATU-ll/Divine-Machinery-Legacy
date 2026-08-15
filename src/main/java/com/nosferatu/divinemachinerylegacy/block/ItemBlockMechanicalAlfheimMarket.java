package com.nosferatu.divinemachinerylegacy.block;

import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockMechanicalAlfheimMarket extends ItemBlock {
    public ItemBlockMechanicalAlfheimMarket(Block block) {
        super(block);
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage & 3;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "tile.divinemachinerylegacy.mechanical_alfheim_market."
                + MachineTier.fromMeta(stack.getItemDamage()).getKey();
    }
}
