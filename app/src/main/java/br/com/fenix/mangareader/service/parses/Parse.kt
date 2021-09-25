package br.com.fenix.mangareader.service.parses

import java.io.File
import java.io.InputStream

interface Parse {
    fun parse(file: File?)
    fun destroy()
    fun getType(): String?
    fun getPage(num: Int): InputStream?
    fun numPages(): Int
    fun getSubtitles(): List<String>
    fun getPageName(num: Int) : String?
    fun getPagePath(num: Int) : String?
}