package com.nosferatu.divinemachinerylegacy.gaiarelics;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.ModItems;

public final class GaiaRelicsLegacy {
    public static Item yggdrasilEssence;
    public static Item muspelEssence;
    public static Item niflEssence;
    public static Item yggdrasilIngot;
    public static Item muspelIngot;
    public static Item niflIngot;
    public static Item gaiaEchoBlade;
    public static Item gaiaBlade;
    public static Item valkyrieFeather;

    private static boolean registered;

    private GaiaRelicsLegacy() { }

    public static void registerAll() {
        if (registered) return;
        registered = true;

        yggdrasilEssence = material("yggdrasil_essence", ItemGaiaRelicMaterial.Kind.YGGDRASIL_ESSENCE);
        muspelEssence = material("muspel_essence", ItemGaiaRelicMaterial.Kind.MUSPEL_ESSENCE);
        niflEssence = material("nifl_essence", ItemGaiaRelicMaterial.Kind.NIFL_ESSENCE);
        yggdrasilIngot = material("yggdrasil_ingot", ItemGaiaRelicMaterial.Kind.YGGDRASIL_INGOT);
        muspelIngot = material("muspel_ingot", ItemGaiaRelicMaterial.Kind.MUSPEL_INGOT);
        niflIngot = material("nifl_ingot", ItemGaiaRelicMaterial.Kind.NIFL_INGOT);

        gaiaEchoBlade = new ItemGaiaEchoBlade();
        gaiaBlade = new ItemGaiaBlade();
        valkyrieFeather = new ItemValkyrieFeather();
        GameRegistry.registerItem(gaiaEchoBlade, "gaia_echo_blade");
        GameRegistry.registerItem(gaiaBlade, "gaia_blade");
        GameRegistry.registerItem(valkyrieFeather, "valkyrie_feather");
        GaiaRelicsLocalization.register();

        GaiaRelicsEvents events = new GaiaRelicsEvents();
        MinecraftForge.EVENT_BUS.register(events);
        FMLCommonHandler.instance().bus().register(events);

        registerRecipes();
    }

    private static Item material(String key, ItemGaiaRelicMaterial.Kind kind) {
        Item item = new ItemGaiaRelicMaterial(key, kind);
        GameRegistry.registerItem(item, key);
        return item;
    }

    private static void registerRecipes() {
        // 1.7.10 adaptations of the three modern 90k-mana realm-essence recipes.
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(yggdrasilEssence, 2), 90000,
                rune(2), rune(4), rune(8), manaResource(2), manaResource(9), manaResource(14),
                new ItemStack(Items.golden_apple), new ItemStack(Items.nether_star), new ItemStack(Blocks.emerald_block));

        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(muspelEssence, 2), 90000,
                rune(1), rune(5), rune(13), manaResource(2), manaResource(9), manaResource(14),
                new ItemStack(Items.nether_star), new ItemStack(Items.blaze_rod), new ItemStack(Items.lava_bucket));

        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(niflEssence, 2), 90000,
                rune(0), rune(7), rune(14), manaResource(2), manaResource(9), manaResource(14),
                new ItemStack(Items.nether_star), new ItemStack(Blocks.packed_ice), new ItemStack(Items.water_bucket));

        // Netherite does not exist in 1.7.10; Terrasteel is the natural late-Botania core.
        realmIngot(yggdrasilIngot, yggdrasilEssence);
        realmIngot(muspelIngot, muspelEssence);
        realmIngot(niflIngot, niflEssence);

        GameRegistry.addRecipe(new ItemStack(gaiaEchoBlade),
                " G ", "ESE", " N ",
                'G', manaResource(14),
                'E', manaResource(9),
                'S', new ItemStack(Items.diamond_sword),
                'N', new ItemStack(Items.nether_star));

        GameRegistry.addRecipe(new ItemStack(gaiaBlade),
                "YGM", "GBG", "NGN",
                'Y', yggdrasilIngot,
                'M', muspelIngot,
                'N', niflIngot,
                'G', manaResource(14),
                'B', gaiaEchoBlade);

        GameRegistry.addRecipe(new ItemStack(valkyrieFeather),
                "YFY", "MGM", "NNN",
                'Y', yggdrasilIngot,
                'F', Items.feather,
                'M', muspelIngot,
                'G', manaResource(14),
                'N', niflIngot);
    }

    private static void realmIngot(Item output, Item essence) {
        GameRegistry.addRecipe(new ItemStack(output),
                " E ", "ETE", " E ",
                'E', essence,
                'T', manaResource(4));
    }

    private static ItemStack rune(int meta) {
        return new ItemStack(ModItems.rune, 1, meta);
    }

    private static ItemStack manaResource(int meta) {
        return new ItemStack(ModItems.manaResource, 1, meta);
    }
}
