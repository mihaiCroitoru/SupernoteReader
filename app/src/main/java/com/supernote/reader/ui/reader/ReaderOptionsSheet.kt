package com.supernote.reader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
            .fillMaxWidth(0.88f)
            .wrapContentHeight()
            .background(Color.White)
            .border(1.dp, Color.Black),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Left 2/3 — font grid
            Column(modifier = Modifier.weight(2f)) {
                SectionLabel("Font")
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    fonts.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { name ->
                                val selected = name == currentFont
                                val family = ReaderFonts[name] ?: FontFamily.Default
                                Chip(
                                    label = name,
                                    fontFamily = family,
                                    selected = selected,
                                    onClick = { onFontChange(name) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            // pad last row if odd number of fonts
                            repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(BorderColor),
            )

            // Right 1/3 — theme + size
            Column(modifier = Modifier.weight(1f)) {
                SectionLabel("Theme")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Light", "Dark").forEach { name ->
                        val selected = if (name == "Dark") isDark else !isDark
                        Chip(
                            label = name,
                            selected = selected,
                            onClick = { onThemeChange(name == "Dark") },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                SectionLabel("Size")
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SizeButton("-") { onSizeChange(currentSize - 2) }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .border(1.dp, BorderColor),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$currentSize", fontSize = 18.sp, color = Color.Black)
                    }
                    SizeButton("+") { onSizeChange(currentSize + 2) }
                }
            }
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
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color.Black else BorderColor,
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontFamily = fontFamily,
            fontSize = 14.sp,
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
            .width(40.dp)
            .height(40.dp)
            .border(1.dp, BorderColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, fontSize = 22.sp, color = Color.Black)
    }
}
