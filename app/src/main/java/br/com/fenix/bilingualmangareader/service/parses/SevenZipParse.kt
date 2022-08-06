package br.com.fenix.bilingualmangareader.service.parses

import br.com.fenix.bilingualmangareader.util.helpers.Util
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry

class SevenZipParse : Parse {

    private var mEntries = ArrayList<SevenZEntry>()
    private var mSubtitles =  ArrayList<SevenZEntry>()

    private class SevenZEntry(val entry: SevenZArchiveEntry, val bytes: ByteArray)

    override fun parse(file: File?) {
        mEntries.clear()
        val sevenZFile = SevenZFile(file)
        var entry = sevenZFile.nextEntry
        while (entry != null) {
            if (entry.isDirectory)
                continue

            if (Util.isImage(entry.name)) {
                val content = ByteArray(entry.size.toInt())
                sevenZFile.read(content)
                mEntries.add(SevenZEntry(entry, content))
            } else if (Util.isJson(entry.name)) {
                val content = ByteArray(entry.size.toInt())
                sevenZFile.read(content)
                mSubtitles.add(SevenZEntry(entry, content))
            }

            entry = sevenZFile.nextEntry
        }
        mEntries.sortBy { (it as ZipEntry).name }
    }

    override fun numPages(): Int {
        return mEntries.size
    }

    override fun getSubtitles(): List<String> {
        val subtitles = arrayListOf<String>()
        mSubtitles.forEach {
            val sub = ByteArrayInputStream(it.bytes)

            val reader = BufferedReader(sub.reader())
            val content = StringBuilder()
            reader.use { rd ->
                var line = rd.readLine()
                while (line != null) {
                    content.append(line)
                    line = rd.readLine()
                }
            }
            subtitles.add(content.toString())
        }
        return subtitles
    }

    override fun getSubtitlesNames(): Map<String, Int> {
        val paths = mutableMapOf<String, Int>()

        for((index, entry) in mSubtitles.withIndex()) {
            val path = Util.getNameFromPath(getName(entry as ZipEntry))
            if (path.isNotEmpty() && !paths.containsKey(path))
                paths[path] = index
        }

        return paths
    }

    private fun getName(entry: ZipEntry): String {
        return entry.name
    }

    override fun getPagePath(num: Int): String? {
        if (mEntries.size < num)
            return null
        return getName((mEntries[num] as ZipEntry))
    }

    override fun getPagePaths(): Map<String, Int> {
        val paths = mutableMapOf<String, Int>()

        for((index, entry) in mEntries.withIndex()) {
            val path = Util.getFolderFromPath(getName(entry as ZipEntry))
            if (path.isNotEmpty() && !paths.containsKey(path))
                paths[path] = index
        }

        return paths
    }

    override fun getPage(num: Int): InputStream {
        return ByteArrayInputStream(mEntries[num].bytes)
    }

    override fun getType(): String {
        return "tar"
    }


    override fun destroy(isClearCache: Boolean) {
    }
}