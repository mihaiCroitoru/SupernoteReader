package com.supernote.reader.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.supernote.reader.R

val LiterataFamily = FontFamily(
    Font(R.font.literata_regular, FontWeight.Normal),
    Font(R.font.literata_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.literata_semibold, FontWeight.SemiBold),
)

val MerriweatherFamily = FontFamily(
    Font(R.font.merriweather_regular, FontWeight.Normal),
    Font(R.font.merriweather_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.merriweather_bold, FontWeight.Bold),
)

// Lora is a variable font — same file covers all weights via the wght axis
val LoraFamily = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.lora_regular, FontWeight.SemiBold),
)

val SourceSerifFamily = FontFamily(
    Font(R.font.source_serif_regular, FontWeight.Normal),
    Font(R.font.source_serif_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.source_serif_semibold, FontWeight.SemiBold),
)

val AtkinsonFamily = FontFamily(
    Font(R.font.atkinson_regular, FontWeight.Normal),
    Font(R.font.atkinson_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.atkinson_bold, FontWeight.Bold),
)

val OpenDyslexicFamily = FontFamily(
    Font(R.font.opendyslexic_regular, FontWeight.Normal),
    Font(R.font.opendyslexic_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.opendyslexic_bold, FontWeight.Bold),
)

val ReaderFonts = mapOf(
    "Literata"      to LiterataFamily,
    "Merriweather"  to MerriweatherFamily,
    "Lora"          to LoraFamily,
    "Source Serif"  to SourceSerifFamily,
    "Atkinson"      to AtkinsonFamily,
    "OpenDyslexic"  to OpenDyslexicFamily,
)
