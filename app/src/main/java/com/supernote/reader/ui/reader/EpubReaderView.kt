package com.supernote.reader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.supernote.reader.ui.theme.ReaderFonts
import com.supernote.reader.util.EpubPage
import com.supernote.reader.util.TextBlock

private val InversionFilter = ColorFilter.colorMatrix(
    ColorMatrix(floatArrayOf(
        -1f, 0f, 0f, 0f, 1f,
         0f,-1f, 0f, 0f, 1f,
         0f, 0f,-1f, 0f, 1f,
         0f, 0f, 0f, 1f, 0f,
    ))
)

@Composable
fun EpubReaderView(
    page: EpubPage,
    fontName: String,
    fontSize: Int,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isDark) Color.Black else Color.White
    val textColor = if (isDark) Color.White else Color.Black

    if (page.coverBitmap != null) {
        AsyncImage(
            model = page.coverBitmap,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = if (isDark) InversionFilter else null,
            modifier = modifier.fillMaxSize().background(bgColor),
        )
        return
    }

    val family = ReaderFonts[fontName] ?: FontFamily.Serif
    val lineHeight = (fontSize.toFloat() * 1.75f).sp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 28.dp, vertical = 24.dp),
    ) {
        page.blocks.forEach { block ->
            when (block) {
                is TextBlock.Heading -> {
                    Text(
                        text = block.text,
                        fontFamily = family,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = (fontSize + 4).sp,
                        lineHeight = ((fontSize + 4).toFloat() * 1.4f).sp,
                        color = textColor,
                        letterSpacing = 0.06.sp,
                    )
                    Spacer(Modifier.height(20.dp))
                }
                is TextBlock.Paragraph -> {
                    Text(
                        text = block.text,
                        fontFamily = family,
                        fontWeight = FontWeight.Normal,
                        fontSize = fontSize.sp,
                        lineHeight = lineHeight,
                        color = textColor,
                    )
                    Spacer(Modifier.height((fontSize * 0.6f).dp))
                }
            }
        }
    }
}
