package com.nosferatu.divinemachinerylegacy.gaiarelics;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import vazkii.botania.common.entity.EntityDoppleganger;

import java.util.ArrayList;
import java.util.List;

public final class GaiaRelicsEvents {
    @SubscribeEvent
    public void onGaiaHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityDoppleganger)) return;
        EntityPlayer player = sourcePlayer(event.source);
        if (player == null) return;
        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() instanceof ItemGaiaEchoBlade) {
            ItemGaiaEchoBlade.setEcho(held, ItemGaiaEchoBlade.getEcho(held) + 1);
        }
    }

    @SubscribeEvent
    public void onGaiaDrops(LivingDropsEvent event) {
        if (!(event.entityLiving instanceof EntityDoppleganger)) return;
        EntityPlayer player = sourcePlayer(event.source);
        if (player == null) return;
        ItemStack held = player.getHeldItem();
        if (held == null) return;

        if (held.getItem() instanceof ItemGaiaEchoBlade) {
            int echo = ItemGaiaEchoBlade.getEcho(held);
            if (echo > 0 && player.worldObj.rand.nextInt(100) < echo) {
                List<EntityItem> copies = new ArrayList<EntityItem>();
                for (Object obj : event.drops) {
                    if (!(obj instanceof EntityItem)) continue;
                    EntityItem original = (EntityItem) obj;
                    ItemStack copy = original.getEntityItem() == null ? null : original.getEntityItem().copy();
                    if (copy != null) copies.add(new EntityItem(player.worldObj, original.posX, original.posY, original.posZ, copy));
                }
                event.drops.addAll(copies);
            }
            ItemGaiaEchoBlade.setEcho(held, 0);
            return;
        }

        if (held.getItem() instanceof ItemGaiaBlade) {
            ItemGaiaBlade.addKill(held);
            if (player.worldObj.rand.nextDouble() <= ItemGaiaBlade.getBonusDropChance(held)) {
                dropRealmEssence(event, GaiaRelicsLegacy.yggdrasilEssence, player);
                dropRealmEssence(event, GaiaRelicsLegacy.muspelEssence, player);
                dropRealmEssence(event, GaiaRelicsLegacy.niflEssence, player);
            }
        }
    }

    private static void dropRealmEssence(LivingDropsEvent event, net.minecraft.item.Item item, EntityPlayer player) {
        if (item == null) return;
        int amount = 5 + player.worldObj.rand.nextInt(16);
        event.drops.add(new EntityItem(player.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ,
                new ItemStack(item, amount)));
    }

    private static EntityPlayer sourcePlayer(DamageSource source) {
        return source != null && source.getEntity() instanceof EntityPlayer ? (EntityPlayer) source.getEntity() : null;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.worldObj.isRemote) return;
        EntityPlayer player = event.player;
        ItemStack feather = findFeather(player);
        if (feather == null || feather.getTagCompound() == null) return;

        long now = player.worldObj.getTotalWorldTime();
        long flightEnd = feather.getTagCompound().getLong(ItemValkyrieFeather.TAG_FLIGHT_END);
        long cooldownEnd = feather.getTagCompound().getLong(ItemValkyrieFeather.TAG_COOLDOWN_END);

        if (flightEnd > now) {
            if (!player.capabilities.allowFlying) {
                player.capabilities.allowFlying = true;
                syncAbilities(player);
            }
            return;
        }

        if (flightEnd > 0) {
            feather.getTagCompound().setLong(ItemValkyrieFeather.TAG_FLIGHT_END, 0);
            feather.getTagCompound().setLong(ItemValkyrieFeather.TAG_COOLDOWN_END, now + 60L * 20L);
            if (!player.capabilities.isCreativeMode) {
                player.capabilities.isFlying = false;
                player.capabilities.allowFlying = false;
                player.fallDistance = 0F;
                syncAbilities(player);
            }
            return;
        }

        if (cooldownEnd <= now && !player.capabilities.isCreativeMode && player.capabilities.allowFlying) {
            player.capabilities.isFlying = false;
            player.capabilities.allowFlying = false;
            syncAbilities(player);
        }
    }

    private static ItemStack findFeather(EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemValkyrieFeather) return stack;
        }
        return null;
    }

    private static void syncAbilities(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) ((EntityPlayerMP) player).sendPlayerAbilities();
    }
}
