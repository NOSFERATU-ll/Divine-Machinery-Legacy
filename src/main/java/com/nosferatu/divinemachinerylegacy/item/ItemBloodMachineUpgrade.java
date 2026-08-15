package com.nosferatu.divinemachinerylegacy.item;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

/** Speed/parallel upgrade card used by the Blood Altar Assembler. */
public class ItemBloodMachineUpgrade extends Item {
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
        // bmaddon exposes at most four parallel cards and nine dedicated
        // Blood Magic speed cards to this machine.
        setMaxStackSize(type == Type.PARALLEL ? 4 : 9);
    }

    public Type getType() {
        return type;
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
