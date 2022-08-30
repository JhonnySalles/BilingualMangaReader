package br.com.fenix.bilingualmangareader.service.parses

import br.com.fenix.bilingualmangareader.util.helpers.Util
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.*

class TarParse : Parse {

    private var mEntries = ArrayList<TarEntry>()
    private var mSubtitles =  ArrayList<TarEntry>()

    private class TarEntry(val entry: TarArchiveEntry, val bytes: ByteArray)

    override fun parse(file: File?) {
        mEntries.clear()
        val fis = BufferedInputStream(FileInputStream(file))
        val `is` = TarArchiveInputStream(fis)
        var entry = `is`.nextTarEntry
        while (entry != null) {
            if (entry.isDirectory)
                continue

            if (Util.isImage(entry.name))
                mEntries.add(TarEntry(entry, Util.toByteArray(`is`)!!))
            else if (Util.isJson(entry.name))
                mSubtitles.add(TarEntry(entry, Util.toByteArray(`is`)!!))

            entry = `is`.nextTarEntry
        }

        mEntries.sortWith(compareBy<TarEntry> { Util.getFolderFromPath(it.entry.name) }.thenComparing { a, b ->
            Util.getNormalizedNameOrdering(a.entry.name).compareTo(Util.getNormalizedNameOrdering(b.entry.name))
        })
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

        for((index, entry) in mEntries.withIndex()) {
            val path = Util.getFolderFromPath(getName(entry))
            if (path.isNotEmpty() && !paths.containsKey(path))
                paths[path] = index
        }

        return paths
    }

    private fun getName(entry: TarEntry): String {
        return entry.entry.name
    }

    override fun getPagePath(num: Int): String? {
        if (mEntries.size < num)
            return null
        return getName(mEntries[num])
    }

    override fun getPagePaths(): Map<String, Int> {
        val paths = mutableMapOf<String, Int>()

        for((index, entry) in mEntries.withIndex()) {
            val path = Util.getFolderFromPath(getName(entry))
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