package com.nosferatu.divinemachinerylegacy.integration.ae2;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Bridges DML processing machines back into an adjacent AE2 ME Interface.
 *
 * AE2 rv3 processing patterns already push their inputs into our machines via
 * ICraftingMachine. Vanilla/Forge automation expects the completed output to
 * be inserted back into the Interface, after which AE2 returns it to the ME
 * network and the crafting CPU can finish the job. This helper performs that
 * last hop without requiring a separate Import Bus.
 *
 * Both the full-block ME Interface and the cable/part ME Interface are
 * supported. If no Interface is adjacent, or it cannot currently accept the
 * output, the item remains in the machine's normal output slot and ordinary
 * pipes / Import Buses continue to work exactly as before.
 */
public final class Ae2InterfaceOutputReturner {

    private static final ForgeDirection[] SIDES = {
            ForgeDirection.DOWN,
            ForgeDirection.UP,
            ForgeDirection.NORTH,
            ForgeDirection.SOUTH,
            ForgeDirection.WEST,
            ForgeDirection.EAST
    };

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.world == null || event.world.isRemote) return;

        // A one-tick return is unnecessary for AE2's crafting CPU. Checking on
        // alternate ticks keeps this effectively free even in TE-heavy packs.
        if ((event.world.getTotalWorldTime() & 1L) != 0L) return;

        World world = event.world;
        for (Object obj : world.loadedTileEntityList) {
            if (!(obj instanceof TileEntity)) continue;
            TileEntity tile = (TileEntity) obj;
            if (!(tile instanceof ICraftingMachine) || !(tile instanceof ISidedInventory)) continue;
            if (!isDmlMachine(tile)) continue;

            returnOutputs(world, tile, (ISidedInventory) tile);
        }
    }

    private void returnOutputs(World world, TileEntity machineTile, ISidedInventory machine) {
        for (ForgeDirection direction : SIDES) {
            TileEntity neighbor = world.getTileEntity(
                    machineTile.xCoord + direction.offsetX,
                    machineTile.yCoord + direction.offsetY,
                    machineTile.zCoord + direction.offsetZ);
            if (neighbor == null) continue;

            IInventory target = findInterfaceInventory(neighbor, direction);
            if (target == null) continue;

            int sourceSide = direction.ordinal();
            int targetSide = direction.getOpposite().ordinal();
            boolean movedAny = false;

            for (int slot = 0; slot < machine.getSizeInventory(); slot++) {
                ItemStack stack = machine.getStackInSlot(slot);
                if (stack == null || stack.stackSize <= 0) continue;
                if (!machine.canExtractItem(slot, stack, sourceSide)) continue;

                int accepted = insertInto(target, stack, targetSide);
                if (accepted <= 0) continue;

                machine.decrStackSize(slot, accepted);
                movedAny = true;
            }

            if (movedAny) {
                machine.markDirty();
                target.markDirty();
            }
        }
    }

    /**
     * Resolve either a full-block ME Interface or an Interface part mounted on
     * the face of an adjacent cable bus. Class-name checks deliberately avoid
     * binding DML to AE2 implementation classes outside the public parts API.
     */
    private IInventory findInterfaceInventory(TileEntity neighbor, ForgeDirection fromMachine) {
        if (neighbor instanceof IInventory && isClassOrSubclass(neighbor, "appeng.tile.misc.TileInterface")) {
            return (IInventory) neighbor;
        }

        if (neighbor instanceof IPartHost) {
            IPart part = ((IPartHost) neighbor).getPart(fromMachine.getOpposite());
            if (part instanceof IInventory && isClassOrSubclass(part, "appeng.parts.misc.PartInterface")) {
                return (IInventory) part;
            }
        }

        return null;
    }

    /**
     * Insert as ordinary sided automation would. ME Interfaces expose their
     * nine storage slots to external inventories and move accepted items into
     * the ME network on their own tick.
     *
     * @return amount accepted by the Interface
     */
    private int insertInto(IInventory target, ItemStack source, int side) {
        ItemStack incoming = source.copy();
        int original = incoming.stackSize;
        int[] slots = accessibleSlots(target, side);

        // Merge into matching stacks first.
        for (int i = 0; i < slots.length && incoming.stackSize > 0; i++) {
            int slot = slots[i];
            ItemStack existing = target.getStackInSlot(slot);
            if (existing == null || !canStacksMerge(existing, incoming)) continue;
            if (!canInsert(target, slot, incoming, side)) continue;

            int limit = Math.min(target.getInventoryStackLimit(), existing.getMaxStackSize());
            int room = limit - existing.stackSize;
            if (room <= 0) continue;

            int moved = Math.min(room, incoming.stackSize);
            existing.stackSize += moved;
            incoming.stackSize -= moved;
            target.setInventorySlotContents(slot, existing);
        }

        // Then use empty Interface storage slots.
        for (int i = 0; i < slots.length && incoming.stackSize > 0; i++) {
            int slot = slots[i];
            if (target.getStackInSlot(slot) != null) continue;
            if (!canInsert(target, slot, incoming, side)) continue;

            int limit = Math.min(target.getInventoryStackLimit(), incoming.getMaxStackSize());
            int moved = Math.min(limit, incoming.stackSize);
            ItemStack placed = incoming.copy();
            placed.stackSize = moved;
            target.setInventorySlotContents(slot, placed);
            incoming.stackSize -= moved;
        }

        return original - incoming.stackSize;
    }

    private int[] accessibleSlots(IInventory target, int side) {
        if (target instanceof ISidedInventory) {
            int[] slots = ((ISidedInventory) target).getAccessibleSlotsFromSide(side);
            return slots == null ? new int[0] : slots;
        }

        int[] slots = new int[target.getSizeInventory()];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
        return slots;
    }

    private boolean canInsert(IInventory target, int slot, ItemStack stack, int side) {
        return !(target instanceof ISidedInventory)
                || ((ISidedInventory) target).canInsertItem(slot, stack, side);
    }

    private boolean canStacksMerge(ItemStack a, ItemStack b) {
        return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    private boolean isDmlMachine(TileEntity tile) {
        String name = tile.getClass().getName();
        return name.startsWith("com.nosferatu.divinemachinerylegacy.tile.TileMechanical")
                || name.equals("com.nosferatu.divinemachinerylegacy.tile.TileBloodAltarAssembler");
    }

    private boolean isClassOrSubclass(Object object, String className) {
        Class<?> type = object.getClass();
        while (type != null) {
            if (className.equals(type.getName())) return true;
            type = type.getSuperclass();
        }
        return false;
    }
}
