package com.nosferatu.divinemachinerylegacy.bloodmagic;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import com.nosferatu.divinemachinerylegacy.tile.TileBloodAltarAssemblerExtended;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/** Exact 1.7.10 equivalent of bmaddon's Blood Altar Assembler memory-card helper. */
public final class BloodAltarAssemblerMemoryCardHandler {
    public static final String PATTERN_LIST_KEY = "BMAddonBloodPatterns";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event == null || event.entityPlayer == null || event.world == null
                || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        EntityPlayer player = event.entityPlayer;
        ItemStack held = player.getCurrentEquippedItem();
        if (held == null || !(held.getItem() instanceof IMemoryCard)) return;

        TileEntity tile = event.world.getTileEntity(event.x, event.y, event.z);
        if (!(tile instanceof TileBloodAltarAssemblerExtended)) return;

        event.setCanceled(true);
        if (event.world.isRemote) return;

        IMemoryCard card = (IMemoryCard) held.getItem();
        TileBloodAltarAssemblerExtended assembler = (TileBloodAltarAssemblerExtended) tile;

        if (player.isSneaking()) {
            copyPatterns(card, held, assembler, player);
        } else {
            pastePatterns(card, held, assembler, player);
        }
    }

    private void copyPatterns(IMemoryCard card, ItemStack cardStack,
                              TileBloodAltarAssemblerExtended assembler, EntityPlayer player) {
        NBTTagCompound data = card.getData(cardStack);
        if (data == null) data = new NBTTagCompound();

        NBTTagList saved = new NBTTagList();
        for (int i = 0; i < TileBloodAltarAssemblerExtended.PATTERN_SLOT_COUNT; i++) {
            ItemStack pattern = assembler.getBloodPattern(i);
            NBTTagCompound patternTag = new NBTTagCompound();
            if (pattern != null) pattern.writeToNBT(patternTag);
            saved.appendTag(patternTag);
        }
        data.setTag(PATTERN_LIST_KEY, saved);

        card.setMemoryCardContents(cardStack,
                "tile.divinemachinerylegacy.blood_altar_assembler.name", data);
        card.notifyUser(player, MemoryCardMessages.SETTINGS_SAVED);
    }

    private void pastePatterns(IMemoryCard card, ItemStack cardStack,
                               TileBloodAltarAssemblerExtended assembler, EntityPlayer player) {
        NBTTagCompound data = card.getData(cardStack);
        if (data == null || !data.hasKey(PATTERN_LIST_KEY, Constants.NBT.TAG_LIST)) {
            card.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
            return;
        }

        NBTTagList saved = data.getTagList(PATTERN_LIST_KEY, Constants.NBT.TAG_COMPOUND);
        boolean insertedAny = false;
        boolean missingBlankPatterns = false;

        int count = Math.min(TileBloodAltarAssemblerExtended.PATTERN_SLOT_COUNT, saved.tagCount());
        for (int i = 0; i < count; i++) {
            if (assembler.getBloodPattern(i) != null) continue;

            NBTTagCompound savedTag = saved.getCompoundTagAt(i);
            if (savedTag == null || savedTag.hasNoTags()) continue;

            ItemStack pattern = ItemStack.loadItemStackFromNBT(savedTag);
            if (!isEncodedBloodPattern(pattern)) continue;

            if (!player.capabilities.isCreativeMode && !consumeBlankBloodPattern(player)) {
                missingBlankPatterns = true;
                continue;
            }

            if (assembler.setBloodPatternIfEmpty(i, pattern)) {
                insertedAny = true;
            }
        }

        if (insertedAny) {
            assembler.markDirty();
            assembler.getWorldObj().markBlockForUpdate(assembler.xCoord, assembler.yCoord, assembler.zCoord);
            card.notifyUser(player, MemoryCardMessages.SETTINGS_LOADED);
        }

        if (missingBlankPatterns) {
            player.addChatMessage(new ChatComponentTranslation(
                    "chat.bmaddon.memory_card.missing_blank_patterns"));
        }
    }

    private boolean consumeBlankBloodPattern(EntityPlayer player) {
        if (BloodMagicContent.bloodAltarPattern == null) return false;

        for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if (stack == null || stack.getItem() != BloodMagicContent.bloodAltarPattern
                    || BloodMagicPatternData.isEncoded(stack)) continue;

            stack.stackSize--;
            if (stack.stackSize <= 0) player.inventory.setInventorySlotContents(slot, null);
            player.inventory.markDirty();
            return true;
        }
        return false;
    }

    private boolean isEncodedBloodPattern(ItemStack stack) {
        return stack != null
                && BloodMagicContent.bloodAltarPattern != null
                && stack.getItem() == BloodMagicContent.bloodAltarPattern
                && BloodMagicPatternData.isEncoded(stack);
    }
}
