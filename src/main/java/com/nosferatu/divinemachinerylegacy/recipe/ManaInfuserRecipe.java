package com.nosferatu.divinemachinerylegacy.recipe;

import net.minecraft.item.ItemStack;

/** Multi-input mana recipe used by the 1.7.10 replacement for MythicBotany's Mana Infuser. */
public final class ManaInfuserRecipe {
    private final ItemStack output;
    private final int manaCost;
    private final int minimumTier;
    private final ItemStack[] ingredients;

    public ManaInfuserRecipe(ItemStack output, int manaCost, int minimumTier, ItemStack... ingredients) {
        this.output = output.copy();
        this.manaCost = manaCost;
        this.minimumTier = minimumTier;
        this.ingredients = new ItemStack[ingredients.length];
        for (int i = 0; i < ingredients.length; i++) this.ingredients[i] = ingredients[i].copy();
    }

    public ItemStack getOutput() { return output.copy(); }
    public int getManaCost() { return manaCost; }
    public int getMinimumTier() { return minimumTier; }

    public boolean isIngredient(ItemStack stack) {
        if (stack == null) return false;
        for (ItemStack ingredient : ingredients) if (matches(ingredient, stack)) return true;
        return false;
    }

    public int getMaxCrafts(ItemStack[] inventory, int first, int last, int limit) {
        int crafts = Math.max(0, limit);
        for (ItemStack ingredient : ingredients) {
            int available = 0;
            for (int slot = first; slot <= last; slot++) {
                ItemStack stack = inventory[slot];
                if (matches(ingredient, stack)) available += stack.stackSize;
            }
            crafts = Math.min(crafts, available / Math.max(1, ingredient.stackSize));
            if (crafts <= 0) return 0;
        }
        return crafts;
    }

    public void consume(ItemStack[] inventory, int first, int last, int crafts) {
        for (ItemStack ingredient : ingredients) {
            int remaining = ingredient.stackSize * crafts;
            for (int slot = first; slot <= last && remaining > 0; slot++) {
                ItemStack stack = inventory[slot];
                if (!matches(ingredient, stack)) continue;
                int take = Math.min(remaining, stack.stackSize);
                stack.stackSize -= take;
                remaining -= take;
                if (stack.stackSize <= 0) inventory[slot] = null;
            }
        }
    }

    private static boolean matches(ItemStack required, ItemStack found) {
        return required != null && found != null
                && required.isItemEqual(found)
                && ItemStack.areItemStackTagsEqual(required, found);
    }
}
