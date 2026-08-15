package com.nosferatu.divinemachinerylegacy.recipe;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.block.ItemBlockSpecialFlower;

/** DML-native 1.7.10 progression for machines, materials, catalysts and Greenhouse upgrades. */
public final class RecipeRegistrar {
    private RecipeRegistrar() { }

    public static void registerAll() {
        registerMaterialProgression();
        ManaInfuserRecipes.registerDefaults();
        registerMachines();
        registerCatalysts();
        registerGreenhouseUpgrades();
    }

    private static void registerMaterialProgression() {
        // Botania 1.7.10 has no extensible Terra Plate recipe registry. Crystal is
        // therefore the bootstrap step on the Rune Altar; the remaining Reforked
        // Terra Plate material recipes are handled by our Mana Infuser registry.
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(DivineMachineryLegacy.materialIngots[4]), 250000,
                manaResource(0, 1), new ItemStack(Blocks.glass), new ItemStack(Blocks.glass), manaResource(7, 1));

        // These are the original Reforked Elven Trade chains, translated directly
        // to Botania 1.7.10's real Elven Trade registry. The Mechanical Alfheim
        // Market picks them up automatically because it consumes that registry.
        BotaniaAPI.registerElvenTradeRecipe(new ItemStack(DivineMachineryLegacy.materialDragonstones[4]),
                new ItemStack(DivineMachineryLegacy.materialIngots[4]), manaResource(0, 1));
        BotaniaAPI.registerElvenTradeRecipe(new ItemStack(DivineMachineryLegacy.materialDragonstones[0]),
                new ItemStack(DivineMachineryLegacy.materialIngots[0]), manaResource(9, 1));
        BotaniaAPI.registerElvenTradeRecipe(new ItemStack(DivineMachineryLegacy.materialDragonstones[1]),
                new ItemStack(DivineMachineryLegacy.materialIngots[1]), new ItemStack(DivineMachineryLegacy.materialDragonstones[0]));
        BotaniaAPI.registerElvenTradeRecipe(new ItemStack(DivineMachineryLegacy.materialDragonstones[2]),
                new ItemStack(DivineMachineryLegacy.materialIngots[2]), new ItemStack(DivineMachineryLegacy.materialDragonstones[1]));
        BotaniaAPI.registerElvenTradeRecipe(new ItemStack(DivineMachineryLegacy.materialDragonstones[3]),
                new ItemStack(DivineMachineryLegacy.materialIngots[3]), new ItemStack(DivineMachineryLegacy.materialDragonstones[2]));
        BotaniaAPI.registerElvenTradeRecipe(new ItemStack(DivineMachineryLegacy.materialDragonstones[5]),
                new ItemStack(DivineMachineryLegacy.materialIngots[5]), new ItemStack(DivineMachineryLegacy.materialDragonstones[3]));
        BotaniaAPI.registerElvenTradeRecipe(new ItemStack(DivineMachineryLegacy.materialDragonstones[6]),
                new ItemStack(DivineMachineryLegacy.materialIngots[6]), new ItemStack(DivineMachineryLegacy.materialDragonstones[5]));
    }

    private static void registerMachines() {
        baseMachine(DivineMachineryLegacy.mechanicalRunicAltar, new ItemStack(vazkii.botania.common.block.ModBlocks.runeAltar));
        baseMachine(DivineMachineryLegacy.mechanicalManaPool, new ItemStack(vazkii.botania.common.block.ModBlocks.pool));
        baseManaInfuser();
        baseMachine(DivineMachineryLegacy.mechanicalApothecary, new ItemStack(vazkii.botania.common.block.ModBlocks.altar));
        baseMachine(DivineMachineryLegacy.mechanicalDaisy, ItemBlockSpecialFlower.ofType("puredaisy"));
        baseMachine(DivineMachineryLegacy.mechanicalIndustrialAgglomerationFactory, new ItemStack(vazkii.botania.common.block.ModBlocks.terraPlate));
        baseMachine(DivineMachineryLegacy.mechanicalAlfheimMarket, new ItemStack(vazkii.botania.common.block.ModBlocks.alfPortal));
        baseMachine(DivineMachineryLegacy.mechanicalOrechid, ItemBlockSpecialFlower.ofType("orechid"));

        tierUp(DivineMachineryLegacy.mechanicalRunicAltar);
        tierUp(DivineMachineryLegacy.mechanicalManaPool);
        tierUp(DivineMachineryLegacy.mechanicalManaInfuser);
        tierUp(DivineMachineryLegacy.mechanicalApothecary);
        tierUp(DivineMachineryLegacy.mechanicalDaisy);
        tierUp(DivineMachineryLegacy.mechanicalIndustrialAgglomerationFactory);
        tierUp(DivineMachineryLegacy.mechanicalAlfheimMarket);
        tierUp(DivineMachineryLegacy.mechanicalOrechid);

        GameRegistry.addRecipe(new ItemStack(DivineMachineryLegacy.jadedAmaranthus),
                "idi", "djd", "idi",
                'i', DivineMachineryLegacy.materialIngots[4],
                'd', DivineMachineryLegacy.materialDragonstones[4],
                'j', ItemBlockSpecialFlower.ofType("jadedAmaranthus"));

        GameRegistry.addRecipe(new ItemStack(DivineMachineryLegacy.greenhouse),
                "cgc", "geg", "cgc",
                'c', DivineMachineryLegacy.materialIngots[4],
                'g', DivineMachineryLegacy.materialDragonstones[4],
                'e', ItemBlockSpecialFlower.ofType("endoflame"));
    }

    /** Crystal bootstraps the Malachite machine that creates the rest of the material chain. */
    private static void baseManaInfuser() {
        GameRegistry.addRecipe(new ItemStack(DivineMachineryLegacy.mechanicalManaInfuser, 1, 0),
                "idi", "dtd", "idi",
                'i', DivineMachineryLegacy.materialIngots[4],
                'd', DivineMachineryLegacy.materialDragonstones[4],
                't', new ItemStack(vazkii.botania.common.block.ModBlocks.terraPlate));
    }

    private static void baseMachine(Block machine, ItemStack core) {
        GameRegistry.addRecipe(new ItemStack(machine, 1, 0),
                "idi", "dcd", "idi",
                'i', DivineMachineryLegacy.materialIngots[0],
                'd', DivineMachineryLegacy.materialDragonstones[0],
                'c', core);
    }

    private static void tierUp(Block machine) {
        for (int tier = 1; tier <= 3; tier++) {
            GameRegistry.addRecipe(new ItemStack(machine, 1, tier),
                    "idi", "dmd", "idi",
                    'i', DivineMachineryLegacy.materialIngots[tier],
                    'd', DivineMachineryLegacy.materialDragonstoneBlocks[tier],
                    'm', new ItemStack(machine, 1, tier - 1));
        }
    }

    private static void registerCatalysts() {
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(DivineMachineryLegacy.catalystSpeed), 250000,
                rune(3), rune(8), rune(11), new ItemStack(Items.sugar),
                new ItemStack(DivineMachineryLegacy.materialIngots[2]), new ItemStack(DivineMachineryLegacy.materialDragonstones[2]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(DivineMachineryLegacy.catalystManaInfinity), 1000000,
                rune(8), rune(9), rune(15), new ItemStack(Items.nether_star),
                new ItemStack(DivineMachineryLegacy.materialIngotBlocks[3]), new ItemStack(DivineMachineryLegacy.materialDragonstoneBlocks[3]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(DivineMachineryLegacy.catalystLivingrockInfinity), 750000,
                rune(0), rune(8), new ItemStack(vazkii.botania.common.block.ModBlocks.livingrock), new ItemStack(Items.nether_star),
                new ItemStack(DivineMachineryLegacy.materialIngots[3]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(DivineMachineryLegacy.catalystSeedInfinity), 500000,
                rune(1), rune(8), new ItemStack(Items.wheat_seeds), new ItemStack(Items.nether_star), new ItemStack(DivineMachineryLegacy.materialIngots[2]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(DivineMachineryLegacy.catalystWaterInfinity), 500000,
                rune(2), rune(8), new ItemStack(Items.water_bucket), new ItemStack(Items.nether_star), new ItemStack(DivineMachineryLegacy.materialIngots[2]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(DivineMachineryLegacy.catalystStoneInfinity), 500000,
                rune(0), rune(8), new ItemStack(Blocks.stone), new ItemStack(Items.nether_star), new ItemStack(DivineMachineryLegacy.materialIngots[2]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(DivineMachineryLegacy.catalystWoodInfinity), 500000,
                rune(1), rune(8), new ItemStack(Blocks.log), new ItemStack(Items.nether_star), new ItemStack(DivineMachineryLegacy.materialIngots[2]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(DivineMachineryLegacy.catalystPetal), 300000,
                rune(3), rune(8), new ItemStack(vazkii.botania.common.item.ModItems.petal, 1, 0),
                new ItemStack(DivineMachineryLegacy.materialIngots[1]), new ItemStack(DivineMachineryLegacy.materialDragonstones[1]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(DivineMachineryLegacy.catalystPetalBlock), 600000,
                rune(3), rune(8), new ItemStack(DivineMachineryLegacy.catalystPetal), new ItemStack(vazkii.botania.common.block.ModBlocks.petalBlock, 1, 0),
                new ItemStack(DivineMachineryLegacy.materialIngots[3]), new ItemStack(DivineMachineryLegacy.materialDragonstones[3]));
    }

    private static void registerGreenhouseUpgrades() {
        shapedUpgrade(0, DivineMachineryLegacy.materialIngots[0], DivineMachineryLegacy.materialDragonstones[0], Items.redstone);
        chainedUpgrade(1, 0, DivineMachineryLegacy.materialIngots[1], DivineMachineryLegacy.materialDragonstones[1]);
        chainedUpgrade(2, 1, DivineMachineryLegacy.materialIngots[2], DivineMachineryLegacy.materialDragonstones[2]);
        chainedUpgrade(3, 2, DivineMachineryLegacy.materialIngots[3], DivineMachineryLegacy.materialDragonstones[3]);
        shapedUpgrade(4, DivineMachineryLegacy.materialIngots[1], DivineMachineryLegacy.materialDragonstones[1], Items.glowstone_dust);
        shapedUpgrade(5, DivineMachineryLegacy.materialIngots[1], DivineMachineryLegacy.materialDragonstones[1], Blocks.chest);
        shapedUpgrade(6, DivineMachineryLegacy.materialIngots[1], DivineMachineryLegacy.materialDragonstones[1], Items.redstone);
        shapedUpgrade(7, DivineMachineryLegacy.materialIngots[1], DivineMachineryLegacy.materialDragonstones[1], Items.quartz);
        chainedUpgrade(8, 7, DivineMachineryLegacy.materialIngots[2], DivineMachineryLegacy.materialDragonstones[2]);
        shapedUpgrade(9, DivineMachineryLegacy.materialIngots[0], DivineMachineryLegacy.materialDragonstones[0], Blocks.redstone_block);
        chainedUpgrade(10, 9, DivineMachineryLegacy.materialIngots[1], DivineMachineryLegacy.materialDragonstones[1]);
        chainedUpgrade(11, 10, DivineMachineryLegacy.materialIngots[3], DivineMachineryLegacy.materialDragonstones[3]);
        shapedUpgrade(12, DivineMachineryLegacy.materialIngots[0], DivineMachineryLegacy.materialDragonstones[0], DivineMachineryLegacy.manaSparks[1]);
        chainedUpgrade(13, 12, DivineMachineryLegacy.materialIngots[1], DivineMachineryLegacy.materialDragonstones[1]);
        chainedUpgrade(14, 13, DivineMachineryLegacy.materialIngots[3], DivineMachineryLegacy.materialDragonstones[3]);
        GameRegistry.addRecipe(new ItemStack(DivineMachineryLegacy.greenhouseHeatUpgrade),
                "bcb", "csc", "bcb", 'b', Items.blaze_powder, 'c', DivineMachineryLegacy.materialIngots[2], 's', DivineMachineryLegacy.materialDragonstones[2]);
    }

    private static void shapedUpgrade(int out, Object a, Object b, Object center) {
        GameRegistry.addRecipe(new ItemStack(DivineMachineryLegacy.greenhouseUpgrades[out]),
                "aba", "bcb", "aba", 'a', a, 'b', b, 'c', center);
    }
    private static void chainedUpgrade(int out, int previous, Object a, Object b) {
        GameRegistry.addRecipe(new ItemStack(DivineMachineryLegacy.greenhouseUpgrades[out]),
                "aba", "bpb", "aba", 'a', a, 'b', b, 'p', DivineMachineryLegacy.greenhouseUpgrades[previous]);
    }
    private static ItemStack rune(int meta) { return new ItemStack(ModItems.rune, 1, meta); }
    private static ItemStack manaResource(int meta, int count) { return new ItemStack(ModItems.manaResource, count, meta); }
}
