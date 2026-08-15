package com.nosferatu.divinemachinerylegacy;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Dedicated creative/NEI category for all ported machinery and upgrades. */
public class CreativeTabDivineMachinery extends CreativeTabs {

    public CreativeTabDivineMachinery() {
        super(DivineMachineryLegacy.MODID);
    }

    @Override
    public Item getTabIconItem() {
        if (DivineMachineryLegacy.mechanicalRunicAltar != null) {
            return Item.getItemFromBlock(DivineMachineryLegacy.mechanicalRunicAltar);
        }
        return Items.nether_star;
    }

    @Override
    public void displayAllReleventItems(List list) {
        super.displayAllReleventItems(list);
        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object left, Object right) {
                ItemStack a = (ItemStack) left;
                ItemStack b = (ItemStack) right;
                int groupA = group(a);
                int groupB = group(b);
                if (groupA != groupB) return groupA - groupB;
                int orderA = localOrder(a);
                int orderB = localOrder(b);
                if (orderA != orderB) return orderA - orderB;
                return key(a).compareTo(key(b));
            }
        });
    }

    private static String key(ItemStack stack) {
        if (stack == null) return "";
        String name = stack.getUnlocalizedName();
        return name == null ? "" : name;
    }

    private static int group(ItemStack stack) {
        String n = key(stack);
        // Botania machinery first.
        if (n.contains("mechanical_") || n.contains("jaded_amaranthus") || n.contains("greenhouse")) {
            if (!n.contains("upgrade_")) return 100;
        }
        // Reforked materials and storage blocks.
        if ((n.contains("_ingot") || n.contains("_dragonstone")) && !n.contains("gaia")) return 200;
        if (n.contains("_spark")) return 220;
        if (n.contains("catalyst_")) return 240;
        if (n.contains("upgrade_")) return 260;

        // Gaia Relics: raw essences -> ingots -> relics.
        if (n.contains("yggdrasil_essence") || n.contains("muspel_essence") || n.contains("nifl_essence")) return 300;
        if (n.contains("yggdrasil_ingot") || n.contains("muspel_ingot") || n.contains("nifl_ingot")) return 320;
        if (n.contains("gaia_echo_blade") || n.contains("gaia_blade") || n.contains("valkyrie_feather")) return 340;

        // Blood Magic machines/patterns and then their upgrade cards.
        if (n.contains("blood_generator") || n.contains("blood_altar_assembler") || n.contains("blood_altar_pattern")) return 400;
        if (n.contains("blood_altar_tier_card") || n.contains("blood_altar_parallel_card") || n.contains("blood_magic_speed_card")) return 420;
        return 900;
    }

    private static int localOrder(ItemStack stack) {
        String n = key(stack);
        if (n.contains("yggdrasil_")) return 10;
        if (n.contains("muspel_")) return 20;
        if (n.contains("nifl_")) return 30;
        if (n.contains("gaia_echo_blade")) return 40;
        if (n.contains("gaia_blade")) return 50;
        if (n.contains("valkyrie_feather")) return 60;
        if (n.contains("blood_generator")) return 10;
        if (n.contains("blood_altar_assembler")) return 20;
        if (n.contains("blood_altar_pattern")) return 30;
        if (n.contains("blood_altar_tier_card_2")) return 40;
        if (n.contains("blood_altar_tier_card_3")) return 50;
        if (n.contains("blood_altar_tier_card_4")) return 60;
        if (n.contains("blood_altar_tier_card_5")) return 70;
        if (n.contains("blood_altar_parallel_card")) return 80;
        if (n.contains("blood_magic_speed_card")) return 90;
        return 500;
    }
}
