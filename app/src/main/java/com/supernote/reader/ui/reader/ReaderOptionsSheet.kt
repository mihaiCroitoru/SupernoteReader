package com.supernote.reader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.supernote.reader.ui.theme.ReaderFonts

private val BorderColor = Color(0xFFCCCCCC)
private val LabelColor = Color(0xFF888888)
private val fonts = ReaderFonts.keys.toList()
private val themes = listOf("Light", "Dark")

@Composable
fun ReaderOptionsSheet(
    currentFont: String,
    currentSize: Int,
    isDark: Boolean,
    onFontChange: (String) -> Unit,
    onSizeChange: (Int) -> Unit,
    onThemeChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.82f)
            .wrapContentHeight()
            .background(Color.White)
            .border(1.dp, Color.Black),
    ) {
        Column(modifier = Modifier.padding(28.dp)) {

            SectionLabel("Font")
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(fonts) { name ->
                    val selected = name == currentFont
                    val family = ReaderFonts[name] ?: FontFamily.Default
                    Chip(
                        label = name,
                        fontFamily = family,
                        selected = selected,
                        onClick = { onFontChange(name) },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionLabel("Size")
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                SizeButton("-") { onSizeChange(currentSize - 2) }
                Box(
                    modifier = Modifier.width(88.dp).height(64.dp).border(1.dp, BorderColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = "$currentSize", fontSize = 22.sp, color = Color.Black)
                }
                SizeButton("+") { onSizeChange(currentSize + 2) }
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "The quick brown fox…",
                    fontSize = currentSize.sp,
                    color = LabelColor,
                    maxLines = 1,
                )
            }

            Spacer(Modifier.height(20.dp))

            SectionLabel("Theme")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                themes.forEach { name ->
                    val selected = if (name == "Dark") isDark else !isDark
                    Chip(
                        label = name,
                        selected = selected,
                        onClick = { onThemeChange(name == "Dark") },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderColor))

        Row(modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("Cancel", fontSize = 16.sp, color = Color.Black)
            }
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(BorderColor))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onConfirm,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("OK", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
private fun Chip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    fontFamily: FontFamily = FontFamily.Default,
) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color.Black else BorderColor,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = fontFamily,
            fontSize = 15.sp,
            color = Color.Black,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text.uppercase(), fontSize = 11.sp, letterSpacing = 0.14.sp, color = LabelColor)
}

@Composable
private fun SizeButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(64.dp)
            .height(64.dp)
            .border(1.dp, BorderColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, fontSize = 26.sp, color = Color.Black)
    }
}
