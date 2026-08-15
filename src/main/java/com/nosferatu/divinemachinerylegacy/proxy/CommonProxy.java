package com.nosferatu.divinemachinerylegacy.proxy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class CommonProxy {

    public void registerRenderers() {
        // Client only.
    }

    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }
}
