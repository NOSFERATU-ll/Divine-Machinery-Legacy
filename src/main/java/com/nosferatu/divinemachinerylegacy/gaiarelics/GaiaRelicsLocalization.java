package com.nosferatu.divinemachinerylegacy.gaiarelics;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.item.Item;

public final class GaiaRelicsLocalization {
    private GaiaRelicsLocalization() { }

    public static void register() {
        name(GaiaRelicsLegacy.yggdrasilEssence, "Yggdrasil Essence", "Эссенция Иггдрасиля");
        name(GaiaRelicsLegacy.muspelEssence, "Muspelheim Essence", "Эссенция Муспельхейма");
        name(GaiaRelicsLegacy.niflEssence, "Niflheim Essence", "Эссенция Нифльхейма");
        name(GaiaRelicsLegacy.yggdrasilIngot, "Yggdrasil Ingot", "Слиток Иггдрасиля");
        name(GaiaRelicsLegacy.muspelIngot, "Muspelheim Ingot", "Слиток Муспельхейма");
        name(GaiaRelicsLegacy.niflIngot, "Niflheim Ingot", "Слиток Нифльхейма");
        name(GaiaRelicsLegacy.gaiaEchoBlade, "Gaia Echo Blade", "Эхо-клинок Гайи");
        name(GaiaRelicsLegacy.gaiaBlade, "Gaia Blade", "Клинок Гайи");
        name(GaiaRelicsLegacy.valkyrieFeather, "Valkyrie Feather", "Перо Валькирии");
    }

    private static void name(Item item, String en, String ru) {
        if (item == null) return;
        LanguageRegistry.instance().addNameForObject(item, "en_US", en);
        LanguageRegistry.instance().addNameForObject(item, "ru_RU", ru);
    }
}
