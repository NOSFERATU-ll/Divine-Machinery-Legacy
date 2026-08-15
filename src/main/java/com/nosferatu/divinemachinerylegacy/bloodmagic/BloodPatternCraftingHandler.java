package com.nosferatu.divinemachinerylegacy.bloodmagic;

import appeng.api.AEApi;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.item.ItemStack;

/** Keeps the AE2 source pattern when the 1.7.10 Blood Pattern encoder is used. */
public final class BloodPatternCraftingHandler {
    @SubscribeEvent
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event == null || event.player == null || event.crafting == null || event.craftMatrix == null) return;
        if (BloodMagicContent.bloodAltarPattern == null
                || event.crafting.getItem() != BloodMagicContent.bloodAltarPattern
                || !BloodMagicPatternData.isEncoded(event.crafting)) return;

        for (int slot = 0; slot < event.craftMatrix.getSizeInventory(); slot++) {
            ItemStack stack = event.craftMatrix.getStackInSlot(slot);
            if (stack == null || !AEApi.instance().definitions().items().encodedPattern().isSameAs(stack)) continue;

            ItemStack returned = stack.copy();
            returned.stackSize = 1;
            if (!event.player.inventory.addItemStackToInventory(returned)) {
                event.player.dropPlayerItemWithRandomChoice(returned, false);
            }
            event.player.inventory.markDirty();
            return;
        }
    }
}
