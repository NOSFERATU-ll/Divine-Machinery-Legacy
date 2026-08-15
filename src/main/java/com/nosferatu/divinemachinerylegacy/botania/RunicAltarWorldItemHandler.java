package com.nosferatu.divinemachinerylegacy.botania;

import com.nosferatu.divinemachinerylegacy.tile.TileMechanicalRunicAltar;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Gives the mechanical altar the physical Rune Altar interaction players
 * expect: valid ingredients and Livingrock dropped onto the altar are pulled
 * into its internal inventory and then handled by the machine recipe logic.
 */
public class RunicAltarWorldItemHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        World world = event.world;
        if (world == null || world.isRemote) return;

        // Twice per second is unnecessary; every other tick feels immediate
        // while keeping the scan cheap even with a room full of machines.
        if ((world.getTotalWorldTime() & 1L) != 0L) return;

        List loadedTiles = new ArrayList(world.loadedTileEntityList);
        for (Object object : loadedTiles) {
            if (!(object instanceof TileMechanicalRunicAltar)) continue;
            collectDroppedItems((TileMechanicalRunicAltar) object);
        }
    }

    private void collectDroppedItems(TileMechanicalRunicAltar tile) {
        World world = tile.getWorldObj();
        if (world == null) return;

        // Includes the visible altar bowl/top and a little air above it so a
        // Q-thrown item is accepted as soon as it lands on the machine.
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                tile.xCoord - 0.05D, tile.yCoord + 0.72D, tile.zCoord - 0.05D,
                tile.xCoord + 1.05D, tile.yCoord + 1.45D, tile.zCoord + 1.05D);

        List entities = world.getEntitiesWithinAABB(EntityItem.class, box);
        for (Object object : entities) {
            EntityItem entity = (EntityItem) object;
            if (entity.isDead) continue;

            ItemStack stack = entity.getEntityItem();
            if (stack == null || stack.stackSize <= 0) continue;
            if (!RunicAltarItemHelper.canInsertAsAltarItem(stack)) continue;

            int moved = RunicAltarItemHelper.insert(tile, stack, stack.stackSize);
            if (moved <= 0) continue;

            stack.stackSize -= moved;
            if (stack.stackSize <= 0) {
                entity.setDead();
            } else {
                entity.setEntityItemStack(stack);
            }

            world.playSoundEffect(tile.xCoord + 0.5D, tile.yCoord + 0.9D, tile.zCoord + 0.5D,
                    "random.pop", 0.15F, 1.35F + world.rand.nextFloat() * 0.2F);
        }
    }
}
