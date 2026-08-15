# Divine Machinery Legacy

A Minecraft 1.7.10 backport/compatibility project focused on compact automation for the magic systems used by Divine Journey.

## First target: Botania

The first module ports the useful automation concepts from:

- Botanical Machinery
- Botanical Machinery Extra Reforked

Target runtime versions:

- Minecraft 1.7.10
- Botania r1.8-249
- Applied Energistics 2 rv3-beta-6

The goal is not a mechanical source-to-source port. Modern Forge, Botania and AE2 APIs are too different. We are reimplementing the same behaviour against the native 1.7.10 APIs while preserving the useful design: tiered machines, parallel crafting, mana buffers, automation, sparks and upgrades.

### Current milestone

Mechanical Runic Altar:

- real Botania `BotaniaAPI.runeAltarRecipes`
- tiered mana capacity
- tiered parallel processing
- Livingrock handling
- rune catalyst preservation
- sided item automation
- Botania mana bursts and sparks
- persistent NBT state
- AE2 integration after the standalone machine core is stable

## Planned Botania scope

- Mechanical Mana Pool
- Mechanical Runic Altar
- Mechanical Daisy
- Mechanical Apothecary
- Industrial Agglomeration Factory
- Alfheim Market
- Mechanical Orechid
- Mana Infuser where compatible with the 1.7.10 Botania ecosystem
- Brewery/base Botanical Machinery features where applicable
- tiered sparks
- machine upgrades/catalysts
- supporting materials

Later modules will cover Blood Magic and Thaumcraft automation.

## Build dependencies

Third-party mod jars are intentionally **not committed**. Put the exact development jars into `libs/` locally:

- `Botania r1.8-249.jar`
- `appliedenergistics2-rv3-beta-6.jar`

## Attribution and assets

Botanical Machinery and Botanical Machinery Extra Reforked are used as behavioural/code references under their respective open-source licenses. License and attribution notices will be kept in `THIRD_PARTY_NOTICES.md`.

Botanical Machinery Extra Reforked explicitly restricts redistribution of its machine GUI textures. Those GUI textures are therefore **not included in this repository**. Temporary/original 1.7.10 UI assets will be used during development and replaced with original project artwork later.

## Status

Early porting work. Not ready for a survival world yet.
