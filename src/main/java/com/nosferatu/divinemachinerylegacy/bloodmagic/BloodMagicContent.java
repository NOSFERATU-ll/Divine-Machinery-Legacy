package com.nosferatu.divinemachinerylegacy.bloodmagic;

import com.nosferatu.divinemachinerylegacy.block.BlockBloodGenerator;
import com.nosferatu.divinemachinerylegacy.config.BloodMagicAddonConfig;
import com.nosferatu.divinemachinerylegacy.tile.TileBloodGenerator;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;

import java.io.File;

/**
 * Registration point for content that belongs to the BloodMagic Additions port.
 * Kept separate from the older Botania bootstrap so the entire bmaddon port can
 * be moved/expanded without turning the main mod class into another monolith.
 */
public final class BloodMagicContent {
    public static final int GUI_BLOOD_GENERATOR = 12;

    public static Block bloodGenerator;

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
    }
}
