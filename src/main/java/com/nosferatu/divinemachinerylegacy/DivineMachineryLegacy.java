package com.nosferatu.divinemachinerylegacy;

import com.nosferatu.divinemachinerylegacy.block.*;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicContent;
import com.nosferatu.divinemachinerylegacy.botania.SparkTier;
import com.nosferatu.divinemachinerylegacy.entity.EntityTieredManaSpark;
import com.nosferatu.divinemachinerylegacy.gaiarelics.GaiaRelicsLegacy;
import com.nosferatu.divinemachinerylegacy.gui.GuiHandler;
import com.nosferatu.divinemachinerylegacy.integration.ae2.Ae2InterfaceOutputReturner;
import com.nosferatu.divinemachinerylegacy.item.ItemBloodAltarTierCard;
import com.nosferatu.divinemachinerylegacy.item.ItemBloodMachineUpgrade;
import com.nosferatu.divinemachinerylegacy.item.ItemGreenhouseUpgrade;
import com.nosferatu.divinemachinerylegacy.item.ItemMachineCatalyst;
import com.nosferatu.divinemachinerylegacy.item.ItemReforkedMaterial;
import com.nosferatu.divinemachinerylegacy.item.ItemTieredManaSpark;
import com.nosferatu.divinemachinerylegacy.proxy.CommonProxy;
import com.nosferatu.divinemachinerylegacy.recipe.RecipeRegistrar;
import com.nosferatu.divinemachinerylegacy.tile.*;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.BotaniaAPI;

@Mod(modid = DivineMachineryLegacy.MODID, name = DivineMachineryLegacy.NAME,
        version = DivineMachineryLegacy.VERSION,
        dependencies = "required-after:Botania;required-after:appliedenergistics2;required-after:CoFHCore;required-after:AWWayofTime")
public class DivineMachineryLegacy {
    public static final String MODID = "divinemachinerylegacy";
    public static final String NAME = "Divine Machinery Legacy";
    public static final String VERSION = "0.1.0-dev";

    public static final int GUI_RUNIC_ALTAR = 1;
    public static final int GUI_MANA_POOL = 2;
    public static final int GUI_APOTHECARY = 3;
    public static final int GUI_DAISY = 4;
    public static final int GUI_INDUSTRIAL_AGGLOMERATION = 5;
    public static final int GUI_ALFHEIM_MARKET = 6;
    public static final int GUI_ORECHID = 7;
    public static final int GUI_JADED_AMARANTHUS = 8;
    public static final int GUI_GREENHOUSE = 9;
    public static final int GUI_MANA_INFUSER = 10;
    public static final int GUI_BLOOD_ALTAR_ASSEMBLER = 11;

    public static final String[] MATERIAL_KEYS = {
            "malachite", "saffron", "shadow", "crimson", "crystal", "aureate", "mazarine"
    };

    @Mod.Instance(MODID)
    public static DivineMachineryLegacy INSTANCE;

    @SidedProxy(clientSide = "com.nosferatu.divinemachinerylegacy.proxy.ClientProxy",
            serverSide = "com.nosferatu.divinemachinerylegacy.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabDivineMachinery();

    public static Block mechanicalRunicAltar;
    public static Block mechanicalManaPool;
    public static Block mechanicalManaInfuser;
    public static Block mechanicalApothecary;
    public static Block mechanicalDaisy;
    public static Block mechanicalIndustrialAgglomerationFactory;
    public static Block mechanicalAlfheimMarket;
    public static Block mechanicalOrechid;
    public static Block jadedAmaranthus;
    public static Block greenhouse;
    public static Block bloodAltarAssembler;

    public static Item catalystManaInfinity;
    public static Item catalystLivingrockInfinity;
    public static Item catalystSeedInfinity;
    public static Item catalystWaterInfinity;
    public static Item catalystStoneInfinity;
    public static Item catalystWoodInfinity;
    public static Item catalystSpeed;
    public static Item catalystPetal;
    public static Item catalystPetalBlock;

    public static final Item[] greenhouseUpgrades = new Item[15];
    public static Item greenhouseHeatUpgrade;

    public static final Item[] bloodAltarTierCards = new Item[4];
    public static Item bloodAltarParallelCard;
    public static Item bloodMagicSpeedCard;

    public static final Item[] materialIngots = new Item[MATERIAL_KEYS.length];
    public static final Item[] materialDragonstones = new Item[MATERIAL_KEYS.length];
    public static final Block[] materialIngotBlocks = new Block[MATERIAL_KEYS.length];
    public static final Block[] materialDragonstoneBlocks = new Block[MATERIAL_KEYS.length];
    public static final Item[] manaSparks = new Item[SparkTier.values().length];

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Registration order is deliberate. NEI 1.7.10 largely follows item/block
        // registry order, so keep each subsystem together instead of interleaving it.
        registerBotaniaMachines();
        registerMaterials();
        registerTieredSparks();
        registerBotaniaSupportItems();

        GaiaRelicsLegacy.registerAll();
        registerBloodMagicSection();

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
        FMLCommonHandler.instance().bus().register(new Ae2InterfaceOutputReturner());
        registerSparkRecipes();
        RecipeRegistrar.registerAll();
        proxy.registerRenderers();
    }

