package com.nosferatu.divinemachinerylegacy;

import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.block.BlockReforkedMaterial;
import com.nosferatu.divinemachinerylegacy.block.ItemBlockMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.gui.GuiHandler;
import com.nosferatu.divinemachinerylegacy.item.ItemMachineCatalyst;
import com.nosferatu.divinemachinerylegacy.item.ItemReforkedMaterial;
import com.nosferatu.divinemachinerylegacy.proxy.CommonProxy;
import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

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
    public static Item catalystManaInfinity;
    public static Item catalystLivingrockInfinity;

    public static final Item[] materialIngots = new Item[MATERIAL_KEYS.length];
    public static final Item[] materialDragonstones = new Item[MATERIAL_KEYS.length];
    public static final Block[] materialIngotBlocks = new Block[MATERIAL_KEYS.length];
    public static final Block[] materialDragonstoneBlocks = new Block[MATERIAL_KEYS.length];

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        registerMaterials();

        catalystManaInfinity = new ItemMachineCatalyst("catalyst_mana_infinity", "catalyst_mana_infinity");
        catalystLivingrockInfinity = new ItemMachineCatalyst("catalyst_livingrock_infinity", "catalyst_living_rock_infinity");
        GameRegistry.registerItem(catalystManaInfinity, "catalyst_mana_infinity");
        GameRegistry.registerItem(catalystLivingrockInfinity, "catalyst_livingrock_infinity");

        mechanicalRunicAltar = new BlockMechanicalRunicAltar();
        GameRegistry.registerBlock(mechanicalRunicAltar, ItemBlockMechanicalRunicAltar.class, "mechanical_runic_altar");
        GameRegistry.registerTileEntity(TileMechanicalRunicAltar.class, MODID + ".mechanical_runic_altar");
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());

        proxy.registerRenderers();
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
