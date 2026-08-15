package com.nosferatu.divinemachinerylegacy.bloodmagic;

import com.nosferatu.divinemachinerylegacy.config.BloodMagicAddonConfig;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * 1.7.10 equivalent of bmaddon's SyncCommonConfigS2CPacket + player events.
 *
 * The modern addon synchronises the eight Blood Generator common-config values
 * whenever a player logs in, respawns, or changes dimension. Assembler values
 * are intentionally not part of that packet in bmaddon 1.0.4.
 */
public final class BloodMagicConfigSync {
    private static final SimpleNetworkWrapper NETWORK =
            NetworkRegistry.INSTANCE.newSimpleChannel("DML_BM_CFG");
    private static boolean registered;

    private BloodMagicConfigSync() { }

    public static void register() {
        if (registered) return;
        registered = true;
        NETWORK.registerMessage(ConfigMessage.Handler.class, ConfigMessage.class, 0, Side.CLIENT);
        FMLCommonHandler.instance().bus().register(new BloodMagicConfigSync());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        sync(event.player);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sync(event.player);
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync(event.player);
    }

    private static void sync(EntityPlayer player) {
        if (player instanceof EntityPlayerMP) {
            NETWORK.sendTo(ConfigMessage.fromServerConfig(), (EntityPlayerMP) player);
        }
    }

    public static final class ConfigMessage implements IMessage {
        private int energyCapacity;
        private int maxEnergyInput;
        private int lifeTankCapacity;
        private int energyPerOperation;
        private int lifeEssencePerOperation;
        private int workIntervalTicks;
        private boolean autoOutput;
        private int maxFluidOutputPerTick;

        public ConfigMessage() { }

        private static ConfigMessage fromServerConfig() {
            ConfigMessage message = new ConfigMessage();
            message.energyCapacity = BloodMagicAddonConfig.bloodGeneratorEnergyCapacity;
            message.maxEnergyInput = BloodMagicAddonConfig.bloodGeneratorMaxEnergyInput;
            message.lifeTankCapacity = BloodMagicAddonConfig.bloodGeneratorLifeTankCapacity;
            message.energyPerOperation = BloodMagicAddonConfig.bloodGeneratorEnergyPerOperation;
            message.lifeEssencePerOperation = BloodMagicAddonConfig.bloodGeneratorLifeEssencePerOperation;
            message.workIntervalTicks = BloodMagicAddonConfig.bloodGeneratorWorkIntervalTicks;
            message.autoOutput = BloodMagicAddonConfig.bloodGeneratorAutoOutput;
            message.maxFluidOutputPerTick = BloodMagicAddonConfig.bloodGeneratorMaxFluidOutputPerTick;
            return message;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            energyCapacity = buf.readInt();
            maxEnergyInput = buf.readInt();
            lifeTankCapacity = buf.readInt();
            energyPerOperation = buf.readInt();
            lifeEssencePerOperation = buf.readInt();
            workIntervalTicks = buf.readInt();
            autoOutput = buf.readBoolean();
            maxFluidOutputPerTick = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(energyCapacity);
            buf.writeInt(maxEnergyInput);
            buf.writeInt(lifeTankCapacity);
            buf.writeInt(energyPerOperation);
            buf.writeInt(lifeEssencePerOperation);
            buf.writeInt(workIntervalTicks);
            buf.writeBoolean(autoOutput);
            buf.writeInt(maxFluidOutputPerTick);
        }

        private void applyClientConfig() {
            BloodMagicAddonConfig.bloodGeneratorEnergyCapacity = Math.max(1, energyCapacity);
            BloodMagicAddonConfig.bloodGeneratorMaxEnergyInput = Math.max(1, maxEnergyInput);
            BloodMagicAddonConfig.bloodGeneratorLifeTankCapacity = Math.max(1, lifeTankCapacity);
            BloodMagicAddonConfig.bloodGeneratorEnergyPerOperation = Math.max(1, energyPerOperation);
            BloodMagicAddonConfig.bloodGeneratorLifeEssencePerOperation = Math.max(1, lifeEssencePerOperation);
            BloodMagicAddonConfig.bloodGeneratorWorkIntervalTicks = Math.max(1, workIntervalTicks);
            BloodMagicAddonConfig.bloodGeneratorAutoOutput = autoOutput;
            BloodMagicAddonConfig.bloodGeneratorMaxFluidOutputPerTick = Math.max(1, maxFluidOutputPerTick);
        }

        public static final class Handler implements IMessageHandler<ConfigMessage, IMessage> {
            @Override
            public IMessage onMessage(ConfigMessage message, MessageContext context) {
                if (message != null) message.applyClientConfig();
                return null;
            }
        }
    }
}
