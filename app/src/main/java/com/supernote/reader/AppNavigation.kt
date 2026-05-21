package com.supernote.reader

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.supernote.reader.ui.gallery.BookGalleryScreen
import com.supernote.reader.ui.reader.BookReaderScreen

@Composable
fun AppNavigation() {
    val nav = rememberNavController()

    NavHost(nav, startDestination = "gallery") {
        composable("gallery") {
            BookGalleryScreen(
                onBookOpen = { path ->
                    nav.navigate("reader/${Uri.encode(path)}")
                }
            )
        }
        composable("reader/{bookPath}") { back ->
            val path = Uri.decode(back.arguments?.getString("bookPath") ?: "")
            BookReaderScreen(
                bookPath = path,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
