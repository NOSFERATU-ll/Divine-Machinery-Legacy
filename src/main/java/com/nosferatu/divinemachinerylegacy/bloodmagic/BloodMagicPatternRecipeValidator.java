package com.nosferatu.divinemachinerylegacy.bloodmagic;

import WayofTime.alchemicalWizardry.api.alchemy.AlchemyRecipe;
import WayofTime.alchemicalWizardry.api.alchemy.AlchemyRecipeRegistry;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipe;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipeRegistry;
import net.minecraft.item.ItemStack;

import java.util.List;

/** Resolves an encoded 1.7.10 Blood Pattern back to a currently registered recipe. */
public final class BloodMagicPatternRecipeValidator {
    private BloodMagicPatternRecipeValidator() { }

    public static boolean recipeStillExists(ItemStack pattern) {
        if (!BloodMagicPatternData.isEncoded(pattern)) return false;

        BloodMagicPatternKind kind = BloodMagicPatternData.getKind(pattern);
        List<ItemStack> inputs = BloodMagicPatternData.getInputs(pattern);
        ItemStack output = BloodMagicPatternData.getOutput(pattern);
        int storedTier = BloodMagicPatternData.getRequiredTier(pattern);
        if (kind == null || inputs.isEmpty() || output == null) return false;

        if (kind == BloodMagicPatternKind.BLOOD_ALTAR) {
            if (inputs.size() != 1) return false;
            ItemStack input = inputs.get(0);
            for (AltarRecipe recipe : AltarRecipeRegistry.altarRecipes) {
                if (recipe == null || recipe.getCanBeFilled() || recipe.getResult() == null) continue;
                if (recipe.getMinTier() != storedTier) continue;
                if (!recipe.doesRequiredItemMatch(input, Integer.MAX_VALUE)) continue;
                if (sameStackAndCount(recipe.getResult(), output)) return true;
            }
            return false;
        }

        if (kind == BloodMagicPatternKind.ALCHEMY_TABLE) {
            if (inputs.size() > 5) return false;
            ItemStack[] slots = new ItemStack[5];
            for (int i = 0; i < inputs.size(); i++) {
                slots[i] = inputs.get(i).copy();
                slots[i].stackSize = 1;
            }
            for (AlchemyRecipe recipe : AlchemyRecipeRegistry.recipes) {
                if (recipe == null || recipe.getResult() == null) continue;
                if (recipe.getOrbLevel() != storedTier) continue;
                if (ingredientCount(recipe.getRecipe()) != inputs.size()) continue;
                if (recipe.doesRecipeMatch(slots, storedTier)
                        && sameStackAndCount(recipe.getResult(), output)) return true;
            }
        }

        return false;
    }

    private static int ingredientCount(ItemStack[] recipe) {
        int count = 0;
        if (recipe != null) for (ItemStack stack : recipe) if (stack != null) count++;
        return count;
    }

    private static boolean sameStackAndCount(ItemStack a, ItemStack b) {
        return a != null && b != null && a.stackSize == b.stackSize
                && a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }
}
