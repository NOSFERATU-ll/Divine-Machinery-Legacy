package com.nosferatu.divinemachinerylegacy.recipe;

import appeng.api.AEApi;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicContent;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternData;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternEncodingHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

/**
 * 1.7.10 encoding bridge for Blood Patterns.
 *
 * AE2 rv3 hard-codes its Pattern Terminal's blank slot to the vanilla AE2
 * blank pattern, so a custom blank cannot be encoded there without a coremod.
 * This recipe copies a valid AE2 processing pattern into one blank Blood
 * Pattern. BloodPatternCraftingHandler returns the source AE2 pattern to the
 * player, making this a non-destructive encoder rather than a conversion cost.
 */
public final class RecipeEncodeBloodPattern implements IRecipe {
    @Override
    public boolean matches(InventoryCrafting inventory, World world) {
        Pair pair = findPair(inventory);
        return pair != null && BloodMagicPatternEncodingHelper.tryEncode(pair.blank, pair.source, world) != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inventory) {
        Pair pair = findPair(inventory);
        if (pair == null) return null;
        return BloodMagicPatternEncodingHelper.tryEncode(pair.blank, pair.source, null);
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return BloodMagicContent.bloodAltarPattern == null
                ? null : new ItemStack(BloodMagicContent.bloodAltarPattern);
    }

    private Pair findPair(InventoryCrafting inventory) {
        ItemStack blank = null;
        ItemStack source = null;

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack == null) continue;

            if (BloodMagicContent.bloodAltarPattern != null
                    && stack.getItem() == BloodMagicContent.bloodAltarPattern
                    && !BloodMagicPatternData.isEncoded(stack)) {
                if (blank != null) return null;
                blank = stack;
                continue;
            }

            if (AEApi.instance().definitions().items().encodedPattern().isSameAs(stack)) {
                if (source != null) return null;
                source = stack;
                continue;
            }

            return null;
        }

        return blank != null && source != null ? new Pair(blank, source) : null;
    }

    private static final class Pair {
        final ItemStack blank;
        final ItemStack source;

        Pair(ItemStack blank, ItemStack source) {
            this.blank = blank;
            this.source = source;
        }
    }
}
