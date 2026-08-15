#!/usr/bin/env bash
set -euo pipefail

UPSTREAM_URL="https://github.com/BOLTMAGIC/Botanical-Machinery-Extra-Reforked.git"
UPSTREAM_REF="master"
TMP_DIR="${TMPDIR:-/tmp}/divine-machinery-reforked-assets"
TARGET="src/main/resources/assets/divinemachinerylegacy/textures"

rm -rf "$TMP_DIR"
git clone --depth 1 --branch "$UPSTREAM_REF" "$UPSTREAM_URL" "$TMP_DIR"
SRC="$TMP_DIR/src/main/resources/assets/botanicalextramachinery/textures"

# Modern Minecraft uses textures/block and textures/item. Forge 1.7.10 expects
# textures/blocks and textures/items for IIcon registration. GUI paths remain GUI.
mkdir -p "$TARGET/gui/reforked" "$TARGET/blocks/reforked" "$TARGET/items/reforked"
cp -a "$SRC/gui/."   "$TARGET/gui/reforked/"
cp -a "$SRC/block/." "$TARGET/blocks/reforked/"
cp -a "$SRC/item/."  "$TARGET/items/reforked/"

printf 'Vendored permitted Extra Reforked visual assets for this build.\n'
find "$TARGET" -type f | sort
