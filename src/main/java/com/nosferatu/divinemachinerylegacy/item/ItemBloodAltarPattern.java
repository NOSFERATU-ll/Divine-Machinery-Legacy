package com.nosferatu.divinemachinerylegacy.item;

import WayofTime.alchemicalWizardry.api.alchemy.AlchemyRecipe;
import WayofTime.alchemicalWizardry.api.alchemy.AlchemyRecipeRegistry;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipe;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipeRegistry;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternData;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternDetails;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicPatternKind;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

/** Blood Pattern from BloodMagic Additions, adapted to AE2 rv3's pattern API. */
public class ItemBloodAltarPattern extends Item implements ICraftingPatternItem {
    @SideOnly(Side.CLIENT)
    private IIcon blankIcon;
    @SideOnly(Side.CLIENT)
    private IIcon encodedIcon;

    public ItemBloodAltarPattern() {
        setUnlocalizedName(DivineMachineryLegacy.MODID + ".blood_altar_pattern");
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        setMaxStackSize(64);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return BloodMagicPatternData.isEncoded(stack) ? 1 : 64;
    }

    @Override
    public ICraftingPatternDetails getPatternForItem(ItemStack stack, World world) {
        if (!BloodMagicPatternData.isEncoded(stack)) return null;
        try {
            return new BloodMagicPatternDetails(stack);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!player.isSneaking()) return stack;

        if (!world.isRemote) {
            if (BloodMagicPatternData.isEncoded(stack)) {
                BloodMagicPatternData.clear(stack);
                stack.stackSize = Math.max(1, stack.stackSize);
                player.inventory.markDirty();
                player.addChatMessage(new ChatComponentTranslation(
                        "message.divinemachinerylegacy.blood_altar_pattern.cleared"));
            } else {
                player.addChatMessage(new ChatComponentTranslation(
                        "message.divinemachinerylegacy.blood_altar_pattern.already_empty"));
            }
        }
        return stack;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (BloodMagicPatternData.isEncoded(stack)) {
            return "item." + DivineMachineryLegacy.MODID + ".blood_altar_pattern.encoded";
        }
        return super.getUnlocalizedName(stack);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (!BloodMagicPatternData.isEncoded(stack)) return super.getItemStackDisplayName(stack);
        ItemStack output = BloodMagicPatternData.getOutput(stack);
        if (output == null) return super.getItemStackDisplayName(stack);
        return StatCollector.translateToLocalFormatted(
                "item." + DivineMachineryLegacy.MODID + ".blood_altar_pattern.encoded.name",
                output.getDisplayName());
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        if (!BloodMagicPatternData.isEncoded(stack)) {
            lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal(
                    "tooltip.divinemachinerylegacy.blood_altar_pattern.empty"));
            lines.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal(
                    "tooltip.divinemachinerylegacy.blood_altar_pattern.how_to_encode"));
            return;
        }

        BloodMagicPatternKind kind = BloodMagicPatternData.getKind(stack);
        ItemStack output = BloodMagicPatternData.getOutput(stack);
        if (kind == null || output == null) {
            lines.add(EnumChatFormatting.RED + StatCollector.translateToLocal(
                    "tooltip.divinemachinerylegacy.blood_altar_pattern.invalid"));
            return;
        }

        String kindName = StatCollector.translateToLocal(
                "tooltip.divinemachinerylegacy.blood_altar_pattern.type." + kind.getId());
        lines.add(EnumChatFormatting.DARK_RED + StatCollector.translateToLocalFormatted(
                "tooltip.divinemachinerylegacy.blood_altar_pattern.type", kindName));

        List<ItemStack> inputs = BloodMagicPatternData.getInputs(stack);
        for (int i = 0; i < inputs.size(); i++) {
            ItemStack input = inputs.get(i);
            String text = input.stackSize + "x " + input.getDisplayName();
            String key = inputs.size() == 1
                    ? "tooltip.divinemachinerylegacy.blood_altar_pattern.input"
                    : "tooltip.divinemachinerylegacy.blood_altar_pattern.input_indexed";
            if (inputs.size() == 1) {
                lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(key, text));
            } else {
                lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(key, i + 1, text));
            }
        }

        lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                "tooltip.divinemachinerylegacy.blood_altar_pattern.output",
                output.stackSize + "x " + output.getDisplayName()));
        lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                "tooltip.divinemachinerylegacy.blood_altar_pattern.tier",
                BloodMagicPatternData.getRequiredTier(stack)));
        // The original tooltip displays the recipe's stored syphon value, not
        // the configurable runtime multiplier.
        lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                "tooltip.divinemachinerylegacy.blood_altar_pattern.life_essence",
                BloodMagicPatternData.getLifeEssenceCost(stack)));
        lines.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal(
                "tooltip.divinemachinerylegacy.blood_altar_pattern.shift_clear"));

        if (!recipeStillExists(kind, inputs, output, BloodMagicPatternData.getRequiredTier(stack))) {
            lines.add(EnumChatFormatting.RED + StatCollector.translateToLocal(
                    "tooltip.divinemachinerylegacy.blood_altar_pattern.recipe_missing"));
        }
    }

    private boolean recipeStillExists(BloodMagicPatternKind kind, List<ItemStack> inputs,
                                      ItemStack output, int storedTier) {
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
            if (inputs.isEmpty() || inputs.size() > 5) return false;
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

    private int ingredientCount(ItemStack[] recipe) {
        int count = 0;
        if (recipe != null) for (ItemStack stack : recipe) if (stack != null) count++;
        return count;
    }

    private boolean sameStackAndCount(ItemStack a, ItemStack b) {
        return a != null && b != null && a.stackSize == b.stackSize
                && a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        blankIcon = register.registerIcon(DivineMachineryLegacy.MODID + ":bloodmagic/blood_altar_pattern");
        encodedIcon = register.registerIcon(DivineMachineryLegacy.MODID + ":bloodmagic/blood_altar_pattern_encoded");
        itemIcon = blankIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return BloodMagicPatternData.isEncoded(stack) ? encodedIcon : blankIcon;
    }
}
