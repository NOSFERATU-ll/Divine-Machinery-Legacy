package com.nosferatu.divinemachinerylegacy;

import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalManaPool;
import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.block.BlockReforkedMaterial;
import com.nosferatu.divinemachinerylegacy.block.ItemBlockMechanicalManaPool;
import com.nosferatu.divinemachinerylegacy.block.ItemBlockMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.botania.SparkTier;
import com.nosferatu.divinemachinerylegacy.entity.EntityTieredManaSpark;
import com.nosferatu.divinemachinerylegacy.gui.GuiHandler;
import com.nosferatu.divinemachinerylegacy.item.ItemMachineCatalyst;
import com.nosferatu.divinemachinerylegacy.item.ItemReforkedMaterial;
import com.nosferatu.divinemachinerylegacy.item.ItemTieredManaSpark;
import com.nosferatu.divinemachinerylegacy.proxy.CommonProxy;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalManaPool;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
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

@Mod(
        modid = DivineMachineryLegacy.MODID,
        name = DivineMachineryLegacy.NAME,
        version = DivineMachineryLegacy.VERSION,
        dependencies = "required-after:Botania;required-after:appliedenergistics2"
)
public class DivineMachineryLegacy {

    public static final String MODID = "divinemachinerylegacy";
    public static final String NAME = "Divine Machinery Legacy";
    public static final String VERSION = "0.1.0-dev";

    public static final int GUI_RUNIC_ALTAR = 1;
    public static final int GUI_MANA_POOL = 2;

    public static final String[] MATERIAL_KEYS = {
            "malachite", "saffron", "shadow", "crimson", "crystal", "aureate", "mazarine"
    };

    @Mod.Instance(MODID)
    public static DivineMachineryLegacy INSTANCE;

    @SidedProxy(
            clientSide = "com.nosferatu.divinemachinerylegacy.proxy.ClientProxy",
            serverSide = "com.nosferatu.divinemachinerylegacy.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    public static final CreativeTabs CREATIVE_TAB = new CreativeTabDivineMachinery();

    public static Block mechanicalRunicAltar;
    public static Block mechanicalManaPool;
    public static Item catalystManaInfinity;
    public static Item catalystLivingrockInfinity;

    public static final Item[] materialIngots = new Item[MATERIAL_KEYS.length];
    public static final Item[] materialDragonstones = new Item[MATERIAL_KEYS.length];
    public static final Block[] materialIngotBlocks = new Block[MATERIAL_KEYS.length];
    public static final Block[] materialDragonstoneBlocks = new Block[MATERIAL_KEYS.length];

    /** Base, Malachite, Saffron, Shadow, Crimson, Aureate, Mazarine. */
    public static final Item[] manaSparks = new Item[SparkTier.values().length];

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        registerMaterials();
        registerTieredSparks();

        catalystManaInfinity = new ItemMachineCatalyst("catalyst_mana_infinity", "catalyst_mana_infinity");
        catalystLivingrockInfinity = new ItemMachineCatalyst("catalyst_livingrock_infinity", "catalyst_living_rock_infinity");
        GameRegistry.registerItem(catalystManaInfinity, "catalyst_mana_infinity");
        GameRegistry.registerItem(catalystLivingrockInfinity, "catalyst_livingrock_infinity");

        mechanicalRunicAltar = new BlockMechanicalRunicAltar();
        GameRegistry.registerBlock(mechanicalRunicAltar, ItemBlockMechanicalRunicAltar.class, "mechanical_runic_altar");
        GameRegistry.registerTileEntity(TileMechanicalRunicAltar.class, MODID + ".mechanical_runic_altar");

        mechanicalManaPool = new BlockMechanicalManaPool();
        GameRegistry.registerBlock(mechanicalManaPool, ItemBlockMechanicalManaPool.class, "mechanical_mana_pool");
        GameRegistry.registerTileEntity(TileMechanicalManaPool.class, MODID + ".mechanical_mana_pool");

        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());

