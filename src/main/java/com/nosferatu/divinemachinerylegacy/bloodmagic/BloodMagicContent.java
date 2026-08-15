package com.nosferatu.divinemachinerylegacy.bloodmagic;

import WayofTime.alchemicalWizardry.ModItems;
import appeng.api.AEApi;
import com.nosferatu.divinemachinerylegacy.block.BlockBloodGenerator;
import com.nosferatu.divinemachinerylegacy.config.BloodMagicAddonConfig;
import com.nosferatu.divinemachinerylegacy.item.ItemBloodAltarPattern;
import com.nosferatu.divinemachinerylegacy.recipe.RecipeEncodeBloodPattern;
import com.nosferatu.divinemachinerylegacy.tile.TileBloodGenerator;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.io.File;

/**
 * Registration point for content that belongs to the BloodMagic Additions port.
 * Kept separate from the older Botania bootstrap so the entire bmaddon port can
 * be moved/expanded without turning the main mod class into another monolith.
 */
public final class BloodMagicContent {
    public static final int GUI_BLOOD_GENERATOR = 12;

    public static Block bloodGenerator;
    public static Item bloodAltarPattern;

    private static boolean registered;

    private BloodMagicContent() { }

    public static void register() {
        if (registered) return;
        registered = true;

        BloodMagicAddonConfig.loadFromDirectory(new File("config"));

        bloodGenerator = new BlockBloodGenerator();
        GameRegistry.registerBlock(bloodGenerator, "blood_generator");
        GameRegistry.registerTileEntity(TileBloodGenerator.class,
                "divinemachinerylegacy.blood_generator");

        bloodAltarPattern = new ItemBloodAltarPattern();
        GameRegistry.registerItem(bloodAltarPattern, "blood_altar_pattern");

        ItemStack aeBlankPattern = AEApi.instance().definitions().materials().blankPattern().maybeStack(1).orNull();
        if (aeBlankPattern != null && ModItems.weakBloodShard != null) {
            // Exact bmaddon recipe: four Weak Blood Shards around one AE2 blank pattern.
            GameRegistry.addRecipe(new ItemStack(bloodAltarPattern),
                    " S ", "SPS", " S ",
                    'S', new ItemStack(ModItems.weakBloodShard),
                    'P', aeBlankPattern);
        }

        // AE2 rv3 cannot accept a foreign blank pattern in its Pattern Terminal.
        // The bridge recipe copies a normal AE2 processing pattern into the
        // Blood Pattern while this event handler gives the source pattern back.
        GameRegistry.addRecipe(new RecipeEncodeBloodPattern());
        FMLCommonHandler.instance().bus().register(new BloodPatternCraftingHandler());
    }
}
