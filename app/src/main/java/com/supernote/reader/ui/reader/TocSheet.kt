package com.supernote.reader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DividerColor = Color(0xFFCCCCCC)

@Composable
fun TocSheet(
    toc: List<TocEntry>,
    currentPage: Int,
    onJump: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    // Index of the active chapter: last entry whose pageIndex ≤ currentPage
    val activeIdx = toc.indexOfLast { it.pageIndex <= currentPage }.coerceAtLeast(0)
    val listState = rememberLazyListState()

    LaunchedEffect(activeIdx) {
        if (toc.isNotEmpty()) listState.scrollToItem(activeIdx)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .background(Color.White)
            .border(1.dp, Color.Black),
    ) {
        // Header
        Box(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 28.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text("Chapters", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))

        // Chapter list
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
        ) {
            itemsIndexed(toc) { idx, entry ->
                val isActive = idx == activeIdx
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(if (isActive) Color(0xFFF0F0F0) else Color.White)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onJump(entry.pageIndex) }
                        .padding(horizontal = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = entry.title,
                        fontSize = 14.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "p.${entry.pageIndex + 1}",
                        fontSize = 11.sp,
                        color = Color(0xFF888888),
                    )
                }
                if (idx < toc.lastIndex) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))

        // Close button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("Close", fontSize = 15.sp, color = Color.Black)
        }
    }
}
