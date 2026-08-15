package com.nosferatu.divinemachinerylegacy.client.render;

import com.nosferatu.divinemachinerylegacy.entity.EntityTieredManaSpark;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import vazkii.botania.client.render.entity.RenderSparkBase;
import vazkii.botania.common.item.ItemSpark;
import vazkii.botania.common.item.ItemSparkUpgrade;

/** Uses Botania's native Spark billboard renderer with the permitted Reforked tier art. */
public class RenderTieredManaSpark extends RenderSparkBase<EntityTieredManaSpark> {

    @Override
    public IIcon getBaseIcon(EntityTieredManaSpark entity) {
        Item item = entity.getSparkItem();
        IIcon icon = item == null ? null : item.getIconFromDamage(0);
        return icon == null ? ItemSpark.worldIcon : icon;
    }

    @Override
    public IIcon getSpinningIcon(EntityTieredManaSpark entity) {
        int upgrade = entity.getUpgrade() - 1;
        return upgrade >= 0 && upgrade < ItemSparkUpgrade.worldIcons.length
                ? ItemSparkUpgrade.worldIcons[upgrade]
                : null;
    }
}
