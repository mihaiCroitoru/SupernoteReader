package com.supernote.reader.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.supernote.reader.util.parseEpubMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File

class BookRepository(private val progressStore: ProgressStore) {

    // Initialised once; mkdirs() runs a single time per repository instance.
    val booksDir: File = File("/storage/emulated/0/Document/Books").also { it.mkdirs() }

    suspend fun scanBooks(): List<BookItem> = withContext(Dispatchers.IO) {
        val files = booksDir.listFiles { f ->
            f.isFile && f.extension.lowercase() in listOf("epub", "pdf")
        } ?: return@withContext emptyList()

        // One DataStore read for all books instead of one per book.
        val prefs = progressStore.loadAllPrefs()

        coroutineScope {
            files.map { file ->
                async {
                    val format = if (file.extension.lowercase() == "epub") BookFormat.EPUB else BookFormat.PDF
                    val (title, author, cover) = when (format) {
                        BookFormat.EPUB -> extractEpubMeta(file)
                        BookFormat.PDF  -> Triple(file.nameWithoutExtension, "", extractPdfCover(file))
                    }
                    val (page, total) = progressStore.getProgress(prefs, file.absolutePath)
                    val lastRead = progressStore.getLastRead(prefs, file.absolutePath)
                    BookItem(file, title, author, cover, format, page, total, lastRead)
                }
            }.map { it.await() }
        }.sortedBy { it.title.lowercase() }
    }

    // Cache stores Triple(title, author, cover?) for both EPUB and PDF.
    private val metaCache = HashMap<String, Triple<String, String, Bitmap?>>()

    private fun extractEpubMeta(file: File): Triple<String, String, Bitmap?> =
        metaCache.getOrPut(file.absolutePath) {
            try {
                val meta = parseEpubMeta(file)
                Triple(meta.title, meta.author, meta.cover?.toGrayscale())
            } catch (_: Exception) {
                Triple(file.nameWithoutExtension, "", null)
            }
        }

    private fun extractPdfCover(file: File): Bitmap? =
        (metaCache[file.absolutePath]?.third) ?: run {
            try {
                val cover = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        renderer.openPage(0).use { page ->
                            val scale = 300f / page.width.coerceAtLeast(1)
                            val w = (page.width * scale).toInt().coerceAtLeast(1)
                            val h = (page.height * scale).toInt().coerceAtLeast(1)
                            Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bmp ->
                                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            }.toGrayscale()
                        }
                    }
                }
                metaCache[file.absolutePath] = Triple(file.nameWithoutExtension, "", cover)
                cover
            } catch (_: Exception) { null }
        }
}

fun Bitmap.toGrayscale(): Bitmap {
    val src = if (config == Bitmap.Config.HARDWARE) copy(Bitmap.Config.ARGB_8888, false) else this
    val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    Canvas(result).drawBitmap(src, 0f, 0f, Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().also { it.setSaturation(0f) })
    })
    if (src !== this) src.recycle()
    return result
}
