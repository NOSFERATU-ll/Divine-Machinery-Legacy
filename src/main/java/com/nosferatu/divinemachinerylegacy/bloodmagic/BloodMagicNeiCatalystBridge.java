package com.nosferatu.divinemachinerylegacy.bloodmagic;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Method;

/**
 * Optional NEI bridge with no hard NEI dependency.
 *
 * BloodMagic Additions registers the Blood Altar Assembler as a JEI catalyst
 * for Blood Altar and Alchemy Table recipes. Blood Magic 1.7.10 exposes those
 * same recipe views in NEI under "alchemicalwizardry.altar" and
 * "alchemicalwizardry.alchemy". Register the machine for both when NEI exists.
 */
public final class BloodMagicNeiCatalystBridge {
    private boolean attempted;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (attempted || event.phase != TickEvent.Phase.END) return;
        attempted = true;

        if (DivineMachineryLegacy.bloodAltarAssembler == null) return;

        try {
            Class<?> api = Class.forName("codechicken.nei.api.API");
            Method addRecipeCatalyst = api.getMethod(
                    "addRecipeCatalyst", ItemStack.class, String.class);

            ItemStack assembler = new ItemStack(DivineMachineryLegacy.bloodAltarAssembler);
            addRecipeCatalyst.invoke(null, assembler.copy(), "alchemicalwizardry.altar");
            addRecipeCatalyst.invoke(null, assembler.copy(), "alchemicalwizardry.alchemy");
        } catch (ClassNotFoundException ignored) {
            // NEI is optional; nothing to do on installations without it.
        } catch (Throwable error) {
            System.err.println("[Divine Machinery Legacy] Could not register Blood Assembler NEI catalysts: "
                    + error.getMessage());
        }
    }
}
