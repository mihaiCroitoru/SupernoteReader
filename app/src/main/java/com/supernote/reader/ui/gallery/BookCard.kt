package com.supernote.reader.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.supernote.reader.R
import com.supernote.reader.data.BookItem

private val CardShape = RoundedCornerShape(6.dp)
private val BorderColor = Color(0xFFCCCCCC)
private val ShadowColor = Color(0xFFCCCCCC)
private val PlaceholderBg = Color(0xFFEFEFEF)
private val DividerColor = Color(0xFFE8E8E8)
private val ProgressColor = Color.Black

@Composable
fun BookCard(book: BookItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .clip(CardShape)
            .drawBehind {
                // border (no bottom — progress bar replaces it)
                val stroke = 1.dp.toPx()
                val shadow = 1.dp.toPx()
                // hard-offset shadow (e-ink safe: no blur)
                drawRect(color = ShadowColor, topLeft = Offset(2.dp.toPx(), 3.dp.toPx()))
                // top border
                drawLine(BorderColor, Offset(0f, 0f), Offset(size.width, 0f), stroke)
                // left border
                drawLine(BorderColor, Offset(0f, 0f), Offset(0f, size.height), stroke)
                // right border
                drawLine(BorderColor, Offset(size.width, 0f), Offset(size.width, size.height), stroke)
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
    ) {
        // Cover
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(PlaceholderBg),
            contentAlignment = Alignment.Center,
        ) {
            if (book.cover != null) {
                AsyncImage(
                    model = book.cover,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                AsyncImage(
                    model = R.drawable.ic_book_placeholder,
                    contentDescription = null,
                )
            }
        }

        // Divider
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))

        // Title + author
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = book.title,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = if (book.author.isBlank()) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = Color.Black,
            )
            if (book.author.isNotBlank()) {
                Text(
                    text = book.author,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF888888),
                )
            }
        }

        // Progress bar — replaces bottom border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(BorderColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(book.progress)
                    .height(4.dp)
                    .background(ProgressColor),
            )
        }
    }
}
