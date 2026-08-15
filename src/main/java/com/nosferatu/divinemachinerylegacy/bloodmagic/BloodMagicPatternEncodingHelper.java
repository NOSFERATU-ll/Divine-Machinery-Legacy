package com.nosferatu.divinemachinerylegacy.bloodmagic;

import WayofTime.alchemicalWizardry.api.alchemy.AlchemyRecipe;
import WayofTime.alchemicalWizardry.api.alchemy.AlchemyRecipeRegistry;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipe;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipeRegistry;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import com.nosferatu.divinemachinerylegacy.config.BloodMagicAddonConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** Converts an ordinary AE2 rv3 processing pattern into a Blood Pattern. */
public final class BloodMagicPatternEncodingHelper {
    private static final int LEGACY_ALCHEMY_PROGRESS_TICKS = 100;

    private BloodMagicPatternEncodingHelper() { }

    public static ItemStack tryEncode(ItemStack blankBloodPattern, ItemStack sourcePattern, World world) {
        if (blankBloodPattern == null || sourcePattern == null) return null;
        if (!(sourcePattern.getItem() instanceof ICraftingPatternItem)) return null;

        ICraftingPatternDetails details;
        try {
            details = ((ICraftingPatternItem) sourcePattern.getItem()).getPatternForItem(sourcePattern, world);
        } catch (Throwable ignored) {
            return null;
        }
        if (details == null || details.isCraftable()) return null;

        List<ItemStack> inputs = expandInputs(details.getCondensedInputs());
        ItemStack output = getSingleOutput(details.getCondensedOutputs());
        if (inputs.isEmpty() || output == null) return null;

        ItemStack encoded = tryBloodAltar(blankBloodPattern, inputs, output);
        if (encoded != null) return encoded;
        return tryAlchemyTable(blankBloodPattern, inputs, output);
    }

    private static ItemStack tryBloodAltar(ItemStack blank, List<ItemStack> inputs, ItemStack expectedOutput) {
        if (inputs.size() != 1) return null;
        ItemStack input = inputs.get(0);

        for (AltarRecipe recipe : AltarRecipeRegistry.altarRecipes) {
            if (recipe == null || recipe.getCanBeFilled() || recipe.getResult() == null) continue;
            if (!recipe.doesRequiredItemMatch(input, Integer.MAX_VALUE)) continue;
            if (!sameStackAndCount(recipe.getResult(), expectedOutput)) continue;

            ItemStack result = blank.copy();
            result.stackSize = 1;
            List<ItemStack> encodedInputs = new ArrayList<ItemStack>();
            ItemStack oneInput = input.copy();
            oneInput.stackSize = 1;
            encodedInputs.add(oneInput);
            BloodMagicPatternData.encode(result, BloodMagicPatternKind.BLOOD_ALTAR,
                    encodedInputs, recipe.getResult(),
                    scaledLifeCost(recipe.getLiquidRequired()), recipe.getMinTier(),
                    BloodMagicAddonConfig.bloodAltarAssemblerBaseCraftTimeTicks);
            return result;
        }
        return null;
    }

    private static ItemStack tryAlchemyTable(ItemStack blank, List<ItemStack> inputs, ItemStack expectedOutput) {
        if (inputs.isEmpty() || inputs.size() > 5) return null;

        ItemStack[] slots = new ItemStack[5];
        for (int i = 0; i < inputs.size(); i++) {
            slots[i] = inputs.get(i).copy();
            slots[i].stackSize = 1;
        }

        for (AlchemyRecipe recipe : AlchemyRecipeRegistry.recipes) {
            if (recipe == null || recipe.getResult() == null) continue;
            if (ingredientCount(recipe.getRecipe()) != inputs.size()) continue;
            if (!recipe.doesRecipeMatch(slots, Integer.MAX_VALUE)) continue;
            if (!sameStackAndCount(recipe.getResult(), expectedOutput)) continue;

            ItemStack result = blank.copy();
            result.stackSize = 1;
            List<ItemStack> encodedInputs = new ArrayList<ItemStack>();
            for (ItemStack input : inputs) {
                ItemStack one = input.copy();
                one.stackSize = 1;
                encodedInputs.add(one);
            }
            BloodMagicPatternData.encode(result, BloodMagicPatternKind.ALCHEMY_TABLE,
                    encodedInputs, recipe.getResult(),
                    scaledAlchemyLifeCost(recipe.getAmountNeeded()), recipe.getOrbLevel(),
                    BloodMagicAddonConfig.bloodAltarAssemblerBaseCraftTimeTicks);
            return result;
        }
        return null;
    }

    /**
     * Blood Magic 1.7.10 stores AlchemyRecipe.amountNeeded as LP siphoned per
     * progress tick. The Writing Table completes at 100 progress, so the
     * machine equivalent must reserve the total recipe LP rather than one tick.
     */
    private static int scaledAlchemyLifeCost(int amountPerTick) {
        long total = Math.max(0L, (long) amountPerTick) * LEGACY_ALCHEMY_PROGRESS_TICKS;
        double scaled = Math.ceil(total * BloodMagicAddonConfig.bloodAltarAssemblerLifeEssenceMultiplier);
        return scaled >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) scaled;
    }

    private static int scaledLifeCost(int base) {
        double scaled = Math.ceil(Math.max(0, base) * BloodMagicAddonConfig.bloodAltarAssemblerLifeEssenceMultiplier);
        return scaled >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) scaled;
    }

    private static List<ItemStack> expandInputs(IAEItemStack[] aeInputs) {
        List<ItemStack> result = new ArrayList<ItemStack>();
        if (aeInputs == null) return result;

        for (IAEItemStack ae : aeInputs) {
            if (ae == null) continue;
            ItemStack stack = ae.getItemStack();
            if (stack == null) continue;
            long amount = Math.max(0L, ae.getStackSize());
            if (amount > 64L) return new ArrayList<ItemStack>();
            for (int i = 0; i < amount; i++) {
                ItemStack one = stack.copy();
                one.stackSize = 1;
                result.add(one);
                if (result.size() > 5) return result;
            }
        }
        return result;
    }

    private static ItemStack getSingleOutput(IAEItemStack[] outputs) {
        if (outputs == null || outputs.length != 1 || outputs[0] == null) return null;
        ItemStack stack = outputs[0].getItemStack();
        if (stack == null) return null;
        long amount = outputs[0].getStackSize();
        if (amount <= 0L || amount > Integer.MAX_VALUE) return null;
        stack.stackSize = (int) amount;
        return stack;
    }

    private static int ingredientCount(ItemStack[] recipe) {
        int count = 0;
        if (recipe != null) {
            for (ItemStack stack : recipe) if (stack != null) count++;
        }
        return count;
    }

    private static boolean sameStackAndCount(ItemStack a, ItemStack b) {
        return a != null && b != null
                && a.stackSize == b.stackSize
                && a.isItemEqual(b)
                && ItemStack.areItemStackTagsEqual(a, b);
    }
}
