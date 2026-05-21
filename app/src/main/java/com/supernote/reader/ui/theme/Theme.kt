package com.supernote.reader.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EInkColorScheme = lightColorScheme(
    background        = Color.White,
    surface           = Color.White,
    onBackground      = Color.Black,
    onSurface         = Color.Black,
    primary           = Color.Black,
    onPrimary         = Color.White,
    secondary         = Color(0xFF888888),
    outline           = Color(0xFFCCCCCC),
)

@Composable
fun SupernoteReaderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EInkColorScheme,
        content = content,
    )
}
