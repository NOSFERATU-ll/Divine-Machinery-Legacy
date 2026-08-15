package com.nosferatu.divinemachinerylegacy.bloodmagic;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import com.nosferatu.divinemachinerylegacy.config.BloodMagicAddonConfig;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/** AE2 rv3 processing-pattern view of an encoded Blood Pattern. */
public final class BloodMagicPatternDetails implements ICraftingPatternDetails {
    private final ItemStack pattern;
    private final IAEItemStack[] inputs;
    private final IAEItemStack[] condensedInputs;
    private final IAEItemStack[] outputs;
    private int priority;

    public BloodMagicPatternDetails(ItemStack pattern) {
        if (!BloodMagicPatternData.isEncoded(pattern)) {
            throw new IllegalArgumentException("Blood Pattern is not encoded");
        }
        if (!BloodMagicPatternRecipeValidator.recipeStillExists(pattern)) {
            throw new IllegalArgumentException("Blood Pattern recipe no longer exists");
        }
        this.pattern = pattern;

        List<IAEItemStack> rawInputs = new ArrayList<IAEItemStack>();
        for (ItemStack stack : BloodMagicPatternData.getInputs(pattern)) {
            IAEItemStack ae = AEApi.instance().storage().createItemStack(stack);
            if (ae != null) rawInputs.add(ae);
        }
        this.inputs = rawInputs.toArray(new IAEItemStack[rawInputs.size()]);
        this.condensedInputs = condense(rawInputs);

        ItemStack output = BloodMagicPatternData.getOutput(pattern);
        IAEItemStack aeOutput = AEApi.instance().storage().createItemStack(output);
        this.outputs = aeOutput == null ? new IAEItemStack[0] : new IAEItemStack[]{aeOutput};

        if (this.inputs.length == 0 || this.outputs.length == 0) {
            throw new IllegalArgumentException("Blood Pattern has no usable input/output");
        }
    }

    private static IAEItemStack[] condense(List<IAEItemStack> stacks) {
        List<IAEItemStack> condensed = new ArrayList<IAEItemStack>();
        for (IAEItemStack stack : stacks) {
            boolean merged = false;
            for (IAEItemStack existing : condensed) {
                if (existing.isSameType(stack)) {
                    existing.add(stack);
                    merged = true;
                    break;
                }
            }
            if (!merged) condensed.add(stack.copy());
        }
        return condensed.toArray(new IAEItemStack[condensed.size()]);
    }

    public BloodMagicPatternKind getKind() {
        return BloodMagicPatternData.getKind(pattern);
    }

    /**
     * Patterns store the recipe's unscaled effective LP requirement. The
     * multiplier is applied when AE2 asks the machine to execute it, matching
     * bmaddon's getRequiredLifeEssence() behavior and allowing config changes
     * to affect already encoded patterns.
     */
    public int getLifeEssenceCost() {
        int base = BloodMagicPatternData.getLifeEssenceCost(pattern);
        double multiplier = BloodMagicAddonConfig.bloodAltarAssemblerLifeEssenceMultiplier;
        if (base <= 0 || multiplier <= 0.0D) return 0;
        double scaled = Math.ceil(base * multiplier);
        if (scaled >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return Math.max(1, (int) scaled);
    }

    public int getRequiredTier() {
        return BloodMagicPatternData.getRequiredTier(pattern);
    }

    public int getCraftTime() {
        return BloodMagicPatternData.getCraftTime(pattern);
    }

    @Override
    public ItemStack getPattern() {
        return pattern;
    }

    @Override
    public boolean isValidItemForSlot(int slotIndex, ItemStack stack, World world) {
        return false;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public IAEItemStack[] getInputs() {
        return inputs;
    }

    @Override
    public IAEItemStack[] getCondensedInputs() {
        return condensedInputs;
    }

    @Override
    public IAEItemStack[] getOutputs() {
        return outputs;
    }

    @Override
    public IAEItemStack[] getCondensedOutputs() {
        return outputs;
    }

    @Override
    public boolean canSubstitute() {
        return false;
    }

    @Override
    public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
        return outputs.length == 0 ? null : outputs[0].getItemStack();
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
