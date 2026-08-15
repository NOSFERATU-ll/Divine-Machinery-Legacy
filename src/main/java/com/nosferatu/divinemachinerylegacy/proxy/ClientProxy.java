package com.nosferatu.divinemachinerylegacy.proxy;

import com.nosferatu.divinemachinerylegacy.DivineMachineryLegacy;
import com.nosferatu.divinemachinerylegacy.block.*;
import com.nosferatu.divinemachinerylegacy.bloodmagic.BloodMagicContent;
import com.nosferatu.divinemachinerylegacy.client.render.*;
import com.nosferatu.divinemachinerylegacy.entity.EntityTieredManaSpark;
import com.nosferatu.divinemachinerylegacy.gaiarelics.GaiaRelicsLegacy;
import com.nosferatu.divinemachinerylegacy.gui.*;
import com.nosferatu.divinemachinerylegacy.tile.*;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public void registerRenderers() {
        int runic = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalRunicAltar.setRenderId(runic);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalRunicAltar(runic));

        int pool = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalManaPool.setRenderId(pool);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalManaPool(pool));

        int infuser = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalManaInfuser.setRenderId(infuser);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalManaInfuser(infuser));

        int apothecary = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalApothecary.setRenderId(apothecary);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalApothecary(apothecary));

        int daisy = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalDaisy.setRenderId(daisy);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalDaisy(daisy));

        int agglomeration = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalIndustrialAgglomerationFactory.setRenderId(agglomeration);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalIndustrialAgglomerationFactory(agglomeration));

        int alfheim = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalAlfheimMarket.setRenderId(alfheim);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalAlfheimMarket(alfheim));

        int orechid = RenderingRegistry.getNextAvailableRenderId();
        BlockMechanicalOrechid.setRenderId(orechid);
        RenderingRegistry.registerBlockHandler(new RenderMechanicalOrechid(orechid));

        int jaded = RenderingRegistry.getNextAvailableRenderId();
        BlockJadedAmaranthus.setRenderId(jaded);
        RenderingRegistry.registerBlockHandler(new RenderJadedAmaranthus(jaded));

        int greenhouse = RenderingRegistry.getNextAvailableRenderId();
        BlockGreenhouse.setRenderId(greenhouse);
        RenderingRegistry.registerBlockHandler(new RenderGreenhouse(greenhouse));

        int bloodAssembler = RenderingRegistry.getNextAvailableRenderId();
        BlockBloodAltarAssembler.setRenderId(bloodAssembler);
        RenderingRegistry.registerBlockHandler(new RenderBloodAltarAssembler(bloodAssembler));

        RenderBloodGenerator bloodGeneratorRenderer = new RenderBloodGenerator();
        ClientRegistry.bindTileEntitySpecialRenderer(TileBloodGenerator.class, bloodGeneratorRenderer);
        if (BloodMagicContent.bloodGenerator != null) {
            MinecraftForgeClient.registerItemRenderer(
                    Item.getItemFromBlock(BloodMagicContent.bloodGenerator), bloodGeneratorRenderer);
        }

        RenderGaiaRelicItem relicRenderer = new RenderGaiaRelicItem();
        Item[] relics = {
                GaiaRelicsLegacy.yggdrasilEssence, GaiaRelicsLegacy.muspelEssence, GaiaRelicsLegacy.niflEssence,
                GaiaRelicsLegacy.yggdrasilIngot, GaiaRelicsLegacy.muspelIngot, GaiaRelicsLegacy.niflIngot,
                GaiaRelicsLegacy.gaiaEchoBlade, GaiaRelicsLegacy.gaiaBlade, GaiaRelicsLegacy.valkyrieFeather
        };
        for (Item relic : relics) {
            if (relic != null) MinecraftForgeClient.registerItemRenderer(relic, relicRenderer);
        }

        RenderingRegistry.registerEntityRenderingHandler(EntityTieredManaSpark.class, new RenderTieredManaSpark());
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (id == DivineMachineryLegacy.GUI_RUNIC_ALTAR && te instanceof TileMechanicalRunicAltar)
            return new GuiMechanicalRunicAltar(player.inventory, (TileMechanicalRunicAltar) te);
        if (id == DivineMachineryLegacy.GUI_MANA_POOL && te instanceof TileMechanicalManaPool)
            return new GuiMechanicalManaPool(player.inventory, (TileMechanicalManaPool) te);
        if (id == DivineMachineryLegacy.GUI_MANA_INFUSER && te instanceof TileMechanicalManaInfuser)
            return new GuiMechanicalManaInfuser(player.inventory, (TileMechanicalManaInfuser) te);
        if (id == DivineMachineryLegacy.GUI_APOTHECARY && te instanceof TileMechanicalApothecary)
            return new GuiMechanicalApothecary(player.inventory, (TileMechanicalApothecary) te);
        if (id == DivineMachineryLegacy.GUI_DAISY && te instanceof TileMechanicalDaisy)
            return new GuiMechanicalDaisy(player.inventory, (TileMechanicalDaisy) te);
        if (id == DivineMachineryLegacy.GUI_INDUSTRIAL_AGGLOMERATION
                && te instanceof TileMechanicalIndustrialAgglomerationFactory)
            return new GuiMechanicalIndustrialAgglomerationFactory(player.inventory,
                    (TileMechanicalIndustrialAgglomerationFactory) te);
        if (id == DivineMachineryLegacy.GUI_ALFHEIM_MARKET && te instanceof TileMechanicalAlfheimMarket)
            return new GuiMechanicalAlfheimMarket(player.inventory, (TileMechanicalAlfheimMarket) te);
        if (id == DivineMachineryLegacy.GUI_ORECHID && te instanceof TileMechanicalOrechid)
            return new GuiMechanicalOrechid(player.inventory, (TileMechanicalOrechid) te);
        if (id == DivineMachineryLegacy.GUI_JADED_AMARANTHUS && te instanceof TileJadedAmaranthus)
            return new GuiJadedAmaranthus(player.inventory, (TileJadedAmaranthus) te);
        if (id == DivineMachineryLegacy.GUI_GREENHOUSE && te instanceof TileGreenhouse)
            return new GuiGreenhouse(player.inventory, (TileGreenhouse) te);
        if (id == DivineMachineryLegacy.GUI_BLOOD_ALTAR_ASSEMBLER && te instanceof TileBloodAltarAssembler)
            return new GuiBloodAltarAssembler(player.inventory, (TileBloodAltarAssembler) te);
        if (id == BloodMagicContent.GUI_BLOOD_GENERATOR && te instanceof TileBloodGenerator)
            return new GuiBloodGenerator(player.inventory, (TileBloodGenerator) te);
        return null;
    }
}
