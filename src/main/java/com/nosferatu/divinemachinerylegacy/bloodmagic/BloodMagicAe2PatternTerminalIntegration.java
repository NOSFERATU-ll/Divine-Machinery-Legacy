package com.nosferatu.divinemachinerylegacy.bloodmagic;

import appeng.api.AEApi;
import appeng.container.implementations.ContainerPatternTerm;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

/** 1.7.10 equivalent of bmaddon's Pattern Encoding Terminal mixins. */
public final class BloodMagicAe2PatternTerminalIntegration {
    private BloodMagicAe2PatternTerminalIntegration() { }

    /** Called only after AE2 has performed its ordinary restricted-slot checks. */
    public static boolean acceptAsBlankPattern(boolean vanillaResult, ItemStack stack) {
        return vanillaResult || isBlankBloodPattern(stack);
    }

    /**
     * Handles ContainerPatternTerm#encode when a Blood Pattern is being used.
     * Returning true tells the transformer to skip AE2's normal encoder.
     */
    public static boolean tryEncode(ContainerPatternTerm container) {
        if (container == null || DivineMachineryLegacy.bloodMagicSpeedCard == null) {
            // The speed-card check is simply a cheap proxy that DML preInit has run.
            // BloodMagicContent's pattern item is registered in the same phase.
        }

        if (container == null || BloodMagicContent.bloodAltarPattern == null) return false;

        IInventory patternInv = container.getInventoryByName("pattern");
        if (patternInv == null || patternInv.getSizeInventory() < 2) return false;

        ItemStack blank = patternInv.getStackInSlot(0);
        ItemStack existing = patternInv.getStackInSlot(1);
        boolean blankBlood = isBlankBloodPattern(blank);
        boolean outputBlood = isBloodPattern(existing);
        if (!blankBlood && !outputBlood) return false;

        // Blood Altar/Alchemy patterns are processing patterns only. Modern
        // bmaddon cancels vanilla encoding if a Blood Pattern is selected while
        // the terminal is in crafting-pattern mode, so do the same here.
        if (container.craftingMode) return true;

        IInventory crafting = container.getInventoryByName("crafting");
        IInventory output = container.getInventoryByName("output");
        IInventory playerInv = container.getInventoryByName("player");
        if (crafting == null || output == null || !(playerInv instanceof InventoryPlayer)) return true;

        World world = ((InventoryPlayer) playerInv).player.worldObj;
        ItemStack temporaryAePattern = buildAe2ProcessingPattern(crafting, output, container.substitute);
        if (temporaryAePattern == null) return true;

        ItemStack bloodContainer = outputBlood ? existing : blank;
        if (bloodContainer == null) return true;

        ItemStack encoded = BloodMagicPatternEncodingHelper.tryEncode(bloodContainer, temporaryAePattern, world);
        if (encoded == null) return true;

        if (existing == null) {
            // Exactly one blank Blood Pattern is consumed when creating a new
            // encoded pattern, matching the normal AE2 blank-pattern workflow.
            ItemStack remaining = blank.copy();
            remaining.stackSize--;
            patternInv.setInventorySlotContents(0, remaining.stackSize <= 0 ? null : remaining);
        }

        patternInv.setInventorySlotContents(1, encoded);
        patternInv.markDirty();
        container.detectAndSendChanges();
        return true;
    }

    private static ItemStack buildAe2ProcessingPattern(IInventory crafting, IInventory outputs,
                                                        boolean substitute) {
        ItemStack encoded = AEApi.instance().definitions().items().encodedPattern().maybeStack(1).orNull();
        if (encoded == null) return null;

        boolean hasInput = false;
        NBTTagList in = new NBTTagList();
        for (int slot = 0; slot < crafting.getSizeInventory(); slot++) {
            ItemStack stack = crafting.getStackInSlot(slot);
            NBTTagCompound tag = new NBTTagCompound();
            if (stack != null && stack.stackSize > 0) {
                stack.writeToNBT(tag);
                hasInput = true;
            }
            // ContainerPatternTerm writes all nine fake input positions,
            // including empty compounds for blank slots.
            in.appendTag(tag);
        }

        boolean hasOutput = false;
        NBTTagList out = new NBTTagList();
        for (int slot = 0; slot < outputs.getSizeInventory(); slot++) {
            ItemStack stack = outputs.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            out.appendTag(tag);
            hasOutput = true;
        }

        if (!hasInput || !hasOutput) return null;

        NBTTagCompound encodedValue = new NBTTagCompound();
        encodedValue.setTag("in", in);
        encodedValue.setTag("out", out);
        encodedValue.setBoolean("crafting", false);
        encodedValue.setBoolean("substitute", substitute);
        encoded.setTagCompound(encodedValue);
        return encoded;
    }

    public static boolean isBloodPattern(ItemStack stack) {
        return stack != null && BloodMagicContent.bloodAltarPattern != null
                && stack.getItem() == BloodMagicContent.bloodAltarPattern;
    }

    public static boolean isBlankBloodPattern(ItemStack stack) {
        return isBloodPattern(stack) && !BloodMagicPatternData.isEncoded(stack);
    }
}
