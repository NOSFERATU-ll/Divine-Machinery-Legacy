package com.nosferatu.divinemachinerylegacy;

import com.nosferatu.divinemachinerylegacy.block.BlockMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.block.ItemBlockMechanicalRunicAltar;
import com.nosferatu.divinemachinerylegacy.gui.GuiHandler;
import com.nosferatu.divinemachinerylegacy.item.ItemMachineCatalyst;
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

@Mod(
        modid = DivineMachineryLegacy.MODID,
        name = DivineMachineryLegacy.NAME,
        version = DivineMachineryLegacy.VERSION,
        dependencies = "required-after:Botania;after:appliedenergistics2"
)
public class DivineMachineryLegacy {

    public static final String MODID = "divinemachinerylegacy";
    public static final String NAME = "Divine Machinery Legacy";
    public static final String VERSION = "0.1.0-dev";

    public static final int GUI_RUNIC_ALTAR = 1;

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

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        catalystManaInfinity = new ItemMachineCatalyst("catalyst_mana_infinity", "minecraft:nether_star");
        catalystLivingrockInfinity = new ItemMachineCatalyst("catalyst_livingrock_infinity", "minecraft:quartz");
        GameRegistry.registerItem(catalystManaInfinity, "catalyst_mana_infinity");
        GameRegistry.registerItem(catalystLivingrockInfinity, "catalyst_livingrock_infinity");

        mechanicalRunicAltar = new BlockMechanicalRunicAltar();
        GameRegistry.registerBlock(mechanicalRunicAltar, ItemBlockMechanicalRunicAltar.class, "mechanical_runic_altar");
        GameRegistry.registerTileEntity(TileMechanicalRunicAltar.class, MODID + ".mechanical_runic_altar");
        NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());
    }
}
