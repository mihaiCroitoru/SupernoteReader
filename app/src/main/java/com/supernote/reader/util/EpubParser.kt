package com.supernote.reader.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.File
import java.util.zip.ZipFile

data class EpubMeta(val title: String, val author: String, val cover: Bitmap?)

data class RawTocEntry(val title: String, val href: String) // href = full zip path, no fragment

// Full content for the reader: one ZipFile open extracts everything.
data class EpubContent(
    val title: String,
    val author: String,
    val cover: Bitmap?,
    val spineBlocks: List<List<TextBlock>>, // one list per spine item, for repagination
    val spineHrefs: List<String>,           // full zip paths, parallel with spineBlocks
    val rawToc: List<RawTocEntry>,
)

fun parseEpubMeta(file: File): EpubMeta = ZipFile(file).use { zip ->
    val opfPath = opfPath(zip) ?: return EpubMeta(file.nameWithoutExtension, "", null)
    val opfDir  = opfPath.substringBeforeLast('/', "")
    val opf     = zipText(zip, opfPath) ?: return EpubMeta(file.nameWithoutExtension, "", null)
    val doc     = Jsoup.parse(opf, "", Parser.xmlParser())

    val title  = doc.selectFirst("dc|title")?.text()
        ?: doc.selectFirst("title")?.text()
        ?: file.nameWithoutExtension
    val author = doc.selectFirst("dc|creator")?.text()
        ?: doc.selectFirst("creator")?.text()
        ?: ""

    val coverId   = doc.selectFirst("*[name=cover]")?.attr("content")
        ?: doc.selectFirst("*[properties=cover-image]")?.attr("id")
    val coverHref = coverId?.let { doc.selectFirst("*[id=$it]")?.attr("href") }
        ?: doc.selectFirst("*[media-type^=image]")?.attr("href")
    val cover = coverHref?.let {
        val path = if (opfDir.isEmpty()) it else "$opfDir/$it"
        zipBytes(zip, path)?.let { b -> BitmapFactory.decodeByteArray(b, 0, b.size) }
    }

    EpubMeta(title, author, cover)
}

fun loadEpub(file: File): EpubContent = ZipFile(file).use { zip ->
    val opfPath = opfPath(zip) ?: return EpubContent(file.nameWithoutExtension, "", null, emptyList(), emptyList(), emptyList())
    val opfDir  = opfPath.substringBeforeLast('/', "")
    val opf     = zipText(zip, opfPath) ?: return EpubContent(file.nameWithoutExtension, "", null, emptyList(), emptyList(), emptyList())
    val doc     = Jsoup.parse(opf, "", Parser.xmlParser())

    val title  = doc.selectFirst("dc|title")?.text()
        ?: doc.selectFirst("title")?.text()
        ?: file.nameWithoutExtension
    val author = doc.selectFirst("dc|creator")?.text()
        ?: doc.selectFirst("creator")?.text()
        ?: ""

    val coverId   = doc.selectFirst("*[name=cover]")?.attr("content")
        ?: doc.selectFirst("*[properties=cover-image]")?.attr("id")
    val coverHref = coverId?.let { doc.selectFirst("*[id=$it]")?.attr("href") }
        ?: doc.selectFirst("*[media-type^=image]")?.attr("href")
    val cover = coverHref?.let {
        val path = if (opfDir.isEmpty()) it else "$opfDir/$it"
        zipBytes(zip, path)?.let { b -> BitmapFactory.decodeByteArray(b, 0, b.size) }
    }

    val manifest = doc.select("manifest > *, manifest item")
        .associate { it.attr("id") to it.attr("href") }

    // Spine: keep per-item block lists and zip paths for TOC resolution + repagination
    val spineItems = doc.select("spine > *, spine itemref").mapNotNull { ref ->
        val href = manifest[ref.attr("idref")] ?: return@mapNotNull null
        val zipPath = if (opfDir.isEmpty()) href else "$opfDir/$href"
        val html = zipText(zip, zipPath) ?: return@mapNotNull null
        zipPath to parseHtmlBlocks(html)
    }
    val spineHrefs  = spineItems.map { it.first }
    val spineBlocks = spineItems.map { it.second }

    // TOC: prefer EPUB 3 NAV, fall back to EPUB 2 NCX
    val navHref = doc.selectFirst("*[properties=nav]")?.attr("href")
    val ncxId   = doc.selectFirst("spine[toc]")?.attr("toc")
    val ncxHref = ncxId?.let { manifest[it] } ?: manifest.values.firstOrNull { it.endsWith(".ncx") }

    val rawToc = when {
        navHref != null -> {
            val path = if (opfDir.isEmpty()) navHref else "$opfDir/$navHref"
            parseNavToc(zip, path, opfDir)
        }
        ncxHref != null -> {
            val path = if (opfDir.isEmpty()) ncxHref else "$opfDir/$ncxHref"
            parseNcxToc(zip, path, opfDir)
        }
        else -> emptyList()
    }

    EpubContent(title, author, cover, spineBlocks, spineHrefs, rawToc)
}

private fun parseNcxToc(zip: ZipFile, ncxPath: String, opfDir: String): List<RawTocEntry> {
    val ncx = zipText(zip, ncxPath) ?: return emptyList()
    val doc = Jsoup.parse(ncx, "", Parser.xmlParser())
    return doc.select("navPoint").sortedBy { it.attr("playOrder").toIntOrNull() ?: 0 }
        .mapNotNull { navPoint ->
            val title = navPoint.selectFirst("navLabel text")?.text()?.trim()
                ?: navPoint.selectFirst("navLabel")?.text()?.trim()
                ?: return@mapNotNull null
            val src = navPoint.selectFirst("content")?.attr("src")
                ?.substringBefore('#')?.takeIf { it.isNotEmpty() }
                ?: return@mapNotNull null
            val href = if (opfDir.isEmpty()) src else "$opfDir/$src"
            RawTocEntry(title, href)
        }
}

private fun parseNavToc(zip: ZipFile, navPath: String, opfDir: String): List<RawTocEntry> {
    val nav = zipText(zip, navPath) ?: return emptyList()
    val doc = Jsoup.parse(nav)
    val tocEl = doc.selectFirst("nav[epub|type=toc]")
        ?: doc.selectFirst("nav")
        ?: return emptyList()
    return tocEl.select("a[href]").mapNotNull { a ->
        val title = a.text().trim().takeIf { it.isNotEmpty() } ?: return@mapNotNull null
        val raw = a.attr("href").substringBefore('#').takeIf { it.isNotEmpty() }
            ?: return@mapNotNull null
        val href = if (opfDir.isEmpty()) raw else "$opfDir/$raw"
        RawTocEntry(title, href)
    }
}

private fun opfPath(zip: ZipFile): String? {
    val container = zipText(zip, "META-INF/container.xml") ?: return null
    return Jsoup.parse(container, "", Parser.xmlParser())
        .selectFirst("rootfile")?.attr("full-path")
}

private fun zipText(zip: ZipFile, path: String): String? =
    zip.getEntry(path)?.let { zip.getInputStream(it).bufferedReader().readText() }

private fun zipBytes(zip: ZipFile, path: String): ByteArray? =
    zip.getEntry(path)?.let { zip.getInputStream(it).readBytes() }