    private void registerBotaniaMachines() {
        mechanicalRunicAltar = new BlockMechanicalRunicAltar();
        GameRegistry.registerBlock(mechanicalRunicAltar, ItemBlockMechanicalRunicAltar.class, "mechanical_runic_altar");
        GameRegistry.registerTileEntity(TileMechanicalRunicAltar.class, MODID + ".mechanical_runic_altar");

        mechanicalManaPool = new BlockMechanicalManaPool();
        GameRegistry.registerBlock(mechanicalManaPool, ItemBlockMechanicalManaPool.class, "mechanical_mana_pool");
        GameRegistry.registerTileEntity(TileMechanicalManaPool.class, MODID + ".mechanical_mana_pool");

        mechanicalManaInfuser = new BlockMechanicalManaInfuser();
        GameRegistry.registerBlock(mechanicalManaInfuser, ItemBlockMechanicalManaInfuser.class, "mechanical_mana_infuser");
        GameRegistry.registerTileEntity(TileMechanicalManaInfuser.class, MODID + ".mechanical_mana_infuser");

        mechanicalApothecary = new BlockMechanicalApothecary();
        GameRegistry.registerBlock(mechanicalApothecary, ItemBlockMechanicalApothecary.class, "mechanical_apothecary");
        GameRegistry.registerTileEntity(TileMechanicalApothecary.class, MODID + ".mechanical_apothecary");

        mechanicalDaisy = new BlockMechanicalDaisy();
        GameRegistry.registerBlock(mechanicalDaisy, ItemBlockMechanicalDaisy.class, "mechanical_daisy");
        GameRegistry.registerTileEntity(TileMechanicalDaisy.class, MODID + ".mechanical_daisy");

        mechanicalIndustrialAgglomerationFactory = new BlockMechanicalIndustrialAgglomerationFactory();
        GameRegistry.registerBlock(mechanicalIndustrialAgglomerationFactory,
                ItemBlockMechanicalIndustrialAgglomerationFactory.class, "mechanical_industrial_agglomeration_factory");
        GameRegistry.registerTileEntity(TileMechanicalIndustrialAgglomerationFactory.class,
                MODID + ".mechanical_industrial_agglomeration_factory");

        mechanicalAlfheimMarket = new BlockMechanicalAlfheimMarket();
        GameRegistry.registerBlock(mechanicalAlfheimMarket, ItemBlockMechanicalAlfheimMarket.class, "mechanical_alfheim_market");
        GameRegistry.registerTileEntity(TileMechanicalAlfheimMarket.class, MODID + ".mechanical_alfheim_market");

        mechanicalOrechid = new BlockMechanicalOrechid();
        GameRegistry.registerBlock(mechanicalOrechid, ItemBlockMechanicalOrechid.class, "mechanical_orechid");
        GameRegistry.registerTileEntity(TileMechanicalOrechid.class, MODID + ".mechanical_orechid");

        jadedAmaranthus = new BlockJadedAmaranthus();
        GameRegistry.registerBlock(jadedAmaranthus, "jaded_amaranthus");
        GameRegistry.registerTileEntity(TileJadedAmaranthus.class, MODID + ".jaded_amaranthus");

        greenhouse = new BlockGreenhouse();
        GameRegistry.registerBlock(greenhouse, "greenhouse");
        GameRegistry.registerTileEntity(TileGreenhouse.class, MODID + ".greenhouse");
    }

