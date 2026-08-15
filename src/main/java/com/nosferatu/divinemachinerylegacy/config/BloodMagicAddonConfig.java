package com.nosferatu.divinemachinerylegacy.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * 1.7.10 equivalent of bmaddon-common.toml.
 * Defaults intentionally match BloodMagic Additions 1.0.4.
 */
public final class BloodMagicAddonConfig {
    public static int bloodGeneratorEnergyCapacity = 100000;
    public static int bloodGeneratorMaxEnergyInput = 2000;
    public static int bloodGeneratorLifeTankCapacity = 16000;
    public static int bloodGeneratorEnergyPerOperation = 1000;
    public static int bloodGeneratorLifeEssencePerOperation = 10;
    public static int bloodGeneratorWorkIntervalTicks = 1;
    public static boolean bloodGeneratorAutoOutput = true;
    public static int bloodGeneratorMaxFluidOutputPerTick = 100;

    public static int bloodAltarAssemblerBaseCraftTimeTicks = 200;
    public static int bloodAltarAssemblerMinCraftTimeTicks = 20;
    public static double bloodAltarAssemblerAePerTickBase = 8.0D;
    public static double bloodAltarAssemblerAePerTickPerAccelerationCard = 12.0D;
    public static double bloodAltarAssemblerLifeEssenceMultiplier = 1.0D;
    public static int bloodAltarAssemblerBaseParallelCrafts = 1;
    public static int bloodAltarAssemblerParallelCraftsPerCard = 2;
    public static int bloodAltarAssemblerMaxParallelCrafts = 8;

    private BloodMagicAddonConfig() { }

    public static void load(File suggestedConfigurationFile) {
        File parent = suggestedConfigurationFile.getParentFile();
        File file = new File(parent, "divinemachinerylegacy-bloodmagic.cfg");
        Configuration config = new Configuration(file);
        try {
            config.load();

            String generator = "blood_generator";
            bloodGeneratorEnergyCapacity = config.getInt("energyCapacity", generator, 100000, 1, Integer.MAX_VALUE,
                    "Maximum RF stored by the Blood Generator.");
            bloodGeneratorMaxEnergyInput = config.getInt("maxEnergyInput", generator, 2000, 1, Integer.MAX_VALUE,
                    "Maximum RF accepted per insertion.");
            bloodGeneratorLifeTankCapacity = config.getInt("lifeTankCapacity", generator, 16000, 1, Integer.MAX_VALUE,
                    "Maximum Life Essence stored by the Blood Generator, in mB.");
            bloodGeneratorEnergyPerOperation = config.getInt("energyPerOperation", generator, 1000, 1, Integer.MAX_VALUE,
                    "RF consumed per generation operation.");
            bloodGeneratorLifeEssencePerOperation = config.getInt("lifeEssencePerOperation", generator, 10, 1, Integer.MAX_VALUE,
                    "Life Essence generated per operation, in mB.");
            bloodGeneratorWorkIntervalTicks = config.getInt("workIntervalTicks", generator, 1, 1, 1200,
                    "How often the generator runs. 1 = every tick, 20 = once per second.");
            bloodGeneratorAutoOutput = config.getBoolean("autoOutput", generator, true,
                    "If true, the Blood Generator pushes Life Essence into adjacent fluid handlers.");
            bloodGeneratorMaxFluidOutputPerTick = config.getInt("maxFluidOutputPerTick", generator, 100, 1, Integer.MAX_VALUE,
                    "Maximum Life Essence pushed to adjacent fluid handlers per tick, in mB.");

            String assembler = "blood_altar_assembler";
            bloodAltarAssemblerBaseCraftTimeTicks = config.getInt("baseCraftTimeTicks", assembler, 200, 1, 72000,
                    "Base craft time for Blood Altar Assembler, in ticks.");
            bloodAltarAssemblerMinCraftTimeTicks = config.getInt("minCraftTimeTicks", assembler, 20, 1, 72000,
                    "Minimum craft time after acceleration cards, in ticks.");
            bloodAltarAssemblerAePerTickBase = config.get(assembler, "aePerTickBase", 8.0D,
                    "Base AE energy consumed per active craft each tick.", 0.0D, Double.MAX_VALUE).getDouble();
            bloodAltarAssemblerAePerTickPerAccelerationCard = config.get(assembler, "aePerTickPerAccelerationCard", 12.0D,
                    "Additional AE energy consumed per active craft each tick per acceleration card.", 0.0D, Double.MAX_VALUE).getDouble();
            bloodAltarAssemblerLifeEssenceMultiplier = config.get(assembler, "lifeEssenceMultiplier", 1.0D,
                    "Multiplier for Life Essence required by Blood Altar recipes.", 0.0D, Double.MAX_VALUE).getDouble();
            bloodAltarAssemblerBaseParallelCrafts = config.getInt("baseParallelCrafts", assembler, 1, 1, 64,
                    "How many crafts the Blood Altar Assembler can run without parallel cards.");
            bloodAltarAssemblerParallelCraftsPerCard = config.getInt("parallelCraftsPerCard", assembler, 2, 0, 64,
                    "How many active crafts each Blood Altar Parallel Card allows.");
            bloodAltarAssemblerMaxParallelCrafts = config.getInt("maxParallelCrafts", assembler, 8, 1, 64,
                    "Hard cap for active crafts in one Blood Altar Assembler.");
        } finally {
            if (config.hasChanged()) config.save();
        }
    }
}
