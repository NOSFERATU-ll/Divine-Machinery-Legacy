# BloodMagic Additions 1.0.4 -> Minecraft 1.7.10 port status

Reference: `bmaddon-1.0.4` (BloodMagic Additions), ported for Blood Magic 1.7.10 + AE2 rv3.

## Implemented

- Blood Altar Assembler / Blood Assembler
  - 9 Blood Pattern slots
  - 9 upgrade slots
  - Blood Altar tier cards II-V
  - AE2 Speed Cards (max 4 installed)
  - Blood Magic Speed Cards (max 9 installed, each counts as four speed cards)
  - Parallel Processing Cards (max 4 installed)
  - configurable parallel limits, craft time, AE/t and LP multiplier
  - per-active-craft AE consumption
  - pending crafts do not consume AE
  - direct completed-output return to the connected ME network
  - network power light state
  - covered-cable connection rendering
  - memory-card copy/paste of the 9 Blood Pattern slots
  - pattern-only crafting; ordinary AE2 processing patterns are rejected by the machine
  - exposed sided inventory is the Blood Pattern inventory, matching the modern AE2 block entity
  - original permitted assembler textures and animated light layer
  - 1.7.10 recreation of the modern AE2 screen layout

- Blood Patterns
  - blank and encoded states with original permitted textures/animation
  - dynamic stack limit (64 blank / 1 encoded)
  - Blood Altar recipes
  - Blood Magic 1.7.10 Alchemy/Writing Table recipes
  - stale recipes are warned about in the tooltip and are not advertised to AE2
  - Shift + right click clears a pattern
  - exact blank-pattern crafting recipe (four Weak Blood Shards around an AE2 blank pattern)
  - because AE2 rv3 cannot inject a foreign blank pattern into its Pattern Encoding Terminal, the 1.7.10 adaptation copies an already encoded AE2 processing pattern in a crafting grid and returns the source AE2 pattern

- Blood Generator
  - original permitted block model, texture and GUI
  - RF input through CoFH 1.7.10 energy API
  - output-only Life Essence Forge-fluid tank
  - proportional partial generation
  - optional automatic output to adjacent fluid handlers
  - exact BloodMagic Additions 1.0.4 defaults
  - server -> client common generator config sync on login, respawn, dimension change and GUI open

- Recipe / viewer integration
  - exact Blood Altar progression recipes for the assembler and upgrade cards, translated to Blood Magic 1.7.10 tier numbering
  - NEI recipe-catalyst bridge for Blood Altar and Alchemy recipes

- Config
  - all 16 `bmaddon-common.toml` settings have 1.7.10 equivalents in `divinemachinerylegacy-bloodmagic.cfg`, with matching defaults and ranges

## Version-specific adaptations

These are compatibility translations rather than omitted features:

1. Modern AE2 represents Life Essence directly as a fluid ingredient in an encoded crafting pattern. AE2 rv3 has no equivalent generic-fluid crafting input, so the 1.7.10 assembler exposes a Life Essence fluid buffer and drains the required LP when AE2 starts a Blood Pattern craft.
2. Modern Blood Patterns are encoded directly in the AE2 Pattern Encoding Terminal by a mixin. AE2 rv3 hard-codes its own blank pattern, so the 1.7.10 port uses the copy recipe described above.
3. Modern Blood Magic has an Alchemy Table recipe API. Blood Magic 1.7.10 uses the Writing Table/Alchemy recipe API; its `amountNeeded` is LP per progress tick, so the port converts it to the real 100-tick total before applying the configurable LP multiplier.
4. Modern JEI integration is represented by NEI integration on 1.7.10.
5. Modern FE is represented by CoFH RF on 1.7.10.

## Test gate

The code port is considered complete when the current `blood-magic-port` GitHub Actions build is green. After that, the remaining work is runtime testing inside the actual modpack: block placement/rendering, GUI interaction, RF/fluid IO, one altar pattern, one alchemy pattern, card limits/speed/parallel behavior, memory card copy/paste and an AE2 autocrafting request.