    private void registerBotaniaSupportItems() {
        catalystManaInfinity = cat("catalyst_mana_infinity", "catalyst_mana_infinity");
        catalystLivingrockInfinity = cat("catalyst_livingrock_infinity", "catalyst_living_rock_infinity");
        catalystSeedInfinity = cat("catalyst_seed_infinity", "catalyst_seed_infinity");
        catalystWaterInfinity = cat("catalyst_water_infinity", "catalyst_water_infinity");
        catalystStoneInfinity = cat("catalyst_stone_infinity", "catalyst_stone_infinity");
        catalystWoodInfinity = cat("catalyst_wood_infinity", "catalyst_wood_infinity");
        catalystSpeed = cat("catalyst_speed", "catalyst_speed");
        catalystPetal = cat("catalyst_petal", "catalyst_petal");
        catalystPetalBlock = cat("catalyst_petal_block", "catalyst_petal_block_pattern");
        registerGreenhouseUpgrades();
    }

    private void registerBloodMagicSection() {
        bloodAltarAssembler = new BlockBloodAltarAssembler();
        GameRegistry.registerBlock(bloodAltarAssembler, "blood_altar_assembler");
        GameRegistry.registerTileEntity(TileBloodAltarAssembler.class, MODID + ".blood_altar_assembler");

        // Generator and Blood Pattern immediately follow the assembler in the registry.
        BloodMagicContent.register();
        registerBloodMagicItems();
    }

    private Item cat(String key, String texture) {
        Item item = new ItemMachineCatalyst(key, texture);
        GameRegistry.registerItem(item, key);
        return item;
    }

    private void registerGreenhouseUpgrades() {
        greenhouseUpgrades[0] = greenhouseUpgrade("upgrade_flower_4x", 0, 4);
        greenhouseUpgrades[1] = greenhouseUpgrade("upgrade_flower_16x", 0, 16);
        greenhouseUpgrades[2] = greenhouseUpgrade("upgrade_flower_32x", 0, 32);
        greenhouseUpgrades[3] = greenhouseUpgrade("upgrade_flower_64x", 0, 64);
        greenhouseUpgrades[4] = greenhouseUpgrade("upgrade_gen_mana", 1, 1);
        greenhouseUpgrades[5] = greenhouseUpgrade("upgrade_slot_add", 2, 1);
        greenhouseUpgrades[6] = greenhouseUpgrade("upgrade_cost_energy", 3, 1);
        greenhouseUpgrades[7] = greenhouseUpgrade("upgrade_tick_gen_mana_1", 4, 25);
        greenhouseUpgrades[8] = greenhouseUpgrade("upgrade_tick_gen_mana_2", 4, 50);
        greenhouseUpgrades[9] = greenhouseUpgrade("upgrade_storage_energy_1", 5, 10);
        greenhouseUpgrades[10] = greenhouseUpgrade("upgrade_storage_energy_2", 5, 100);
        greenhouseUpgrades[11] = greenhouseUpgrade("upgrade_storage_energy_3", 5, 1000);
        greenhouseUpgrades[12] = greenhouseUpgrade("upgrade_storage_mana_1", 6, 10);
        greenhouseUpgrades[13] = greenhouseUpgrade("upgrade_storage_mana_2", 6, 100);
        greenhouseUpgrades[14] = greenhouseUpgrade("upgrade_storage_mana_3", 6, 1000);
        greenhouseHeatUpgrade = greenhouseUpgrade("upgrade_heat_greenhouse", 7, 1);
    }

    private Item greenhouseUpgrade(String key, int slot, int value) {
        Item item = new ItemGreenhouseUpgrade(key, slot, value);
        GameRegistry.registerItem(item, key);
        return item;
    }

    private void registerBloodMagicItems() {
        for (int tier = 2; tier <= 5; tier++) {
            Item item = new ItemBloodAltarTierCard(tier);
            bloodAltarTierCards[tier - 2] = item;
            GameRegistry.registerItem(item, "blood_altar_tier_card_" + tier);
        }

        bloodAltarParallelCard = new ItemBloodMachineUpgrade("blood_altar_parallel_card",
                ItemBloodMachineUpgrade.Type.PARALLEL);
        bloodMagicSpeedCard = new ItemBloodMachineUpgrade("blood_magic_speed_card",
                ItemBloodMachineUpgrade.Type.SPEED);
        GameRegistry.registerItem(bloodAltarParallelCard, "blood_altar_parallel_card");
        GameRegistry.registerItem(bloodMagicSpeedCard, "blood_magic_speed_card");
    }

    private void registerTieredSparks() {
        for (SparkTier tier : SparkTier.values()) {
            Item spark = new ItemTieredManaSpark(tier);
            manaSparks[tier.ordinal()] = spark;
            GameRegistry.registerItem(spark, tier.getKey() + "_spark");
        }
        EntityRegistry.registerModEntity(EntityTieredManaSpark.class, "tiered_mana_spark", 1, INSTANCE, 64, 1, true);
    }

