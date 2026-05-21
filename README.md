# SupernoteReader

A minimal, open-source ebook reader built specifically for the **Supernote A6X2 Nomad** (and compatible Supernote devices). Designed from the ground up for e-ink — no animations, no cloud, no accounts.

Supports EPUB and PDF. Everything is local.

---

## Features

**Library**
- Scans `/Document/Books/` automatically on launch
- Cover art extracted from EPUB metadata and PDF first page
- Author name displayed on each card
- Reading progress bar per book
- Sort by title (A–Z), author, or recently read

**Reader**
- EPUB: full text reflow with 5 font families and adjustable size (14–28 sp)
- EPUB: table of contents navigation — tap `☰` in the bottom bar to jump to any chapter
- PDF: full-page rendering at native resolution
- Per-book dark mode (inverts all colours — text, cover, PDF pages)
- Font and theme changes are live — see the effect behind the settings modal before confirming
- Sidebar buttons (the physical strips on the Nomad) turn pages
- Full-screen e-ink refresh every 8 page turns to clear ghosting

**E-ink first**
- Zero animations anywhere in the app
- Hard shadows only (no blur)
- Stylus resting on screen does not trigger navigation
- Static loading states — no spinners

---

## Building

### Requirements

- macOS or Linux
- Android Studio installed (provides the JDK and Android SDK)
- A USB-C cable and a Supernote Nomad with sideloading enabled

### First-time setup

```bash
git clone https://github.com/your-username/SupernoteReader.git
cd SupernoteReader
bash setup.sh
```

`setup.sh` downloads two things that are not committed to the repo:
- `gradle/wrapper/gradle-wrapper.jar`
- Font files into `app/src/main/res/font/` (Literata, Merriweather, Lora, Source Serif 4, Atkinson Hyperlegible)

### Build

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

If Gradle can't find Java, prefix with the Android Studio JDK path:

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug
```

For a release build:

```bash
./gradlew assembleRelease
```

---

## Installing on the Nomad

The Nomad runs Chauvet OS (Android 11) with no Google Play Store. Installation is via ADB.

### 1. Enable sideloading on the Nomad

**Settings → Security & Privacy → Sideloading → ON**

### 2. Connect via USB-C

Plug the Nomad into your computer. It will not mount as a storage volume on Mac — that's expected. ADB works regardless.

On first connection, the Nomad may show an "Allow USB debugging?" prompt. Accept it.

### 3. Verify the connection

```bash
adb devices
# Should show something like: 192.168.x.x:5555   device
```

### 4. Install

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

The `-r` flag reinstalls over an existing version without losing data.

> The Nomad's built-in file manager cannot install APKs directly — ADB is required.

---

## Adding books

The app creates `/Document/Books/` on first launch. This folder is visible in the Supernote file manager under **Document → Books**.

**Via ADB:**
```bash
adb push book.epub /storage/emulated/0/Document/Books/book.epub
adb push book.pdf  /storage/emulated/0/Document/Books/book.pdf
```

**Via the Supernote web interface or USB transfer:**
Copy `.epub` or `.pdf` files into `Document/Books/` using whatever transfer method you use for your Nomad.

Reading progress is keyed by absolute file path. Renaming or moving a file resets its progress.

---

## Usage

### Navigation
- **Sidebar buttons** — the physical strips on the right edge of the Nomad turn pages (forward / back)
- **← Back** in the toolbar returns to the library

### Font & theme settings
Tap **Font** in the top-right of the toolbar. Changes apply live — the book updates behind the modal so you can judge the effect before confirming. Cancel reverts everything. Theme (light/dark) is per-book.

### Table of contents (EPUB only)
Tap **☰** on the left side of the bottom bar. The chapter list opens, scrolled to the current chapter. Tap any entry to jump there instantly.

### Sort order (library)
Tap the sort label in the top-right of the library header to cycle through:
- **A–Z** — alphabetical by title
- **Author** — alphabetical by author, then title
- **Recent** — books you've read most recently first

---

## Architecture

Single-activity app. Jetpack Compose + Navigation. Two screens: Gallery → Reader.

```
com.supernote.reader
├── MainActivity          Entry point; hides system bars; routes sidebar key events
├── AppNavigation         NavHost (gallery ↔ reader/{bookPath})
├── PageTurnRelay         Singleton SharedFlow bridging Activity key events to the reader
├── data/
│   ├── BookItem          Domain model (title, author, cover, progress, lastRead)
│   ├── BookFormat        EPUB | PDF
│   ├── BookRepository    Scans Books/ folder; parallel cover extraction; in-memory cache
│   └── ProgressStore     DataStore: page, total, font, font size, theme, lastRead per book
├── util/
│   ├── EpubParser        ZipFile + Jsoup: single-pass extraction of meta + spine + TOC
│   ├── EpubPaginator     Character-count pagination; font-size aware; per-spine-item tracking
│   └── PdfPaginator      PdfRenderer wrapper
└── ui/
    ├── gallery/
    │   ├── BookGalleryScreen    4-column grid; sort controls; lifecycle-aware refresh
    │   ├── BookGalleryViewModel Parallel scan; sort modes (Alpha, Author, Recent)
    │   └── BookCard             Cover + author + title + progress bar
    └── reader/
        ├── BookReaderScreen     Toolbar + content + TOC/font modals
        ├── BookReaderViewModel  EPUB repagination on font change; TOC resolution; page jumps
        ├── EpubReaderView       Renders text blocks; dark mode colour inversion
        ├── PdfReaderView        Renders PDF page bitmap via Coil; dark mode colour filter
        ├── TocSheet             Chapter list modal; scrolls to current chapter
        └── ReaderOptionsSheet   Font/size/theme picker; live preview; cancel reverts
```

**Key constraints (e-ink device)**
- No animations — all transitions are instant state swaps
- No ripple or touch feedback
- Stylus touch events ignored for navigation (pen resting on screen)
- Full-screen white flash every 8 page turns to clear e-ink ghosting
- Hard-offset shadows only (2×3 dp solid, no blur)

---

## Tech stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Compose Navigation |
| Persistence | DataStore Preferences |
| Image loading | Coil 2 |
| EPUB parsing | Jsoup (custom ZIP + OPF/NCX/NAV parser) |
| PDF rendering | Android PdfRenderer |
| Fonts | Literata, Merriweather, Lora, Source Serif 4, Atkinson Hyperlegible |

- Min SDK / Target SDK: 30 (Android 11)
- Compile SDK: 34
- Kotlin 2.0 / AGP 8.5

---

## Target device

**Supernote A6X2 Nomad**
- Chauvet OS (Android 11, no GMS)
- 7.8" E Ink, 1404 × 1872 px @ 300 PPI
- Wacom EMR stylus
- Physical sidebar buttons (ratta-slide input device, key codes 310 / 301)

The app should work on other Android 11+ devices but is not optimised for colour LCD displays.

---

## License

MIT
