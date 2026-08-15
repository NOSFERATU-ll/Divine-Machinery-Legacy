package com.nosferatu.divinemachinerylegacy.botania;

import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.RecipeRuneAltar;
import vazkii.botania.common.block.ModBlocks;

import java.util.List;

/** Shared native-1.7.10 interaction helpers for the Mechanical Runic Altar. */
public final class RunicAltarItemHelper {

    private RunicAltarItemHelper() {
    }

    public static boolean isLivingrock(ItemStack stack) {
        return stack != null
                && stack.getItem() == Item.getItemFromBlock(ModBlocks.livingrock)
                && stack.getItemDamage() == 0;
    }

    /**
     * Only real registered Botania Rune Altar ingredients are accepted here.
     * This automatically includes MineTweaker/ModTweaker recipes because they
     * are added to BotaniaAPI.runeAltarRecipes on 1.7.10.
     */
    public static boolean isRuneIngredient(ItemStack stack) {
        if (stack == null || stack.stackSize <= 0) return false;

        for (RecipeRuneAltar recipe : BotaniaAPI.runeAltarRecipes) {
            for (Object requirement : recipe.getInputs()) {
                if (matchesRequirement(requirement, stack)) return true;
            }
        }
        return false;
    }

    public static boolean canInsertAsAltarItem(ItemStack stack) {
        return isLivingrock(stack) || isRuneIngredient(stack);
    }

    /**
     * Inserts up to maxAmount from source into the machine and returns the
     * number actually accepted. Livingrock is routed to its dedicated slots;
     * recipe ingredients go to the 4x4 input area.
     */
    public static int insert(TileMechanicalRunicAltar tile, ItemStack source, int maxAmount) {
        if (tile == null || source == null || source.stackSize <= 0 || maxAmount <= 0) return 0;

        int amount = Math.min(source.stackSize, maxAmount);
        if (isLivingrock(source)) {
            int last = TileMechanicalRunicAltar.SLOT_LIVINGROCK_START + tile.getTier().getLivingrockSlots() - 1;
            return insertIntoRange(tile, source, amount,
                    TileMechanicalRunicAltar.SLOT_LIVINGROCK_START, last);
        }

        if (!isRuneIngredient(source)) return 0;
        return insertIntoRange(tile, source, amount,
                TileMechanicalRunicAltar.SLOT_INPUT_START,
                TileMechanicalRunicAltar.SLOT_INPUT_END);
    }

    private static int insertIntoRange(TileMechanicalRunicAltar tile, ItemStack source,
                                       int amount, int first, int last) {
        if (first > last) return 0;
        int left = amount;

        // Merge first so repeated right-clicks / dropped stacks stay compact.
        for (int slot = first; slot <= last && left > 0; slot++) {
            ItemStack existing = tile.getStackInSlot(slot);
            if (existing == null || !canStacksMerge(existing, source)) continue;

            int limit = Math.min(existing.getMaxStackSize(), tile.getInventoryStackLimit());
            int room = limit - existing.stackSize;
            if (room <= 0) continue;

            int moved = Math.min(room, left);
            ItemStack replacement = existing.copy();
            replacement.stackSize += moved;
            tile.setInventorySlotContents(slot, replacement);
            left -= moved;
        }

        for (int slot = first; slot <= last && left > 0; slot++) {
            if (tile.getStackInSlot(slot) != null) continue;

            ItemStack placed = source.copy();
            placed.stackSize = Math.min(left,
                    Math.min(placed.getMaxStackSize(), tile.getInventoryStackLimit()));
            tile.setInventorySlotContents(slot, placed);
            left -= placed.stackSize;
        }

        return amount - left;
    }

    private static boolean matchesRequirement(Object requirement, ItemStack candidate) {
        if (requirement instanceof String) {
            List<ItemStack> ores = OreDictionary.getOres((String) requirement);
            for (ItemStack ore : ores) {
                if (OreDictionary.itemMatches(ore, candidate, false)) return true;
            }
            return false;
        }

        if (requirement instanceof ItemStack) {
            ItemStack wanted = (ItemStack) requirement;
            if (wanted.getItem() != candidate.getItem()) return false;
            int meta = wanted.getItemDamage();
            return meta == OreDictionary.WILDCARD_VALUE || meta == candidate.getItemDamage();
        }

        return false;
    }

    private static boolean canStacksMerge(ItemStack a, ItemStack b) {
        return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }
}
