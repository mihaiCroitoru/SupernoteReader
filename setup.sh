#!/usr/bin/env bash
# Run once after cloning. Downloads gradle wrapper jar + font files.
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "→ Downloading Gradle wrapper jar…"
mkdir -p "$ROOT/gradle/wrapper"
curl -fsSL \
  "https://github.com/gradle/gradle/raw/v8.7.0/gradle/wrapper/gradle-wrapper.jar" \
  -o "$ROOT/gradle/wrapper/gradle-wrapper.jar"
chmod +x "$ROOT/gradlew"

echo "→ Downloading fonts…"
FONT_DIR="$ROOT/app/src/main/res/font"
mkdir -p "$FONT_DIR"

BASE_LIT="https://github.com/googlefonts/literata/raw/main/fonts/ttf"
curl -fsSL "$BASE_LIT/Literata-Regular.ttf"  -o "$FONT_DIR/literata_regular.ttf"
curl -fsSL "$BASE_LIT/Literata-Italic.ttf"   -o "$FONT_DIR/literata_italic.ttf"
curl -fsSL "$BASE_LIT/Literata-SemiBold.ttf" -o "$FONT_DIR/literata_semibold.ttf"

BASE_MW="https://github.com/SorkinType/Merriweather/raw/master/fonts/ttf"
curl -fsSL "$BASE_MW/Merriweather-Regular.ttf" -o "$FONT_DIR/merriweather_regular.ttf"
curl -fsSL "$BASE_MW/Merriweather-Italic.ttf"  -o "$FONT_DIR/merriweather_italic.ttf"
curl -fsSL "$BASE_MW/Merriweather-Bold.ttf"    -o "$FONT_DIR/merriweather_bold.ttf"

# Lora is a variable font in google/fonts — brackets must be URL-encoded
BASE_LORA="https://github.com/google/fonts/raw/main/ofl/lora"
curl -fsSL "$BASE_LORA/Lora%5Bwght%5D.ttf"        -o "$FONT_DIR/lora_regular.ttf"
curl -fsSL "$BASE_LORA/Lora-Italic%5Bwght%5D.ttf" -o "$FONT_DIR/lora_italic.ttf"

BASE_SS="https://github.com/adobe-fonts/source-serif/raw/release/TTF"
curl -fsSL "$BASE_SS/SourceSerif4-Regular.ttf"  -o "$FONT_DIR/source_serif_regular.ttf"
curl -fsSL "$BASE_SS/SourceSerif4-It.ttf"       -o "$FONT_DIR/source_serif_italic.ttf"
curl -fsSL "$BASE_SS/SourceSerif4-Semibold.ttf" -o "$FONT_DIR/source_serif_semibold.ttf"

BASE_AT="https://github.com/googlefonts/atkinson-hyperlegible/raw/main/fonts/ttf"
curl -fsSL "$BASE_AT/AtkinsonHyperlegible-Regular.ttf" -o "$FONT_DIR/atkinson_regular.ttf"
curl -fsSL "$BASE_AT/AtkinsonHyperlegible-Italic.ttf"  -o "$FONT_DIR/atkinson_italic.ttf"
curl -fsSL "$BASE_AT/AtkinsonHyperlegible-Bold.ttf"    -o "$FONT_DIR/atkinson_bold.ttf"

BASE_OD="https://raw.githubusercontent.com/antijingoist/opendyslexic/main/compiled"
curl -fsSL "$BASE_OD/OpenDyslexic-Regular.otf" -o "$FONT_DIR/opendyslexic_regular.otf"
curl -fsSL "$BASE_OD/OpenDyslexic-Italic.otf"  -o "$FONT_DIR/opendyslexic_italic.otf"
curl -fsSL "$BASE_OD/OpenDyslexic-Bold.otf"    -o "$FONT_DIR/opendyslexic_bold.otf"

echo "✓ Setup complete. Open in Android Studio and build."
