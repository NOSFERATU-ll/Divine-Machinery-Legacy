# Botania port roadmap

This branch targets **Botania r1.8-249 on Minecraft 1.7.10** and uses Botanical Machinery + Botanical Machinery Extra Reforked as behavioural references.

The goal is feature parity where the feature makes sense on 1.7.10, not a literal modern-Forge source translation.

## Phase 1 — common machine foundation

- [x] 1.7.10 project skeleton
- [x] four machine tiers: Malachite / Saffron / Shadow / Crimson
- [x] tier mana capacities: 2.5M / 10M / 50M / 100M
- [x] tier parallel limits: 4 / 8 / 16 / 32
- [x] native Botania mana receiver support
- [x] native Botania spark attachment support
- [x] sided inventory foundation
- [x] persistent inventory, mana and in-progress recipe state
- [x] placeholder GUI that does not redistribute restricted upstream GUI art
- [ ] shared base classes for the remaining machine families
- [ ] config file for capacities, parallelism and speeds
- [ ] MineTweaker/ModTweaker hooks where useful

## Phase 2 — Mechanical Runic Altar

- [x] use the real `BotaniaAPI.runeAltarRecipes` list
- [x] respect each recipe's real mana cost
- [x] consume Livingrock
- [x] preserve/return Botania runes like the vanilla Runic Altar
- [x] parallel crafting by tier
- [x] block output overflow while a batch is selected
- [x] stop receiving mana at the tier capacity
- [x] pipe/hopper-style sided item automation
- [ ] in-game recipe and balance testing
- [ ] NEI presentation
- [ ] AE2 crafting-provider integration
- [ ] direct ME output export
- [ ] Infinity Mana catalyst
- [ ] Infinity Livingrock catalyst
- [ ] proper original block/item artwork

## Phase 3 — remaining Botanical Machinery families

Planned machine families:

- [ ] Mechanical Mana Pool
- [ ] Mechanical Pure Daisy
- [ ] Mechanical Apothecary
- [ ] Mechanical Brewery
- [ ] Industrial Agglomeration Factory / Terrasteel automation
- [ ] Alfheim Market
- [ ] Mechanical Orechid
- [ ] Mana Infuser when a compatible 1.7.10 recipe provider exists
- [ ] Mana Battery variants from base Botanical Machinery where useful
- [ ] Jaded Amaranthus automation where it makes sense on 1.7.10
- [ ] Greenhouse where its modern dependencies can be replaced cleanly

Each machine should work with normal 1.7.10 item/fluid automation first. AE2 should enhance the machine rather than be the only way to use it unless the original mechanic is inherently ME-specific.

## Phase 4 — materials and upgrades

Port/reimplement the useful Extra Reforked progression layer:

- [ ] Malachite materials
- [ ] Saffron materials
- [ ] Shadow materials
- [ ] Crimson materials
- [ ] Aureate materials where used by late upgrades/sparks
- [ ] Mazarine materials where used by late upgrades/sparks
- [ ] Crystal-tier supporting materials where still meaningful

Upgrade/catalyst families to evaluate and port:

- [ ] Infinity Mana
- [ ] Infinity Livingrock
- [ ] Infinity Water
- [ ] Infinity Seed
- [ ] Infinity Stone
- [ ] Infinity Wood
- [ ] Speed upgrades
- [ ] pattern/petal pattern upgrades
- [ ] mana storage upgrades
- [ ] energy/storage upgrades where the target machine actually uses them
- [ ] greenhouse/flower-specific upgrades if those machines survive the 1.7.10 design pass

## Phase 5 — tiered sparks

Reference transfer-rate progression from Extra Reforked:

- vanilla/base reference: 1,000
- Malachite: 25,000
- Saffron: 100,000
- Shadow: 500,000
- Crimson: 1,000,000
- Aureate: 2,500,000
- Mazarine: 5,000,000

The old Botania `EntitySpark` implementation uses different internals from modern Botania. These will be implemented natively for 1.7.10 rather than trying to patch a private transfer constant at runtime.

## Phase 6 — AE2 rv3-beta-6 integration

- [ ] native AE2 grid node where appropriate
- [ ] crafting-provider support
- [ ] pattern handling
- [ ] safe item injection/extraction
- [ ] automatic output return to ME
- [ ] channel/power behaviour appropriate for AE2 rv3-beta-6
- [ ] no GTNH-only AE2 API assumptions

## After Botania

The same repository will later host separate integration modules/families for:

- Blood Magic: BMAddon / NeoVitae behaviour plus 1.7.10 server-mod references
- Thaumcraft 4: compact Infusion Matrix / Matrix Assembler style automation, then Arcane/Crucible automation where useful

## Asset rule

Do **not** commit the graphical interface textures from Botanical Machinery Extra Reforked. Its upstream README explicitly reserves those GUI textures. During development use original/placeholder UI assets; final artwork will be original for Divine Machinery Legacy.
