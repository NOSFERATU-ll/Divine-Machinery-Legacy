package com.nosferatu.divinemachinerylegacy.tile;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.block.ItemBlockSpecialFlower;

import java.util.Random;

/** 1.7.10 fuel rules for the generating flowers supported by Reforked's Greenhouse. */
public final class GreenhouseFlowerRegistry {
    private GreenhouseFlowerRegistry() { }

    public static boolean isGeneratingFlower(ItemStack stack) {
        String type = type(stack);
        return "endoflame".equals(type) || "entropinnyum".equals(type)
                || "gourmaryllis".equals(type) || "kekimurus".equals(type)
                || "munchdew".equals(type) || "narslimmus".equals(type)
                || "rafflowsia".equals(type) || "arcanerose".equals(type)
                || "witherAconite".equals(type) || "witheraconite".equals(type);
    }

    public static boolean isFuelFor(ItemStack flower, ItemStack fuel) {
        return getBaseManaPerFuel(flower, fuel, null) > 0;
    }

    public static int getBaseManaPerFuel(ItemStack flower, ItemStack fuel, Random random) {
        if (flower == null || fuel == null || fuel.stackSize <= 0) return 0;
        String type = type(flower);
        if ("endoflame".equals(type)) return Math.max(0, TileEntityFurnace.getItemBurnTime(fuel));
        if ("entropinnyum".equals(type)) return Block.getBlockFromItem(fuel.getItem()) == Blocks.tnt ? 6500 : 0;
        if ("gourmaryllis".equals(type)) {
            if (!(fuel.getItem() instanceof ItemFood)) return 0;
            int food = Math.min(12, ((ItemFood) fuel.getItem()).func_150905_g(fuel));
            return (int) Math.round(food * food * 70D * 1.8D);
        }
        if ("kekimurus".equals(type)) return fuel.getItem() == Items.cake ? 7200 : 0;
        if ("munchdew".equals(type)) return isOre(fuel, "treeLeaves") ? 160 : 0;
        if ("narslimmus".equals(type)) return fuel.getItem() == Items.slime_ball ? 100 : 0;
        if ("rafflowsia".equals(type)) return Block.getBlockFromItem(fuel.getItem()) == ModBlocks.flower ? 100 : 0;
        if ("arcanerose".equals(type)) {
            if (fuel.getItem() != Items.experience_bottle) return 0;
            Random rng = random == null ? new Random() : random;
            return (3 + rng.nextInt(30)) * 50;
        }
        if ("witherAconite".equals(type) || "witheraconite".equals(type))
            return fuel.getItem() == Items.nether_star ? 1200000 : 0;
        return 0;
    }

    private static String type(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemBlockSpecialFlower)) return "";
        return ItemBlockSpecialFlower.getType(stack);
    }

    private static boolean isOre(ItemStack stack, String name) {
        int wanted = OreDictionary.getOreID(name);
        for (int id : OreDictionary.getOreIDs(stack)) if (id == wanted) return true;
        return false;
    }
}
