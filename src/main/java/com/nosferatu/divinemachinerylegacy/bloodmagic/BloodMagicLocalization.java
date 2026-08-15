package com.nosferatu.divinemachinerylegacy.bloodmagic;

import cpw.mods.fml.common.registry.LanguageRegistry;

/** Runtime registration of bmaddon text for the 1.7.10 port. */
public final class BloodMagicLocalization {
    private static boolean registered;

    private BloodMagicLocalization() { }

    public static void register() {
        if (registered) return;
        registered = true;

        // English.
        en("tile.divinemachinerylegacy.blood_generator.name", "Blood Generator");
        en("tile.divinemachinerylegacy.blood_altar_assembler.name", "Blood Assembler");
        en("container.divinemachinerylegacy.blood_generator", "Blood Generator");
        en("container.divinemachinerylegacy.blood_altar_assembler", "Blood Assembler");
        en("item.divinemachinerylegacy.blood_altar_pattern.name", "Blood Pattern");
        en("item.divinemachinerylegacy.blood_altar_pattern.encoded.name", "Pattern: %s");
        en("item.divinemachinerylegacy.blood_altar_tier_card_2.name", "Blood Altar Tier Card II");
        en("item.divinemachinerylegacy.blood_altar_tier_card_3.name", "Blood Altar Tier Card III");
        en("item.divinemachinerylegacy.blood_altar_tier_card_4.name", "Blood Altar Tier Card IV");
        en("item.divinemachinerylegacy.blood_altar_tier_card_5.name", "Blood Altar Tier Card V");
        en("item.divinemachinerylegacy.blood_altar_parallel_card.name", "Blood Assembler Parallel Processing Card");
        en("item.divinemachinerylegacy.blood_magic_speed_card.name", "Blood Speed Card");
        en("gui.divinemachinerylegacy.blood_altar_assembler.patterns", "Patterns");
        en("gui.divinemachinerylegacy.blood_altar_assembler.tier_cards", "Upgrade Cards");
        en("tooltip.divinemachinerylegacy.blood_generator.energy", "Energy: %s / %s RF");
        en("tooltip.divinemachinerylegacy.blood_generator.blood", "Blood: %s / %s mB");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.empty", "Empty Blood Pattern.");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.invalid", "Invalid pattern.");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.type", "Type: %s");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.type.blood_altar", "Blood Altar");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.type.alchemy_table", "Alchemy Table");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.input", "Input: %s");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.input_indexed", "Input %s: %s");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.output", "Output: %s");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.tier", "Required Altar Tier: %s");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.life_essence", "Blood: %s LP");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.craft_time", "Time: %s t.");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.recipe_missing", "Recipe no longer exists.");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.shift_clear", "Shift + Right Click: clear pattern.");
        en("tooltip.divinemachinerylegacy.blood_altar_pattern.how_to_encode",
                "Craft together with an encoded AE2 processing pattern to copy a Blood Altar or Alchemy Table recipe.");
        en("tooltip.divinemachinerylegacy.blood_altar_tier_card.tier", "Raises altar tier to: %s");
        en("tooltip.divinemachinerylegacy.blood_altar_tier_card.description", "All lower-tier recipes remain available.");
        en("tooltip.divinemachinerylegacy.blood_altar_parallel_card.description",
                "Increases the number of simultaneous crafts in the Blood Assembler.");
        en("tooltip.divinemachinerylegacy.blood_magic_speed_card.description",
                "Counts as four normal speed cards and can exceed the normal AE2 speed limit.");
        en("chat.bmaddon.memory_card.missing_blank_patterns", "Not enough blank Blood Patterns in inventory");
        en("message.divinemachinerylegacy.blood_altar_pattern.cleared", "Pattern cleared.");
        en("message.divinemachinerylegacy.blood_altar_pattern.already_empty", "Pattern is already empty.");

        // Russian — copied from the permitted original localization where applicable.
        ru("tile.divinemachinerylegacy.blood_generator.name", "Генератор крови");
        ru("tile.divinemachinerylegacy.blood_altar_assembler.name", "Кровавый сборщик");
        ru("container.divinemachinerylegacy.blood_generator", "Генератор крови");
        ru("container.divinemachinerylegacy.blood_altar_assembler", "Кровавый сборщик");
        ru("item.divinemachinerylegacy.blood_altar_pattern.name", "Шаблон крови");
        ru("item.divinemachinerylegacy.blood_altar_pattern.encoded.name", "Шаблон: %s");
        ru("item.divinemachinerylegacy.blood_altar_tier_card_2.name", "Карта кровавого алтаря II");
        ru("item.divinemachinerylegacy.blood_altar_tier_card_3.name", "Карта кровавого алтаря III");
        ru("item.divinemachinerylegacy.blood_altar_tier_card_4.name", "Карта кровавого алтаря IV");
        ru("item.divinemachinerylegacy.blood_altar_tier_card_5.name", "Карта кровавого алтаря V");
        ru("item.divinemachinerylegacy.blood_altar_parallel_card.name", "Карта параллельной обработки кровавого сборщика");
        ru("item.divinemachinerylegacy.blood_magic_speed_card.name", "Кровавая карта скорости");
        ru("gui.divinemachinerylegacy.blood_altar_assembler.patterns", "Шаблоны");
        ru("gui.divinemachinerylegacy.blood_altar_assembler.tier_cards", "Карты улучшений");
        ru("tooltip.divinemachinerylegacy.blood_generator.energy", "Энергия: %s / %s RF");
        ru("tooltip.divinemachinerylegacy.blood_generator.blood", "Кровь: %s / %s mB");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.empty", "Пустой шаблон крови.");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.invalid", "Повреждённый шаблон.");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.type", "Тип: %s");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.type.blood_altar", "Кровавый алтарь");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.type.alchemy_table", "Алхимический стол");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.input", "Вход: %s");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.input_indexed", "Вход %s: %s");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.output", "Выход: %s");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.tier", "Требуемый уровень алтаря: %s");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.life_essence", "Кровь: %s LP");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.craft_time", "Время: %s т.");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.recipe_missing", "Рецепт больше не существует.");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.shift_clear", "Shift + ПКМ: очистить шаблон.");
        ru("tooltip.divinemachinerylegacy.blood_altar_pattern.how_to_encode",
                "Соедини с записанным шаблоном обработки AE2 в сетке крафта, чтобы скопировать рецепт Кровавого алтаря или Алхимического стола.");
        ru("tooltip.divinemachinerylegacy.blood_altar_tier_card.tier", "Повышает уровень алтаря до: %s");
        ru("tooltip.divinemachinerylegacy.blood_altar_tier_card.description", "Все рецепты нижних уровней также остаются доступны.");
        ru("tooltip.divinemachinerylegacy.blood_altar_parallel_card.description",
                "Увеличивает количество одновременных крафтов в кровавом сборщике.");
        ru("tooltip.divinemachinerylegacy.blood_magic_speed_card.description",
                "Считается за четыре обычные карты и может превышать обычный лимит скорости AE2.");
        ru("chat.bmaddon.memory_card.missing_blank_patterns", "Нет кровавых шаблонов в инвентаре");
        ru("message.divinemachinerylegacy.blood_altar_pattern.cleared", "Шаблон очищен.");
        ru("message.divinemachinerylegacy.blood_altar_pattern.already_empty", "Шаблон уже пуст.");
    }

    private static void en(String key, String value) {
        LanguageRegistry.instance().addStringLocalization(key, "en_US", value);
    }

    private static void ru(String key, String value) {
        LanguageRegistry.instance().addStringLocalization(key, "ru_RU", value);
    }
}
