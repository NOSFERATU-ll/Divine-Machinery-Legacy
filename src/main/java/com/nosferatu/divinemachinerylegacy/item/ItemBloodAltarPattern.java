package com.nosferatu.divinemachinerylegacy.item;

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
        if (player.isSneaking() && BloodMagicPatternData.isEncoded(stack)) {
            if (!world.isRemote) {
                BloodMagicPatternData.clear(stack);
                stack.stackSize = Math.max(1, stack.stackSize);
                player.inventory.markDirty();
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        if (!BloodMagicPatternData.isEncoded(stack)) {
            lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal(
                    "tooltip.divinemachinerylegacy.blood_altar_pattern.empty"));
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
        lines.add(EnumChatFormatting.DARK_RED + StatCollector.translateToLocalFormatted(
                "tooltip.divinemachinerylegacy.blood_altar_pattern.life_essence",
                BloodMagicPatternData.getLifeEssenceCost(stack)));
        lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                "tooltip.divinemachinerylegacy.blood_altar_pattern.craft_time",
                BloodMagicPatternData.getCraftTime(stack)));
        lines.add(EnumChatFormatting.DARK_GRAY + StatCollector.translateToLocal(
                "tooltip.divinemachinerylegacy.blood_altar_pattern.shift_clear"));
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
