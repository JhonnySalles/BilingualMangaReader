package br.com.fenix.bilingualmangareader.service.parses

import java.io.File
import java.io.InputStream

interface Parse {
    fun parse(file: File?)
    fun destroy(isClearCache: Boolean = true)
    fun getType(): String
    fun getPage(num: Int): InputStream
    fun numPages(): Int
    fun getSubtitles(): List<String>
    fun hasSubtitles(): Boolean
    fun getSubtitlesNames(): Map<String, Int>
    fun getPagePath(num: Int): String?
    fun getPagePaths(): Map<String, Int>
}