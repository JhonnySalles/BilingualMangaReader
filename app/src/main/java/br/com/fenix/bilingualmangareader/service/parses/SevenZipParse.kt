package br.com.fenix.bilingualmangareader.service.parses

import br.com.fenix.bilingualmangareader.util.helpers.Util
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.ArrayList
import java.util.zip.ZipEntry

class SevenZipParse : Parse {

    private var mEntries: ArrayList<SevenZEntry>? = null

    private class SevenZEntry(val entry: SevenZArchiveEntry, val bytes: ByteArray)

    override fun parse(file: File?) {
        mEntries = ArrayList()
        val sevenZFile = SevenZFile(file)
        var entry = sevenZFile.nextEntry
        while (entry != null) {
            if (entry.isDirectory)
                continue

            if (Util.isImage(entry.name)) {
                val content = ByteArray(entry.size.toInt())
                sevenZFile.read(content)
                mEntries!!.add(SevenZEntry(entry, content))
            }
            entry = sevenZFile.nextEntry
        }
        mEntries!!.sortBy { (it as ZipEntry).name }
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