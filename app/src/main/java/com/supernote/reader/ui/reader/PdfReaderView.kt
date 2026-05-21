package com.supernote.reader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.supernote.reader.util.renderPdfPage
import java.io.File

private val InversionFilter = ColorFilter.colorMatrix(
    ColorMatrix(floatArrayOf(
        -1f, 0f, 0f, 0f, 1f,
         0f,-1f, 0f, 0f, 1f,
         0f, 0f,-1f, 0f, 1f,
         0f, 0f, 0f, 1f, 0f,
    ))
)

@Composable
fun PdfReaderView(
    file: File,
    pageIndex: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    var bitmap by remember(pageIndex) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(pageIndex) {
        bitmap = withContext(Dispatchers.IO) {
            runCatching { renderPdfPage(file, pageIndex, targetWidth = 1404) }.getOrNull()
        }
    }

    val bgColor = if (isDark) Color.Black else Color.White
    Box(
        modifier = modifier.fillMaxSize().background(bgColor),
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = if (isDark) InversionFilter else null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
