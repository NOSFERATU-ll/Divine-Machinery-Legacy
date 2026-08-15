package com.nosferatu.divinemachinerylegacy.recipe;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import net.minecraft.item.ItemStack;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DML-native replacement for the modern MythicBotany Mana Infuser recipe layer.
 * The material recipes preserve Extra Reforked's ingredients and mana costs;
 * they live here because Botania 1.7.10 has no extensible Terra Plate registry.
 */
public final class ManaInfuserRecipes {
    private static final List<ManaInfuserRecipe> RECIPES = new ArrayList<ManaInfuserRecipe>();
    private static boolean defaultsRegistered;

    private ManaInfuserRecipes() { }

    public static void registerDefaults() {
        if (defaultsRegistered) return;
        defaultsRegistered = true;

        // Base machine bootstraps the first two tiers; each later machine tier
        // unlocks the material needed to build the following tier.
        add(new ItemStack(DivineMachineryLegacy.materialIngots[0]), 750000, MachineTier.MALACHITE,
                manaResource(4, 1), manaResource(5, 1), manaResource(7, 1));
        add(new ItemStack(DivineMachineryLegacy.materialIngots[1]), 1000000, MachineTier.MALACHITE,
                new ItemStack(DivineMachineryLegacy.materialIngots[0]), manaResource(14, 1),
                new ItemStack(ModBlocks.storage, 1, 1));
        add(new ItemStack(DivineMachineryLegacy.materialIngots[2]), 1500000, MachineTier.SAFFRON,
                new ItemStack(DivineMachineryLegacy.materialIngots[1]), manaResource(14, 2),
                new ItemStack(DivineMachineryLegacy.materialIngotBlocks[0]));
        add(new ItemStack(DivineMachineryLegacy.materialIngots[3]), 2000000, MachineTier.SHADOW,
                new ItemStack(DivineMachineryLegacy.materialIngots[2]), manaResource(14, 4),
                new ItemStack(DivineMachineryLegacy.materialIngotBlocks[1]));
        add(new ItemStack(DivineMachineryLegacy.materialIngots[5]), 2500000, MachineTier.CRIMSON,
                new ItemStack(DivineMachineryLegacy.materialIngots[3]), manaResource(14, 4),
                new ItemStack(DivineMachineryLegacy.materialIngotBlocks[3]));
        add(new ItemStack(DivineMachineryLegacy.materialIngots[6]), 5000000, MachineTier.CRIMSON,
                new ItemStack(DivineMachineryLegacy.materialIngots[5]), manaResource(14, 4),
                new ItemStack(DivineMachineryLegacy.materialIngotBlocks[5]));
    }

    private static void add(ItemStack output, int mana, MachineTier minimumTier, ItemStack... ingredients) {
        RECIPES.add(new ManaInfuserRecipe(output, mana, minimumTier.ordinal(), ingredients));
    }

    private static ItemStack manaResource(int meta, int count) {
        return new ItemStack(ModItems.manaResource, count, meta);
    }

    public static List<ManaInfuserRecipe> all() {
        return Collections.unmodifiableList(RECIPES);
    }

    public static ManaInfuserRecipe get(int index) {
        return index >= 0 && index < RECIPES.size() ? RECIPES.get(index) : null;
    }

    public static int indexOf(ManaInfuserRecipe recipe) {
        return RECIPES.indexOf(recipe);
    }

    public static boolean isIngredient(ItemStack stack) {
        for (ManaInfuserRecipe recipe : RECIPES) if (recipe.isIngredient(stack)) return true;
        return false;
    }
}
