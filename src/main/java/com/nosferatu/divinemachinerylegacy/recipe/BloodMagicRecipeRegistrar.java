package com.nosferatu.divinemachinerylegacy.recipe;

import WayofTime.alchemicalWizardry.ModBlocks;
import WayofTime.alchemicalWizardry.ModItems;
import WayofTime.alchemicalWizardry.api.altarRecipeRegistry.AltarRecipeRegistry;
import appeng.api.AEApi;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicContent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;

/** Registers the BloodMagic Additions port and its altar progression against Blood Magic 1.7.10. */
public final class BloodMagicRecipeRegistrar {
    private BloodMagicRecipeRegistrar() { }

    public static void register() {
        BloodMagicContent.register();

        // bmaddon itself ships no recipe for the Blood Generator. DML gives it
        // a deliberate late-midgame recipe matching what the machine actually does:
        // a dense AE2 power core, Blood Magic capacity/dislocation runes for its
        // Life Essence buffer/output, and Weak Blood Shards as the blood-tech gate.
        ItemStack denseEnergyCell = AEApi.instance().definitions().blocks()
                .energyCellDense().maybeStack(1).orNull();
        if (denseEnergyCell != null && ModItems.weakBloodShard != null) {
            GameRegistry.addRecipe(new ItemStack(BloodMagicContent.bloodGenerator),
                    "WCW", "DED", "WCW",
                    'W', new ItemStack(ModItems.weakBloodShard),
                    'C', new ItemStack(ModBlocks.bloodRune, 1, 4),
                    'D', new ItemStack(ModBlocks.bloodRune, 1, 2),
                    'E', denseEnergyCell);
        }

        // Modern Blood Magic stores altar upgradeLevel zero-based. In 1.7.10
        // AltarRecipeRegistry expects the real altar tier, hence +1 here.
        register(new ItemStack(DivineMachineryLegacy.bloodAltarTierCards[0]),
                new ItemStack(ModBlocks.bloodRune, 1, 1),
                2, 2000, 10, 10);
        register(new ItemStack(DivineMachineryLegacy.bloodAltarTierCards[1]),
                new ItemStack(DivineMachineryLegacy.bloodAltarTierCards[0]),
                3, 5000, 15, 15);
        register(new ItemStack(DivineMachineryLegacy.bloodAltarTierCards[2]),
                new ItemStack(DivineMachineryLegacy.bloodAltarTierCards[1]),
                4, 10000, 20, 20);
        register(new ItemStack(DivineMachineryLegacy.bloodAltarTierCards[3]),
                new ItemStack(DivineMachineryLegacy.bloodAltarTierCards[2]),
                5, 20000, 25, 25);

        ItemStack molecularAssembler = AEApi.instance().definitions().blocks()
                .molecularAssembler().maybeStack(1).orNull();
        if (molecularAssembler != null) {
            register(new ItemStack(DivineMachineryLegacy.bloodAltarAssembler), molecularAssembler,
                    5, 20000, 50, 50);
        }

        ItemStack capacityCard = AEApi.instance().definitions().materials()
                .cardCapacity().maybeStack(1).orNull();
        if (capacityCard != null) {
            register(new ItemStack(DivineMachineryLegacy.bloodAltarParallelCard), capacityCard,
                    3, 5000, 15, 15);
        }

        ItemStack speedCard = AEApi.instance().definitions().materials()
                .cardSpeed().maybeStack(1).orNull();
        if (speedCard != null) {
            register(new ItemStack(DivineMachineryLegacy.bloodMagicSpeedCard), speedCard,
                    4, 10000, 20, 20);
        }
    }

    private static void register(ItemStack output, ItemStack input, int minimumTier,
                                 int lp, int consumptionRate, int drainRate) {
        AltarRecipeRegistry.registerAltarRecipe(output, input, minimumTier,
                lp, consumptionRate, drainRate, false);
    }
}
