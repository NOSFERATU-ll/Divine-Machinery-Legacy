package com.nosferatu.divinemachinerylegacy.item;

import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Speed/parallel upgrade card used by the Blood Altar Assembler. */
public class ItemBloodMachineUpgrade extends Item implements IUpgradeModule {
    public enum Type {
        PARALLEL,
        SPEED
    }

    private final Type type;

    public ItemBloodMachineUpgrade(String key, Type type) {
        this.type = type;
        setUnlocalizedName(DivineMachineryLegacy.MODID + "." + key);
        setTextureName(DivineMachineryLegacy.MODID + ":bloodmagic/" + key);
        setCreativeTab(DivineMachineryLegacy.CREATIVE_TAB);
        // The modern items keep the normal item stack size; upgrade slots
        // themselves have a stack limit of one.
        setMaxStackSize(64);
    }

    public Type getType() {
        return type;
    }

    /**
     * AE2 rv3 has a simpler extension point than modern AE2: any item that
     * implements IUpgradeModule can advertise an existing upgrade type. This
     * is the 1.7.10 equivalent of bmaddon's supportsUpgrade mixin.
     */
    @Override
    public Upgrades getType(ItemStack itemstack) {
        return type == Type.SPEED ? Upgrades.SPEED : null;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        String key = type == Type.PARALLEL
                ? "tooltip.divinemachinerylegacy.blood_altar_parallel_card.description"
                : "tooltip.divinemachinerylegacy.blood_magic_speed_card.description";
        lines.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal(key));
    }
}
