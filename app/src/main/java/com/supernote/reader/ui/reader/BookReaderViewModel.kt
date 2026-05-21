package com.supernote.reader.ui.reader

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.supernote.reader.data.BookFormat
import com.supernote.reader.data.ProgressStore
import com.supernote.reader.util.EpubPage
import com.supernote.reader.util.TextBlock
import com.supernote.reader.util.getPdfPageCount
import com.supernote.reader.util.loadEpub
import com.supernote.reader.util.paginateWithStarts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class TocEntry(val title: String, val pageIndex: Int)

sealed class ReaderState {
    object Loading : ReaderState()
    data class EpubReady(
        val pages: List<EpubPage>,
        val currentPage: Int,
        val toc: List<TocEntry>,
    ) : ReaderState()
    data class PdfReady(val totalPages: Int, val currentPage: Int) : ReaderState()
    data class Error(val message: String) : ReaderState()
}

class BookReaderViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(app) {

    private val bookPath: String = checkNotNull(savedStateHandle["bookPath"])
    private val progressStore = ProgressStore(app)

    private val file = File(bookPath)
    private val format = if (file.extension.lowercase() == "epub") BookFormat.EPUB else BookFormat.PDF

    private val _state = MutableStateFlow<ReaderState>(ReaderState.Loading)
    val state: StateFlow<ReaderState> = _state

    private val _title = MutableStateFlow(file.nameWithoutExtension)
    val title: StateFlow<String> = _title

    val fontName: StateFlow<String> = progressStore.fontFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Literata")

    val fontSize: StateFlow<Int> = progressStore.sizeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 16)

    val isDark: StateFlow<Boolean> = progressStore.themeFlow(bookPath)
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _refreshFlash = MutableStateFlow(false)
    val refreshFlash: StateFlow<Boolean> = _refreshFlash
    private var turnsSinceRefresh = 0

    // Retained for repagination on font-size changes.
    private var epubSpineBlocks: List<List<TextBlock>> = emptyList()
    private var epubSpineHrefs:  List<String> = emptyList()
    private var epubCover: Bitmap? = null
    private var epubRawToc: List<com.supernote.reader.util.RawTocEntry> = emptyList()

    init {
        load()
        viewModelScope.launch {
            var prevSize: Int? = null
            fontSize.collect { size ->
                if (prevSize != null && prevSize != size) repaginate()
                prevSize = size
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val (savedPage, savedTotal) = progressStore.loadProgress(bookPath)
            _state.value = try {
                when (format) {
                    BookFormat.EPUB -> {
                        val content = withContext(Dispatchers.IO) { loadEpub(file) }
                        _title.value = content.title.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension
                        epubSpineBlocks = content.spineBlocks
                        epubSpineHrefs  = content.spineHrefs
                        epubCover       = content.cover
                        epubRawToc      = content.rawToc
                        val state = buildEpubState(fontSize.value)
                        val total = state.pages.size
                        if (savedTotal != total) progressStore.saveProgress(bookPath, savedPage.coerceAtMost(total - 1), total)
                        state.copy(currentPage = savedPage.coerceIn(0, total - 1))
                    }
                    BookFormat.PDF -> {
                        val total = withContext(Dispatchers.IO) { getPdfPageCount(file) }
                        if (savedTotal != total) progressStore.saveProgress(bookPath, savedPage.coerceAtMost(total - 1), total)
                        ReaderState.PdfReady(total, savedPage.coerceIn(0, (total - 1).coerceAtLeast(0)))
                    }
                }
            } catch (e: Exception) {
                ReaderState.Error(e.message ?: "Failed to open book")
            }
        }
    }

    private fun repaginate() {
        val current = _state.value as? ReaderState.EpubReady ?: return
        val ratio = current.currentPage.toFloat() / current.pages.size.coerceAtLeast(1)
        val newState = buildEpubState(fontSize.value)
        val newPage = (ratio * newState.pages.size).toInt().coerceIn(0, newState.pages.size - 1)
        _state.value = newState.copy(currentPage = newPage)
        viewModelScope.launch { progressStore.saveProgress(bookPath, newPage, newState.pages.size) }
    }

    private fun buildEpubState(fontSizeValue: Int): ReaderState.EpubReady {
        val charsPerPage = (1800 * 16f / fontSizeValue.toFloat()).toInt().coerceIn(400, 3200)
        val (contentPages, spineStarts) = paginateWithStarts(epubSpineBlocks, charsPerPage)
        val coverOffset = if (epubCover != null) 1 else 0
        val pages = if (epubCover != null) listOf(EpubPage(emptyList(), epubCover)) + contentPages else contentPages

        val toc = epubRawToc.mapNotNull { raw ->
            val tocFile = raw.href.substringBefore('#').substringAfterLast('/')
            val spineIdx = epubSpineHrefs.indexOfFirst { it.substringAfterLast('/') == tocFile }
            if (spineIdx < 0) return@mapNotNull null
            val pageIdx = spineStarts.getOrElse(spineIdx) { 0 } + coverOffset
            TocEntry(raw.title, pageIdx.coerceIn(0, pages.size - 1))
        }.distinctBy { it.pageIndex } // collapse duplicate page-index entries

        return ReaderState.EpubReady(pages, 0, toc)
    }

    fun nextPage() = changePage(+1)
    fun prevPage() = changePage(-1)

    fun jumpToPage(page: Int) {
        val current = _state.value
        val (newPage, total) = when (current) {
            is ReaderState.EpubReady -> page.coerceIn(0, current.pages.size - 1) to current.pages.size
            is ReaderState.PdfReady  -> page.coerceIn(0, current.totalPages - 1) to current.totalPages
            else -> return
        }
        _state.value = when (current) {
            is ReaderState.EpubReady -> current.copy(currentPage = newPage)
            is ReaderState.PdfReady  -> current.copy(currentPage = newPage)
            else -> return
        }
        viewModelScope.launch { progressStore.saveProgress(bookPath, newPage, total) }
    }

    private fun changePage(delta: Int) {
        val current = _state.value
        val (newPage, total) = when (current) {
            is ReaderState.EpubReady -> (current.currentPage + delta).coerceIn(0, current.pages.size - 1) to current.pages.size
            is ReaderState.PdfReady  -> (current.currentPage + delta).coerceIn(0, current.totalPages - 1) to current.totalPages
            else -> return
        }

        _state.value = when (current) {
            is ReaderState.EpubReady -> current.copy(currentPage = newPage)
            is ReaderState.PdfReady  -> current.copy(currentPage = newPage)
            else -> return
        }

        viewModelScope.launch {
            progressStore.saveProgress(bookPath, newPage, total)
            progressStore.saveLastRead(bookPath)
        }

        turnsSinceRefresh++
        if (turnsSinceRefresh >= 8) {
            turnsSinceRefresh = 0
            viewModelScope.launch {
                _refreshFlash.value = true
                kotlinx.coroutines.delay(16)
                _refreshFlash.value = false
            }
        }
    }

    fun setFont(name: String) = viewModelScope.launch { progressStore.saveFont(name) }
    fun setFontSize(size: Int) = viewModelScope.launch { progressStore.saveSize(size.coerceIn(14, 28)) }
    fun setTheme(dark: Boolean) = viewModelScope.launch { progressStore.saveTheme(bookPath, dark) }
}
