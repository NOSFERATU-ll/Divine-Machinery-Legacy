package com.nosferatu.divinemachinerylegacy.bloodmagic;

import appeng.api.config.Upgrades;
import appeng.parts.automation.UpgradeInventory;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.item.ItemStack;

/**
 * Runtime half of bmaddon's AE2 speed-card mixins.
 *
 * AE2 rv3 already accepts arbitrary IUpgradeModule items, so our Blood Magic
 * Speed Card can advertise itself as Upgrades.SPEED without patching AE2's
 * support registry. The only missing modern behavior is its weight: one Blood
 * Magic Speed Card counts as four ordinary speed cards, capped by the host's
 * normal SPEED limit. The core transformer routes UpgradeInventory's final
 * count through this helper.
 */
public final class BloodMagicAe2SpeedIntegration {
    private BloodMagicAe2SpeedIntegration() { }

    public static int adjustInstalledSpeed(UpgradeInventory inventory, Upgrades upgrade, int vanillaCount) {
        if (inventory == null || upgrade != Upgrades.SPEED || vanillaCount <= 0) {
            return vanillaCount;
        }
        if (DivineMachineryLegacy.bloodMagicSpeedCard == null) {
            return vanillaCount;
        }

        int bloodCards = 0;
        for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() == DivineMachineryLegacy.bloodMagicSpeedCard) {
                // AE2 rv3's UpgradeInventory has a hard stack limit of one, but
                // count the stack defensively in case another host subclasses it.
                bloodCards += Math.max(1, stack.stackSize);
            }
        }
        if (bloodCards <= 0) return vanillaCount;

        // Vanilla has already counted each Blood card once because it implements
        // IUpgradeModule(SPEED). Add the missing three equivalents per card.
        long effective = (long) vanillaCount + (long) bloodCards * 3L;
        int hostLimit = Math.max(0, inventory.getMaxInstalled(Upgrades.SPEED));
        return (int) Math.min((long) hostLimit, effective);
    }
}
