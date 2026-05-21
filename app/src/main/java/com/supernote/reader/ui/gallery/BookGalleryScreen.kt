package com.supernote.reader.ui.gallery

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BookGalleryScreen(
    onBookOpen: (String) -> Unit,
    vm: BookGalleryViewModel = viewModel(),
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    // Refresh whenever this screen enters the foreground — covers first launch and
    // returning from the reader so progress bars and new books are always current.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { vm.refresh() }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.White)
                .padding(horizontal = 36.dp),
        ) {
            Text(
                text = "SupernoteReader",
                fontSize = 21.sp,
                letterSpacing = 0.05.sp,
                color = Color.Black,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            val sortLabel = when ((state as? GalleryState.Ready)?.sortMode) {
                SortMode.Author -> "Author ↕"
                SortMode.Recent -> "Recent ↕"
                else            -> "A–Z ↕"
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { vm.cycleSortMode() },
                contentAlignment = Alignment.Center,
            ) {
                Text(sortLabel, fontSize = 13.sp, color = Color(0xFF888888))
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Black))

        when (val s = state) {
            is GalleryState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Scanning library…", color = Color(0xFF888888))
                }
            }

            is GalleryState.PermissionRequired -> {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        val intent = try {
                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                        } catch (_: Exception) {
                            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        }
                        settingsLauncher.launch(intent)
                    }
                }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Storage permission required", color = Color(0xFF888888))
                }
            }

            is GalleryState.Ready -> {
                if (s.books.isEmpty()) {
                    EmptyState()
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(
                            start = 36.dp, end = 36.dp, top = 40.dp, bottom = 40.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(36.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(s.books, key = { it.file.absolutePath }) { book ->
                            BookCard(
                                book = book,
                                onClick = { onBookOpen(book.file.absolutePath) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("No books found", fontSize = 16.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text(
            "Add .epub or .pdf files to:\nDocument/Books/",
            fontSize = 14.sp,
            color = Color(0xFF888888),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace,
            lineHeight = 22.sp,
        )
    }
}
