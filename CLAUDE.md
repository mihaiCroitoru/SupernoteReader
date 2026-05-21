# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# First-time setup (downloads gradle-wrapper.jar + all fonts)
bash setup.sh

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Lint
./gradlew lint

# Deploy to connected Supernote Nomad via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Push a test book to the device
adb push "book.epub" /storage/emulated/0/Documents/Books/book.epub

# Watch logcat (filter to this app)
adb logcat --pid=$(adb shell pidof -s com.supernote.reader)
```

## Toolchain Requirements

No Java/Android SDK installed? Install Android Studio first:
```bash
brew install --cask android-studio
```
Open Android Studio once to complete SDK setup, then `bash setup.sh` and build.

## Architecture

Single-activity app. Jetpack Compose + Navigation. Two screens: Gallery → Reader.

```
com.supernote.reader
├── MainActivity          entry point; hides system bars; sets fullscreen
├── AppNavigation         NavHost wiring (gallery ↔ reader/{bookPath})
├── data/
│   ├── BookItem          domain model; holds progress: Float (derived)
│   ├── BookFormat        EPUB | PDF
│   ├── BookRepository    scans Books/ folder, extracts covers, loads progress
│   └── ProgressStore     DataStore wrapper; keys: page:path, total:path, font, font_size
├── util/
│   ├── EpubPaginator     epublib + Jsoup → List<EpubPage> (char-count pagination)
│   └── PdfPaginator      PdfRenderer wrapper; renderPdfPage() + getPdfPageCount()
└── ui/
    ├── theme/
    │   ├── Theme          B&W MaterialTheme; no color
    │   └── Type           5 FontFamily definitions + ReaderFonts map
    ├── gallery/
    │   ├── BookGalleryScreen   4-column LazyVerticalGrid; handles permission request
    │   ├── BookGalleryViewModel  scans on init; exposes GalleryState StateFlow
    │   └── BookCard        cover + title + format badge + 4dp progress bar
    └── reader/
        ├── BookReaderScreen    tap-zone navigation; options sheet toggle; refresh flash
        ├── BookReaderViewModel  pagination, page turns, progress save, font/size
        ├── EpubReaderView      renders EpubPage blocks as Compose Text
        ├── PdfReaderView       renders PDF page Bitmap via LaunchedEffect
        └── ReaderOptionsSheet  instant-show bottom sheet; font chips + size ±2
```

## Key Design Constraints (e-ink device)

- **Zero animations.** No `AnimatedVisibility`, `Crossfade`, `animateContentSize`, `Ripple`. All transitions are instant state swaps.
- **Stylus events ignored for navigation.** All tap-zone handlers check `MotionEvent.TOOL_TYPE_STYLUS` and skip. Users rest the Wacom pen on screen while reading.
- **Full-screen refresh every 8 page turns** (`refreshFlash` state in ViewModel) — blank white frame clears e-ink ghosting.
- **Grayscale covers only.** `toGrayscale()` extension on `Bitmap` applied after every cover decode.
- **Hard shadow only.** `drawBehind` in `BookCard` draws a 2×3dp solid `#CCCCCC` offset — zero blur. Soft shadows don't render on e-ink.
- **Progress bar = bottom border.** Card has `border-bottom: none`; the 4dp `Box` at card bottom uses `#CCCCCC` track + `#000000` fill. At 0% it is visually a plain border.

## Books Folder

App creates `/storage/emulated/0/Documents/Books/` on first launch. Drop `.epub` or `.pdf` files there. Progress is keyed by absolute file path — renaming a file resets its progress.

## Fonts

Fonts live in `app/src/main/res/font/`. Downloaded by `setup.sh`. Five families: Literata, Merriweather, Lora, Source Serif 4, Atkinson Hyperlegible. Size range: 14–28sp, step 2. Both persisted in DataStore.

## Target Device

Supernote A6X2 Nomad — Android 11 (Chauvet OS), 7.8" E Ink 1404×1872 @ 300 PPI, Wacom EMR stylus, no GMS.
