package com.supernote.reader.util

import android.graphics.Bitmap
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

data class EpubPage(val blocks: List<TextBlock>, val coverBitmap: Bitmap? = null)

sealed class TextBlock {
    data class Heading(val text: String) : TextBlock()
    data class Paragraph(val text: String) : TextBlock()
}

internal fun parseHtmlBlocks(html: String): List<TextBlock> {
    val doc = Jsoup.parse(html)
    val blocks = mutableListOf<TextBlock>()

    fun walk(el: Element) {
        when (el.tagName().lowercase()) {
            "h1", "h2", "h3", "h4" -> {
                val text = el.text().trim()
                if (text.isNotEmpty()) blocks += TextBlock.Heading(text)
            }
            "p", "div", "li" -> {
                val text = el.ownText().trim()
                if (text.isNotEmpty()) blocks += TextBlock.Paragraph(text)
                el.children().forEach { walk(it) }
            }
            else -> el.children().forEach { walk(it) }
        }
    }

    doc.body()?.let { walk(it) }
    return blocks
}

// Paginate per-spine-item block lists.
// Returns (pages, spineStartPages) where spineStartPages[i] is the page index
// at which spine item i's first block appears.
internal fun paginateWithStarts(
    spineBlocks: List<List<TextBlock>>,
    charsPerPage: Int = 1800,
): Pair<List<EpubPage>, List<Int>> {
    val pages = mutableListOf<EpubPage>()
    val spineStarts = mutableListOf<Int>()
    val current = mutableListOf<TextBlock>()
    var charCount = 0

    for (blocks in spineBlocks) {
        spineStarts += pages.size
        for (block in blocks) {
            val len = when (block) {
                is TextBlock.Heading   -> block.text.length + 40
                is TextBlock.Paragraph -> block.text.length
            }
            if (charCount + len > charsPerPage && current.isNotEmpty()) {
                pages += EpubPage(current.toList())
                current.clear()
                charCount = 0
            }
            current += block
            charCount += len
        }
    }

    if (current.isNotEmpty()) pages += EpubPage(current.toList())
    val finalPages = pages.ifEmpty { listOf(EpubPage(listOf(TextBlock.Paragraph("(empty book)")))) }
    return finalPages to spineStarts
}

// Convenience wrapper for single flat block list.
internal fun splitIntoPages(blocks: List<TextBlock>, charsPerPage: Int = 1800): List<EpubPage> =
    paginateWithStarts(listOf(blocks), charsPerPage).first
