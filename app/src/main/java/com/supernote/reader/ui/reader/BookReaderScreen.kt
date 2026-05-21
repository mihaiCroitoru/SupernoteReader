package com.supernote.reader.ui.reader

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.supernote.reader.PageTurnRelay

private val DividerColor = Color(0xFFCCCCCC)

@Composable
fun BookReaderScreen(
    bookPath: String,
    onBack: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val vm: BookReaderViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val handle = androidx.lifecycle.SavedStateHandle(mapOf("bookPath" to bookPath))
                @Suppress("UNCHECKED_CAST")
                return BookReaderViewModel(
                    app = context.applicationContext as android.app.Application,
                    savedStateHandle = handle,
                ) as T
            }
        }
    )
    val state by vm.state.collectAsState()
    val title by vm.title.collectAsState()
    val fontName by vm.fontName.collectAsState()
    val fontSize by vm.fontSize.collectAsState()
    val isDark by vm.isDark.collectAsState()
    val refreshFlash by vm.refreshFlash.collectAsState()
    var showOptions by remember { mutableStateOf(false) }
    var showToc by remember { mutableStateOf(false) }
    var origFont by remember { mutableStateOf("") }
    var origSize by remember { mutableStateOf(0) }
    var origDark by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        PageTurnRelay.flow.collect { keyCode ->
            if (showOptions || showToc) return@collect
            when (keyCode) {
                310,  // ratta-slide forward (scan 249 / F30)
                KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_PAGE_DOWN -> vm.nextPage()
                301,  // ratta-slide back (scan 191 / F23)
                KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_PAGE_UP -> vm.prevPage()
            }
        }
    }

    if (refreshFlash) {
        Box(modifier = Modifier.fillMaxSize().background(if (isDark) Color.Black else Color.White))
        return
    }

    val bgColor = if (isDark) Color.Black else Color.White

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(modifier = Modifier.fillMaxSize()) {

            ReaderToolbar(
                title = title,
                isDark = isDark,
                onBack = onBack,
                onFont = {
                    origFont = fontName
                    origSize = fontSize
                    origDark = isDark
                    showOptions = true
                },
            )

            Box(modifier = Modifier.weight(1f)) { when (val s = state) {
                is ReaderState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Opening…", color = Color(0xFF888888))
                    }
                }

                is ReaderState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(s.message, color = Color(0xFF888888), textAlign = TextAlign.Center)
                    }
                }

                is ReaderState.EpubReady -> {
                    Column(Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            EpubReaderView(
                                page = s.pages[s.currentPage],
                                fontName = fontName,
                                fontSize = fontSize,
                                isDark = isDark,
                            )
                        }
                        PageIndicator(
                            current = s.currentPage + 1,
                            total = s.pages.size,
                            isDark = isDark,
                            onToc = if (s.toc.isNotEmpty()) { { showToc = true } } else null,
                        )
                    }
                }

                is ReaderState.PdfReady -> {
                    Column(Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            PdfReaderView(
                                file = java.io.File(bookPath),
                                pageIndex = s.currentPage,
                                isDark = isDark,
                            )
                        }
                        PageIndicator(
                            current = s.currentPage + 1,
                            total = s.totalPages,
                            isDark = isDark,
                            onToc = null,
                        )
                    }
                }
            } } // close weight(1f) Box
        } // close Column

        // Font/theme modal
        if (showOptions) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0x55000000)),
                contentAlignment = Alignment.Center,
            ) {
                ReaderOptionsSheet(
                    currentFont = fontName,
                    currentSize = fontSize,
                    isDark = isDark,
                    onFontChange = { vm.setFont(it) },
                    onSizeChange = { vm.setFontSize(it) },
                    onThemeChange = { vm.setTheme(it) },
                    onConfirm = { showOptions = false },
                    onDismiss = {
                        vm.setFont(origFont)
                        vm.setFontSize(origSize)
                        vm.setTheme(origDark)
                        showOptions = false
                    },
                )
            }
        }

        // TOC modal
        if (showToc) {
            val epubState = state as? ReaderState.EpubReady
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0x55000000)),
                contentAlignment = Alignment.Center,
            ) {
                TocSheet(
                    toc = epubState?.toc ?: emptyList(),
                    currentPage = epubState?.currentPage ?: 0,
                    onJump = { page ->
                        vm.jumpToPage(page)
                        showToc = false
                    },
                    onDismiss = { showToc = false },
                )
            }
        }
    }
}

@Composable
private fun ReaderToolbar(
    title: String,
    isDark: Boolean,
    onBack: () -> Unit,
    onFont: () -> Unit,
) {
    val bgColor  = if (isDark) Color.Black else Color.White
    val fgColor  = if (isDark) Color.White else Color.Black
    val divColor = if (isDark) Color(0xFF444444) else DividerColor

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).background(bgColor),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .padding(start = 28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("← Back", fontSize = 15.sp, color = fgColor)
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = fgColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .padding(end = 28.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onFont,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("Font", fontSize = 15.sp, color = fgColor)
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(divColor))
    }
}

@Composable
private fun PageIndicator(
    current: Int,
    total: Int,
    isDark: Boolean,
    onToc: (() -> Unit)?,
) {
    val bgColor  = if (isDark) Color.Black else Color.White
    val divColor = if (isDark) Color(0xFF444444) else DividerColor

    Column {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(divColor))
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp).background(bgColor),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // TOC button (EPUB only)
            if (onToc != null) {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .padding(start = 28.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToc,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("☰", fontSize = 18.sp, color = Color(0xFF888888))
                }
            }

            // Page counter — right-aligned
            Box(
                modifier = Modifier.weight(1f).padding(end = 28.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = "p. $current of $total",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                )
            }
        }
    }
}
