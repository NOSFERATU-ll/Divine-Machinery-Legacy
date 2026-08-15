#!/usr/bin/env bash
set -euo pipefail

UPSTREAM_URL="https://github.com/BOLTMAGIC/Botanical-Machinery-Extra-Reforked.git"
# Pinned after comparing the permitted artwork with botanicalextramachinery-1.2.9.9.jar.
# Do not follow master automatically: development visuals should not drift under us.
UPSTREAM_REF="5f017fe066f2b85c1ebc139d5667579f9de420b7"
TMP_DIR="${TMPDIR:-/tmp}/divine-machinery-reforked-assets"
TARGET="src/main/resources/assets/divinemachinerylegacy/textures"

rm -rf "$TMP_DIR"
git clone --filter=blob:none --no-checkout "$UPSTREAM_URL" "$TMP_DIR"
git -C "$TMP_DIR" fetch --depth 1 origin "$UPSTREAM_REF"
git -C "$TMP_DIR" checkout --detach "$UPSTREAM_REF"
SRC="$TMP_DIR/src/main/resources/assets/botanicalextramachinery/textures"

# Modern Minecraft uses textures/block and textures/item. Forge 1.7.10 expects
# textures/blocks and textures/items for IIcon registration. GUI paths remain GUI.
mkdir -p "$TARGET/gui/reforked" "$TARGET/blocks/reforked" "$TARGET/items/reforked"
cp -a "$SRC/gui/."   "$TARGET/gui/reforked/"
cp -a "$SRC/block/." "$TARGET/blocks/reforked/"
cp -a "$SRC/item/."  "$TARGET/items/reforked/"

printf 'Vendored permitted Extra Reforked visual assets for this build.\n'
find "$TARGET" -type f | sort