        registerSparkRecipes();
        proxy.registerRenderers();
    }

    private void registerTieredSparks() {
        for (SparkTier tier : SparkTier.values()) {
            Item spark = new ItemTieredManaSpark(tier);
            manaSparks[tier.ordinal()] = spark;
            GameRegistry.registerItem(spark, tier.getKey() + "_spark");
        }

        EntityRegistry.registerModEntity(EntityTieredManaSpark.class,
                "tiered_mana_spark", 1, INSTANCE, 64, 1, true);
    }

    private void registerSparkRecipes() {
        GameRegistry.addShapelessRecipe(
                new ItemStack(manaSparks[SparkTier.BASE.ordinal()]),
                new ItemStack(vazkii.botania.common.item.ModItems.spark));

        BotaniaAPI.registerRuneAltarRecipe(
                new ItemStack(manaSparks[SparkTier.MALACHITE.ordinal()]), 50000,
                rune(0), rune(1), rune(2), rune(3), rune(8),
                new ItemStack(manaSparks[SparkTier.BASE.ordinal()]),
                new ItemStack(materialIngotBlocks[0]), new ItemStack(materialIngots[0]),
                new ItemStack(materialDragonstoneBlocks[0]));

        BotaniaAPI.registerRuneAltarRecipe(
                new ItemStack(manaSparks[SparkTier.SAFFRON.ordinal()]), 150000,
                rune(4), rune(5), rune(6), rune(7), rune(8),
                new ItemStack(manaSparks[SparkTier.MALACHITE.ordinal()]),
                new ItemStack(materialIngotBlocks[1]), new ItemStack(materialIngots[1]),
                new ItemStack(materialDragonstoneBlocks[1]));

        BotaniaAPI.registerRuneAltarRecipe(
                new ItemStack(manaSparks[SparkTier.SHADOW.ordinal()]), 500000,
                rune(14), rune(11), rune(12), rune(10), rune(8),
                new ItemStack(manaSparks[SparkTier.SAFFRON.ordinal()]),
                new ItemStack(materialIngotBlocks[2]), new ItemStack(materialIngots[2]),
                new ItemStack(materialDragonstoneBlocks[2]));

        BotaniaAPI.registerRuneAltarRecipe(
                new ItemStack(manaSparks[SparkTier.CRIMSON.ordinal()]), 1000000,
                rune(9), rune(15), rune(13), rune(8),
                new ItemStack(manaSparks[SparkTier.SHADOW.ordinal()]),
                new ItemStack(materialIngotBlocks[3]), new ItemStack(materialIngots[3]),
                new ItemStack(materialDragonstoneBlocks[3]));

        BotaniaAPI.registerRuneAltarRecipe(
                new ItemStack(manaSparks[SparkTier.AUREATE.ordinal()]), 2000000,
                rune(9), rune(15), rune(13), rune(8),
                new ItemStack(manaSparks[SparkTier.CRIMSON.ordinal()]),
                new ItemStack(materialIngotBlocks[5]), new ItemStack(materialIngots[5]),
                new ItemStack(materialDragonstoneBlocks[5]));

        BotaniaAPI.registerRuneAltarRecipe(
                new ItemStack(manaSparks[SparkTier.MAZARINE.ordinal()]), 2500000,
                rune(14), rune(11), rune(12), rune(10), rune(8),
                new ItemStack(manaSparks[SparkTier.AUREATE.ordinal()]),
                new ItemStack(materialIngotBlocks[6]), new ItemStack(materialIngots[6]),
                new ItemStack(materialDragonstoneBlocks[6]));
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

            registerMaterialOreDictionary(material, i);
            registerCompressionRecipes(i);
        }
    }

    private void registerMaterialOreDictionary(String material, int index) {
        String suffix = Character.toUpperCase(material.charAt(0)) + material.substring(1);
        OreDictionary.registerOre("ingot" + suffix, new ItemStack(materialIngots[index]));
        OreDictionary.registerOre("block" + suffix, new ItemStack(materialIngotBlocks[index]));
        OreDictionary.registerOre("gem" + suffix + "Dragonstone", new ItemStack(materialDragonstones[index]));
        OreDictionary.registerOre("block" + suffix + "Dragonstone", new ItemStack(materialDragonstoneBlocks[index]));
    }

    private void registerCompressionRecipes(int index) {
        Item ingot = materialIngots[index];
        Item dragonstone = materialDragonstones[index];
        Block ingotBlock = materialIngotBlocks[index];
        Block dragonstoneBlock = materialDragonstoneBlocks[index];

        GameRegistry.addRecipe(new ItemStack(ingotBlock),
                "III", "III", "III", 'I', ingot);
        GameRegistry.addShapelessRecipe(new ItemStack(ingot, 9), ingotBlock);

        GameRegistry.addRecipe(new ItemStack(dragonstoneBlock),
                "DDD", "DDD", "DDD", 'D', dragonstone);
        GameRegistry.addShapelessRecipe(new ItemStack(dragonstone, 9), dragonstoneBlock);
    }
}