    private void registerSparkRecipes() {
        GameRegistry.addShapelessRecipe(new ItemStack(manaSparks[SparkTier.BASE.ordinal()]),
                new ItemStack(vazkii.botania.common.item.ModItems.spark));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(manaSparks[1]), 50000,
                rune(0), rune(1), rune(2), rune(3), rune(8), new ItemStack(manaSparks[0]),
                new ItemStack(materialIngotBlocks[0]), new ItemStack(materialIngots[0]), new ItemStack(materialDragonstoneBlocks[0]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(manaSparks[2]), 150000,
                rune(4), rune(5), rune(6), rune(7), rune(8), new ItemStack(manaSparks[1]),
                new ItemStack(materialIngotBlocks[1]), new ItemStack(materialIngots[1]), new ItemStack(materialDragonstoneBlocks[1]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(manaSparks[3]), 500000,
                rune(14), rune(11), rune(12), rune(10), rune(8), new ItemStack(manaSparks[2]),
                new ItemStack(materialIngotBlocks[2]), new ItemStack(materialIngots[2]), new ItemStack(materialDragonstoneBlocks[2]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(manaSparks[4]), 1000000,
                rune(9), rune(15), rune(13), rune(8), new ItemStack(manaSparks[3]),
                new ItemStack(materialIngotBlocks[3]), new ItemStack(materialIngots[3]), new ItemStack(materialDragonstoneBlocks[3]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(manaSparks[5]), 2000000,
                rune(9), rune(15), rune(13), rune(8), new ItemStack(manaSparks[4]),
                new ItemStack(materialIngotBlocks[5]), new ItemStack(materialIngots[5]), new ItemStack(materialDragonstoneBlocks[5]));
        BotaniaAPI.registerRuneAltarRecipe(new ItemStack(manaSparks[6]), 2500000,
                rune(14), rune(11), rune(12), rune(10), rune(8), new ItemStack(manaSparks[5]),
                new ItemStack(materialIngotBlocks[6]), new ItemStack(materialIngots[6]), new ItemStack(materialDragonstoneBlocks[6]));
    }

    private static ItemStack rune(int meta) {
        return new ItemStack(vazkii.botania.common.item.ModItems.rune, 1, meta);
    }

    private void registerMaterials() {
        for (int i = 0; i < MATERIAL_KEYS.length; i++) {
            String material = MATERIAL_KEYS[i];
            String ingotKey = material + "_ingot";
            String dragonstoneKey = material + "_dragonstone";
            String ingotBlockKey = material + "_ingot_block";
            String dragonstoneBlockKey = material + "_dragonstone_block";
            materialIngots[i] = new ItemReforkedMaterial(ingotKey);
            materialDragonstones[i] = new ItemReforkedMaterial(dragonstoneKey);
            materialIngotBlocks[i] = new BlockReforkedMaterial(ingotBlockKey, true);
            materialDragonstoneBlocks[i] = new BlockReforkedMaterial(dragonstoneBlockKey, false);
            GameRegistry.registerItem(materialIngots[i], ingotKey);
            GameRegistry.registerItem(materialDragonstones[i], dragonstoneKey);
            GameRegistry.registerBlock(materialIngotBlocks[i], ingotBlockKey);
            GameRegistry.registerBlock(materialDragonstoneBlocks[i], dragonstoneBlockKey);

            String suffix = Character.toUpperCase(material.charAt(0)) + material.substring(1);
            OreDictionary.registerOre("ingot" + suffix, new ItemStack(materialIngots[i]));
            OreDictionary.registerOre("block" + suffix, new ItemStack(materialIngotBlocks[i]));
            OreDictionary.registerOre("gem" + suffix + "Dragonstone", new ItemStack(materialDragonstones[i]));
            OreDictionary.registerOre("block" + suffix + "Dragonstone", new ItemStack(materialDragonstoneBlocks[i]));

            GameRegistry.addRecipe(new ItemStack(materialIngotBlocks[i]), "III", "III", "III", 'I', materialIngots[i]);
            GameRegistry.addShapelessRecipe(new ItemStack(materialIngots[i], 9), materialIngotBlocks[i]);
            GameRegistry.addRecipe(new ItemStack(materialDragonstoneBlocks[i]), "DDD", "DDD", "DDD", 'D', materialDragonstones[i]);
            GameRegistry.addShapelessRecipe(new ItemStack(materialDragonstones[i], 9), materialDragonstoneBlocks[i]);
        }
    }
}
