package com.supernote.reader.ui.gallery

import android.app.Application
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.supernote.reader.data.BookItem
import com.supernote.reader.data.BookRepository
import com.supernote.reader.data.ProgressStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class SortMode { Alpha, Author, Recent }

sealed class GalleryState {
    object Loading : GalleryState()
    object PermissionRequired : GalleryState()
    data class Ready(val books: List<BookItem>, val sortMode: SortMode) : GalleryState()
}

class BookGalleryViewModel(app: Application) : AndroidViewModel(app) {

    private val progressStore = ProgressStore(app)
    private val repository = BookRepository(progressStore)

    private val _state = MutableStateFlow<GalleryState>(GalleryState.Loading)
    val state: StateFlow<GalleryState> = _state

    private var rawBooks: List<BookItem> = emptyList()
    private var sortMode: SortMode = SortMode.Alpha

    fun refresh() {
        if (!isStorageGranted()) {
            _state.value = GalleryState.PermissionRequired
            return
        }
        viewModelScope.launch {
            _state.value = GalleryState.Loading
            rawBooks = repository.scanBooks()
            applySort()
        }
    }

    fun cycleSortMode() {
        sortMode = when (sortMode) {
            SortMode.Alpha   -> SortMode.Author
            SortMode.Author  -> SortMode.Recent
            SortMode.Recent  -> SortMode.Alpha
        }
        applySort()
    }

    private fun applySort() {
        val sorted = when (sortMode) {
            SortMode.Alpha  -> rawBooks.sortedBy { it.title.lowercase() }
            SortMode.Author -> rawBooks.sortedWith(compareBy({ it.author.lowercase() }, { it.title.lowercase() }))
            SortMode.Recent -> rawBooks.sortedByDescending { it.lastRead }
        }
        _state.value = GalleryState.Ready(sorted, sortMode)
    }

    private fun isStorageGranted(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            android.content.pm.PackageManager.PERMISSION_GRANTED ==
                androidx.core.content.ContextCompat.checkSelfPermission(
                    getApplication(), android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
        }
}
