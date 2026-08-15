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

        text("tooltip.divinemachinerylegacy.gaia_blade.level", "Level: %s / 10", "Уровень: %s / 10");
        text("tooltip.divinemachinerylegacy.gaia_blade.kills", "Gaia kills: %s", "Убийств Гайи: %s");
        text("tooltip.divinemachinerylegacy.gaia_blade.next", "Kills until next level: %s", "До следующего уровня: %s убийств");
        text("tooltip.divinemachinerylegacy.gaia_blade.drop", "Bonus essence drop: %s%%", "Шанс бонусных эссенций: %s%%");
        text("tooltip.divinemachinerylegacy.gaia_blade.ready", "Ability: ready", "Способность: готова");
        text("tooltip.divinemachinerylegacy.gaia_blade.cooldown", "Cooldown: %s sec.", "Перезарядка: %s сек.");

        text("tooltip.divinemachinerylegacy.gaia_echo.echo", "Echo: %s / 100", "Эхо: %s / 100");
        text("tooltip.divinemachinerylegacy.gaia_echo.chance", "Main Gaia loot x2 chance: %s%%", "Шанс удвоить основной лут Гайи: %s%%");
        text("tooltip.divinemachinerylegacy.gaia_echo.reset", "Echo resets after killing Gaia.", "Эхо сбрасывается после убийства Гайи.");

        text("tooltip.divinemachinerylegacy.valkyrie.use", "Right click: 30 sec. flight", "ПКМ: полёт на 30 сек.");
        text("tooltip.divinemachinerylegacy.valkyrie.flight", "Flight: %s sec.", "Полёт: %s сек.");
        text("tooltip.divinemachinerylegacy.valkyrie.cooldown", "Cooldown: %s sec.", "Перезарядка: %s сек.");
        text("tooltip.divinemachinerylegacy.valkyrie.ready", "Ready", "Готово");
    }

    private static void name(Item item, String en, String ru) {
        if (item == null) return;
        LanguageRegistry.instance().addNameForObject(item, "en_US", en);
        LanguageRegistry.instance().addNameForObject(item, "ru_RU", ru);
    }

    private static void text(String key, String en, String ru) {
        LanguageRegistry.instance().addStringLocalization(key, "en_US", en);
        LanguageRegistry.instance().addStringLocalization(key, "ru_RU", ru);
    }
}
