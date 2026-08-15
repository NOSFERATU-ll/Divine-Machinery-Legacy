package com.nosferatu.divinemachinerylegacy.bloodmagic;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

/** NBT format for the 1.7.10 Blood Pattern. */
public final class BloodMagicPatternData {
    public static final String KEY_MARKER = "DMLBloodPattern";
    public static final String KEY_KIND = "DMLBloodPatternKind";
    public static final String KEY_LP = "DMLBloodPatternLP";
    public static final String KEY_TIER = "DMLBloodPatternTier";
    public static final String KEY_TIME = "DMLBloodPatternTime";

    private BloodMagicPatternData() { }

    public static boolean isEncoded(ItemStack stack) {
        return stack != null && stack.hasTagCompound()
                && stack.getTagCompound().getBoolean(KEY_MARKER)
                && getKind(stack) != null
                && getOutput(stack) != null
                && !getInputs(stack).isEmpty();
    }

    public static BloodMagicPatternKind getKind(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return null;
        return BloodMagicPatternKind.fromId(stack.getTagCompound().getString(KEY_KIND));
    }

    public static int getLifeEssenceCost(ItemStack stack) {
        return stack != null && stack.hasTagCompound() ? Math.max(0, stack.getTagCompound().getInteger(KEY_LP)) : 0;
    }

    public static int getRequiredTier(ItemStack stack) {
        return stack != null && stack.hasTagCompound() ? Math.max(0, stack.getTagCompound().getInteger(KEY_TIER)) : 0;
    }

    public static int getCraftTime(ItemStack stack) {
        return stack != null && stack.hasTagCompound() ? Math.max(1, stack.getTagCompound().getInteger(KEY_TIME)) : 1;
    }

    public static List<ItemStack> getInputs(ItemStack stack) {
        List<ItemStack> result = new ArrayList<ItemStack>();
        if (stack == null || !stack.hasTagCompound()) return result;
        NBTTagList list = stack.getTagCompound().getTagList("in", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack ingredient = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
            if (ingredient != null && ingredient.stackSize > 0) result.add(ingredient);
        }
        return result;
    }

    public static ItemStack getOutput(ItemStack stack) {
        if (stack == null || !stack.hasTagCompound()) return null;
        NBTTagList list = stack.getTagCompound().getTagList("out", Constants.NBT.TAG_COMPOUND);
        if (list.tagCount() <= 0) return null;
        return ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(0));
    }

    public static void encode(ItemStack target, BloodMagicPatternKind kind, List<ItemStack> inputs,
                              ItemStack output, int lifeEssenceCost, int requiredTier, int craftTime) {
        if (target == null || kind == null || inputs == null || inputs.isEmpty() || output == null) {
            throw new IllegalArgumentException("Incomplete Blood Pattern data");
        }

        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean(KEY_MARKER, true);
        tag.setString(KEY_KIND, kind.getId());
        tag.setInteger(KEY_LP, Math.max(0, lifeEssenceCost));
        tag.setInteger(KEY_TIER, Math.max(0, requiredTier));
        tag.setInteger(KEY_TIME, Math.max(1, craftTime));

        // Keep AE2's familiar processing-pattern NBT names as well. This makes
        // the stack easier to inspect and preserves interoperability with tools
        // that only know the old rv3 pattern shape.
        NBTTagList in = new NBTTagList();
        for (ItemStack ingredient : inputs) {
            if (ingredient == null || ingredient.stackSize <= 0) continue;
            NBTTagCompound itemTag = new NBTTagCompound();
            ingredient.copy().writeToNBT(itemTag);
            in.appendTag(itemTag);
        }

        NBTTagList out = new NBTTagList();
        NBTTagCompound outputTag = new NBTTagCompound();
        output.copy().writeToNBT(outputTag);
        out.appendTag(outputTag);

        tag.setTag("in", in);
        tag.setTag("out", out);
        tag.setBoolean("crafting", false);
        tag.setBoolean("substitute", false);
        target.setTagCompound(tag);
    }

    public static void clear(ItemStack stack) {
        if (stack != null) stack.setTagCompound(null);
    }
}
