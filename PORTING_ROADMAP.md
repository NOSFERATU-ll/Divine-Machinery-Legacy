# Divine Machinery Legacy — porting roadmap

Target: Minecraft 1.7.10 / Forge, for the customized Divine Journey pack.

Primary references:
- Botanical Machinery
- Botanical Machinery Extra Reforked
- Botania r1.8-249
- Applied Energistics 2 rv3-beta-6

## Rule for development builds

Do not ask the player to runtime-test a machine until that machine reaches its own **feature-complete milestone**. CI compilation alone is not the milestone.

During development, original Botanical Machinery Extra Reforked GUI/visual assets may be used under BOLT_M4G1C's explicit 2026-08-15 permission. They are temporary development assets and will be replaced/redrawn after the port is feature-complete.

## Milestone 1 — Mechanical Runic Altar

### Core recipe behavior
- [x] Read real `BotaniaAPI.runeAltarRecipes`.
- [x] Use the real mana cost of each recipe.
- [x] Consume the exact matching ingredients.
- [x] Return input runes like the normal Botania altar.
- [x] Consume Livingrock as the final ritual material.
- [x] Refuse to start if outputs do not fit.
- [x] Revalidate inputs immediately before craft completion.

### Tiers
- [x] Malachite: 2.5M mana / up to 4 crafts.
- [x] Saffron: 10M mana / up to 8 crafts.
- [x] Shadow: 50M mana / up to 16 crafts / 1 upgrade slot.
- [x] Crimson: 100M mana / up to 32 crafts / 2 upgrade slots.

### Mana / Botania integration
- [x] Native 1.7.10 mana receiver API.
- [x] Stop accepting bursts when the buffer is full.
- [x] Spark attachment API.
- [x] Infinite Mana catalyst behavior.
- [x] Infinite Livingrock catalyst behavior.

### Inventory / automation
- [x] 16 input slots and 16 output slots.
- [x] Sided inventory insertion/extraction.
- [x] Pipe / hopper compatible input and output.
- [x] AE2 rv3 `ICraftingMachine` processing-pattern input.
- [ ] Verify one-interface + import-bus autocrafting flow in a real 1.7.10 instance.
- [ ] Decide whether to additionally backport Extra Reforked's direct ME-node/output-export behavior after the standard AE2 processing-pattern flow is verified.

### Persistence / safety
- [x] Persist mana, inventory and progress to NBT.
- [x] Restore the active Botania recipe after world reload.
- [x] Drop machine inventory when broken.
- [ ] Runtime-test save/reload during an active craft.
- [ ] Runtime-test output-full behavior and mid-craft inventory changes.

### UI / discoverability
- [x] GUI/container.
- [x] Mana/progress/batch status.
- [x] Upgrade slots and infinite-resource status.
- [x] Dedicated `Divine Machinery Legacy` creative tab.
- [x] All four altar metadata variants exposed to Creative/NEI.
- [x] English and Russian names.
- [x] Original Extra Reforked Runic Altar development GUI wired under explicit permission.
- [x] Original inventory modules, mana bars, tier textures and catalyst art wired for development builds.
- [ ] Final 1.7.10 block renderer/model for the altar frame.
- [ ] Final original Divine Machinery Legacy GUI/artwork pass after feature completion.

### Milestone gate

**Do not call Milestone 1 test-ready until:**
1. CI is green on the exact Botania r1.8-249 + AE2 rv3-beta-6 targets.
2. the block renderer/model is in place;
3. AE2 processing patterns compile and are wired to the machine;
4. the remaining runtime-only checks are the only unverified items.

---

## After the Runic Altar

Port the rest of Botanical Machinery / Extra Reforked as reusable shared machinery code rather than independent copy-pasted tiles:

- Mechanical Mana Pool tiers
- Mechanical Apothecary tiers
- Mechanical Daisy tiers
- Industrial Agglomeration Factory tiers
- Alfheim Market tiers
- Orechid machinery
- Mana Infuser where a 1.7.10-compatible recipe source exists
- tier materials and dragonstone/ingot progression
- upgraded Sparks
- infinity catalysts (water, seeds, stone, wood, etc.)
- speed and other machine upgrades that make sense on the 1.7.10 API

Later modules:
- Blood Magic automation (BloodMagic AE2 Addition / NeoVitae + 1.7.10 server references)
- Thaumcraft Arcane/Alchemy/Infusion automation
