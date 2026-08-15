package com.nosferatu.divinemachinerylegacy.block;
import com.nosferatu.divinemachinerylegacy.botania.MachineTier;
import net.minecraft.block.Block;import net.minecraft.item.ItemBlock;import net.minecraft.item.ItemStack;
public class ItemBlockMechanicalApothecary extends ItemBlock{public ItemBlockMechanicalApothecary(Block b){super(b);setHasSubtypes(true);setMaxDamage(0);}@Override public int getMetadata(int d){return d&3;}@Override public String getUnlocalizedName(ItemStack s){return "tile.divinemachinerylegacy.mechanical_apothecary."+MachineTier.fromMeta(s.getItemDamage()).getKey();}}
