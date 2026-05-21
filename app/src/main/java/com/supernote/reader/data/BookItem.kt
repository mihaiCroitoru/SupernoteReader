package com.supernote.reader.data

import android.graphics.Bitmap
import java.io.File

data class BookItem(
    val file: File,
    val title: String,
    val author: String = "",
    val cover: Bitmap?,
    val format: BookFormat,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val lastRead: Long = 0L,
) {
    val progress: Float
        get() = if (totalPages > 0) currentPage.toFloat() / totalPages else 0f
}
