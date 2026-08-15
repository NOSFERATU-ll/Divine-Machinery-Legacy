package com.nosferatu.divinemachinerylegacy.entity;

import baubles.common.lib.PlayerHandler;
import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.botania.SparkTier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.item.ModItems;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 1.7.10 port of Extra Reforked's tiered Mana Sparks.
 *
 * The transfer graph and Spark Augment behaviour intentionally follow Botania's
 * native Spark contract, while the per-Spark transfer rate follows Extra
 * Reforked (1k -> 5M mana/t).
 */
public class EntityTieredManaSpark extends Entity implements ISparkEntity {

    private static final String TAG_UPGRADE = "upgrade";
    private static final String TAG_INVIS = "invis";
    private static final String TAG_TIER = "tier";

    public static final int INVISIBILITY_DATA_WATCHER_KEY = 27;
    private static final int UPGRADE_DATA_WATCHER_KEY = 28;
    private static final int TIER_DATA_WATCHER_KEY = 29;

    private final Set<ISparkEntity> transfers = Collections.newSetFromMap(new WeakHashMap<ISparkEntity, Boolean>());
    private int removeTransferants = 2;

    public EntityTieredManaSpark(World world) {
        super(world);
        isImmuneToFire = true;
    }

    @Override
    protected void entityInit() {
        setSize(0.1F, 0.5F);
        dataWatcher.addObject(INVISIBILITY_DATA_WATCHER_KEY, 0);
        dataWatcher.addObject(UPGRADE_DATA_WATCHER_KEY, 0);
        dataWatcher.addObject(TIER_DATA_WATCHER_KEY, SparkTier.BASE.ordinal());
        dataWatcher.setObjectWatched(INVISIBILITY_DATA_WATCHER_KEY);
        dataWatcher.setObjectWatched(UPGRADE_DATA_WATCHER_KEY);
        dataWatcher.setObjectWatched(TIER_DATA_WATCHER_KEY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        ISparkAttachable tile = getAttachedTile();
        if (tile == null) {
            if (!worldObj.isRemote) setDead();
            return;
        }

        if (worldObj.isRemote) return;

        int upgrade = getUpgrade();
        List<ISparkEntity> allSparks = null;
        if (upgrade == 2 || upgrade == 3) {
            allSparks = SparkHelper.getSparksAround(worldObj, posX, posY, posZ);
        }

        if (upgrade == 1) {
            distributeToPlayers(tile);
        } else if (upgrade == 2 && allSparks != null) {
            List<ISparkEntity> validSparks = new ArrayList<ISparkEntity>();
            for (ISparkEntity spark : allSparks) {
                if (spark == this) continue;
                if (spark.getUpgrade() == 0 && spark.getAttachedTile() instanceof IManaPool) {
                    validSparks.add(spark);
                }
            }
            if (!validSparks.isEmpty()) {
                validSparks.get(worldObj.rand.nextInt(validSparks.size())).registerTransfer(this);
            }
        } else if (upgrade == 3 && allSparks != null) {
            Collections.shuffle(allSparks);
            for (ISparkEntity spark : allSparks) {
                if (spark == this) continue;
                int otherUpgrade = spark.getUpgrade();
                if (otherUpgrade != 2 && otherUpgrade != 3 && otherUpgrade != 4) {
                    transfers.add(spark);
                }
            }
        }

        transferMana(tile);

        if (removeTransferants > 0) removeTransferants--;
        getTransfers();
    }

    private void distributeToPlayers(ISparkAttachable tile) {
        List<EntityPlayer> players = SparkHelper.getEntitiesAround(EntityPlayer.class, worldObj, posX, posY, posZ);
        Map<EntityPlayer, Map<ItemStack, Integer>> receivingPlayers = new HashMap<EntityPlayer, Map<ItemStack, Integer>>();
        ItemStack input = new ItemStack(getSparkItem());
        int transferRate = getTransferRate();

        for (EntityPlayer player : players) {
            List<ItemStack> stacks = new ArrayList<ItemStack>();
            stacks.addAll(Arrays.asList(player.inventory.mainInventory));
            stacks.addAll(Arrays.asList(player.inventory.armorInventory));
            stacks.addAll(Arrays.asList(PlayerHandler.getPlayerBaubles(player).stackList));

            for (ItemStack stack : stacks) {
                if (stack == null || !(stack.getItem() instanceof IManaItem)) continue;
                IManaItem manaItem = (IManaItem) stack.getItem();
                if (!manaItem.canReceiveManaFromItem(stack, input)) continue;

                int recv = Math.min(tile.getCurrentMana(), Math.min(transferRate, manaItem.getMaxMana(stack) - manaItem.getMana(stack)));
                if (recv <= 0) continue;

                Map<ItemStack, Integer> receivingStacks = receivingPlayers.get(player);
                if (receivingStacks == null) {
                    receivingStacks = new HashMap<ItemStack, Integer>();
                    receivingPlayers.put(player, receivingStacks);
                }
                receivingStacks.put(stack, recv);
            }
        }

        if (!receivingPlayers.isEmpty()) {
            List<EntityPlayer> keys = new ArrayList<EntityPlayer>(receivingPlayers.keySet());
            Collections.shuffle(keys);
            EntityPlayer player = keys.get(0);
            Map<ItemStack, Integer> items = receivingPlayers.get(player);
            ItemStack stack = items.keySet().iterator().next();
            int manaToPut = Math.min(tile.getCurrentMana(), items.get(stack));
            ((IManaItem) stack.getItem()).addMana(stack, manaToPut);
            tile.recieveMana(-manaToPut);
            particlesTowards(player);
        }
    }

    private void transferMana(ISparkAttachable source) {
        Collection<ISparkEntity> targets = getTransfers();
        if (targets.isEmpty() || source.getCurrentMana() <= 0) return;

        long requested = (long) getTransferRate() * (long) targets.size();
        int manaTotal = (int) Math.min((long) source.getCurrentMana(), Math.min((long) Integer.MAX_VALUE, requested));
        int remainingTargets = targets.size();
        int manaSpent = 0;

        // Reforked distributes the available budget fairly among all targets,
        // recalculating the share as full/invalid targets are skipped.
        for (ISparkEntity spark : new ArrayList<ISparkEntity>(targets)) {
            remainingTargets--;
            ISparkAttachable attached = spark.getAttachedTile();
            if (attached == null || attached.isFull() || spark.areIncomingTransfersDone()) continue;

            int divisor = remainingTargets + 1;
            int share = divisor <= 0 ? 0 : (manaTotal - manaSpent) / divisor;
            if (share <= 0) continue;

            int spend = Math.min(attached.getAvailableSpaceForMana(), share);
            if (spend <= 0) continue;

            attached.recieveMana(spend);
            manaSpent += spend;
            particlesTowards((Entity) spark);
        }

        if (manaSpent > 0) source.recieveMana(-manaSpent);
    }

    private void particlesTowards(Entity e) {
        // The actual flow is server-authoritative. Vanilla Spark rendering also
        // runs this helper on the client, so send a lightweight sparkle at the
        // source as visual feedback without touching mana client-side.
        Botania.proxy.wispFX(worldObj, posX, posY + 0.25D, posZ,
                0.6F, 0.7F, 1.0F, 0.25F,
                (float) ((e.posX - posX) * 0.04D),
                (float) ((e.posY - posY) * 0.04D),
                (float) ((e.posZ - posZ) * 0.04D));
    }

    public static void particleBeam(Entity e1, Entity e2) {
        if (e1 == null || e2 == null || !e1.worldObj.isRemote) return;

        Vector3 orig = new Vector3(e1.posX, e1.posY + 0.25D, e1.posZ);
        Vector3 end = new Vector3(e2.posX, e2.posY + 0.25D, e2.posZ);
        Vector3 diff = end.copy().sub(orig);
        if (diff.mag() <= 0.01D) return;
        Vector3 movement = diff.copy().normalize().multiply(0.1D);
        int iters = (int) (diff.mag() / movement.mag());
        float huePer = iters <= 0 ? 0F : 1F / iters;
        float hueSum = (float) Math.random();
        Vector3 currentPos = orig.copy();

        for (int i = 0; i < iters; i++) {
            Color color = Color.getHSBColor(i * huePer + hueSum, 1F, 1F);
            float r = Math.min(1F, color.getRed() / 255F + 0.4F);
            float g = Math.min(1F, color.getGreen() / 255F + 0.4F);
            float b = Math.min(1F, color.getBlue() / 255F + 0.4F);
            Botania.proxy.setSparkleFXNoClip(true);
            Botania.proxy.sparkleFX(e1.worldObj, currentPos.x, currentPos.y, currentPos.z, r, g, b, 1F, 12);
            Botania.proxy.setSparkleFXNoClip(false);
            currentPos.add(movement);
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        ItemStack stack = player.getCurrentEquippedItem();
        if (stack != null) {
            int upgrade = getUpgrade();
            if (stack.getItem() == ModItems.twigWand) {
                if (player.isSneaking()) {
                    if (upgrade > 0) {
                        if (!worldObj.isRemote) {
                            entityDropItem(new ItemStack(ModItems.sparkUpgrade, 1, upgrade - 1), 0F);
                            setUpgrade(0);
                            transfers.clear();
                            removeTransferants = 2;
                        }
                    } else if (!worldObj.isRemote) {
                        setDead();
                    }
                    if (player.worldObj.isRemote) player.swingItem();
                    return true;
                } else {
                    if (player.worldObj.isRemote) {
                        List<ISparkEntity> allSparks = SparkHelper.getSparksAround(worldObj, posX, posY, posZ);
                        for (ISparkEntity spark : allSparks) particleBeam(this, (Entity) spark);
                        player.swingItem();
                    }
                    return true;
                }
            } else if (stack.getItem() == ModItems.sparkUpgrade && upgrade == 0) {
                if (!worldObj.isRemote) {
                    setUpgrade(stack.getItemDamage() + 1);
                    if (!player.capabilities.isCreativeMode) stack.stackSize--;
                }
                if (player.worldObj.isRemote) player.swingItem();
                return true;
            }
        }

        return doPhantomInk(stack);
    }

    private boolean doPhantomInk(ItemStack stack) {
        if (stack != null && stack.getItem() == ModItems.phantomInk && !worldObj.isRemote) {
            int invis = dataWatcher.getWatchableObjectInt(INVISIBILITY_DATA_WATCHER_KEY);
            dataWatcher.updateObject(INVISIBILITY_DATA_WATCHER_KEY, ~invis & 1);
            return true;
        }
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        setUpgrade(tag.getInteger(TAG_UPGRADE));
        setSparkTier(SparkTier.fromOrdinal(tag.getInteger(TAG_TIER)));
        dataWatcher.updateObject(INVISIBILITY_DATA_WATCHER_KEY, tag.getInteger(TAG_INVIS));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger(TAG_UPGRADE, getUpgrade());
        tag.setInteger(TAG_TIER, getSparkTier().ordinal());
        tag.setInteger(TAG_INVIS, dataWatcher.getWatchableObjectInt(INVISIBILITY_DATA_WATCHER_KEY));
    }

    @Override
    public void setDead() {
        boolean wasAlive = !isDead;
        super.setDead();
        if (wasAlive && !worldObj.isRemote) {
            int upgrade = getUpgrade();
            Item sparkItem = getSparkItem();
            if (sparkItem != null) entityDropItem(new ItemStack(sparkItem), 0F);
            if (upgrade > 0) entityDropItem(new ItemStack(ModItems.sparkUpgrade, 1, upgrade - 1), 0F);
        }
    }

    @Override
    public ISparkAttachable getAttachedTile() {
        int x = MathHelper.floor_double(posX);
        int y = MathHelper.floor_double(posY) - 1;
        int z = MathHelper.floor_double(posZ);
        TileEntity tile = worldObj.getTileEntity(x, y, z);
        return tile instanceof ISparkAttachable ? (ISparkAttachable) tile : null;
    }

    @Override
    public Collection<ISparkEntity> getTransfers() {
        Collection<ISparkEntity> removals = new ArrayList<ISparkEntity>();
        int upgrade = getUpgrade();

        for (ISparkEntity spark : transfers) {
            if (spark == null || spark == this) {
                removals.add(spark);
                continue;
            }

            int otherUpgrade = spark.getUpgrade();
            ISparkAttachable attached = spark.getAttachedTile();
            boolean valid = !spark.areIncomingTransfersDone()
                    && attached != null
                    && !attached.isFull()
                    && (upgrade == 0 && otherUpgrade == 2
                    || upgrade == 3 && (otherUpgrade == 0 || otherUpgrade == 1)
                    || !(attached instanceof IManaPool));
            if (!valid) removals.add(spark);
        }

        if (!removals.isEmpty()) transfers.removeAll(removals);
        return transfers;
    }

    @Override
    public void registerTransfer(ISparkEntity entity) {
        if (entity != null && entity != this) transfers.add(entity);
    }

    @Override
    public int getUpgrade() {
        return dataWatcher.getWatchableObjectInt(UPGRADE_DATA_WATCHER_KEY);
    }

    @Override
    public void setUpgrade(int upgrade) {
        dataWatcher.updateObject(UPGRADE_DATA_WATCHER_KEY, Math.max(0, Math.min(4, upgrade)));
    }

    @Override
    public boolean areIncomingTransfersDone() {
        ISparkAttachable tile = getAttachedTile();
        if (tile instanceof IManaPool) return removeTransferants > 0;
        return tile != null && tile.areIncomingTranfersDone();
    }

    public SparkTier getSparkTier() {
        return SparkTier.fromOrdinal(dataWatcher.getWatchableObjectInt(TIER_DATA_WATCHER_KEY));
    }

    public void setSparkTier(SparkTier tier) {
        dataWatcher.updateObject(TIER_DATA_WATCHER_KEY, (tier == null ? SparkTier.BASE : tier).ordinal());
    }

    public int getTransferRate() {
        return getSparkTier().getTransferRate();
    }

    public Item getSparkItem() {
        int index = getSparkTier().ordinal();
        return index >= 0 && index < DivineMachineryLegacy.manaSparks.length
                ? DivineMachineryLegacy.manaSparks[index]
                : null;
    }
}
