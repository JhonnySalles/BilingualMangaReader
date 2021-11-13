package br.com.fenix.bilingualmangareader.service.parses

import br.com.fenix.bilingualmangareader.util.helpers.Util
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.*
import java.util.*

class TarParse : Parse {
    private var mEntries: ArrayList<TarEntry>? = null

    private class TarEntry(val entry: TarArchiveEntry, val bytes: ByteArray)

    override fun parse(file: File?) {
        mEntries = ArrayList()
        val fis = BufferedInputStream(FileInputStream(file))
        val `is` = TarArchiveInputStream(fis)
        var entry = `is`.nextTarEntry
        while (entry != null) {
            if (entry.isDirectory) {
                continue
            }
            if (Util.isImage(entry.name)) {
                mEntries!!.add(TarEntry(entry, Util.toByteArray(`is`)!!))
            }
            entry = `is`.nextTarEntry
        }
        mEntries!!.sortBy { it.entry.name }
    }

    override fun numPages(): Int {
        return mEntries!!.size
    }

    override fun getSubtitles(): List<String> {
        TODO("Not yet implemented")
    }

    override fun getPagePath(num: Int): String? {
        TODO("Not yet implemented")
    }

    override fun getPage(num: Int): InputStream {
        return ByteArrayInputStream(mEntries!![num].bytes)
    }

    override fun getType(): String {
        return "tar"
    }

    override fun destroy() {
    }
}