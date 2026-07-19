#!/bin/bash
# UmbraQRGen Logo Deployment Script
# Uses macOS built-in 'sips' — no ImageMagick needed
# Run from the project root: bash apply_logo.sh

set -e

LOGO="UmbraQRGen_logo.png"
RES="app/src/main/res"

if [ ! -f "$LOGO" ]; then
  echo "ERROR: $LOGO not found in current directory."
  echo "Make sure you run this from the BDQRgenerate project root."
  exit 1
fi

echo "Deploying $LOGO to all mipmap densities..."

# mdpi: 48x48
sips -z 48 48 "$LOGO" --out "$RES/mipmap-mdpi/ic_launcher.png"
sips -z 48 48 "$LOGO" --out "$RES/mipmap-mdpi/ic_launcher_round.png"
echo "  ✓ mipmap-mdpi (48x48)"

# hdpi: 72x72
sips -z 72 72 "$LOGO" --out "$RES/mipmap-hdpi/ic_launcher.png"
sips -z 72 72 "$LOGO" --out "$RES/mipmap-hdpi/ic_launcher_round.png"
echo "  ✓ mipmap-hdpi (72x72)"

# xhdpi: 96x96
sips -z 96 96 "$LOGO" --out "$RES/mipmap-xhdpi/ic_launcher.png"
sips -z 96 96 "$LOGO" --out "$RES/mipmap-xhdpi/ic_launcher_round.png"
echo "  ✓ mipmap-xhdpi (96x96)"

# xxhdpi: 144x144
sips -z 144 144 "$LOGO" --out "$RES/mipmap-xxhdpi/ic_launcher.png"
sips -z 144 144 "$LOGO" --out "$RES/mipmap-xxhdpi/ic_launcher_round.png"
echo "  ✓ mipmap-xxhdpi (144x144)"

# xxxhdpi: 192x192
sips -z 192 192 "$LOGO" --out "$RES/mipmap-xxxhdpi/ic_launcher.png"
sips -z 192 192 "$LOGO" --out "$RES/mipmap-xxxhdpi/ic_launcher_round.png"
echo "  ✓ mipmap-xxxhdpi (192x192)"

# Watermark background drawable
cp "$LOGO" "$RES/drawable/watermark_bg.png"
echo "  ✓ drawable/watermark_bg.png"

echo ""
echo "Done. All launcher icons and watermark updated with UmbraQRGen_logo.png"
