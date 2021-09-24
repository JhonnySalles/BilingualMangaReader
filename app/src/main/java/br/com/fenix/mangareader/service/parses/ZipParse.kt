package br.com.fenix.mangareader.service.parses

import br.com.fenix.mangareader.util.helpers.Util
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipParse : Parse {

    private var mZipFile: ZipFile? = null
    private var mEntries: ArrayList<ZipEntry>? = null
    private var mSubtitles: ArrayList<ZipEntry>? = null

    override fun parse(file: File?) {
        mZipFile = ZipFile(file?.absolutePath)
        mEntries = ArrayList()
        mSubtitles = ArrayList()
        val e = mZipFile!!.entries()
        while (e.hasMoreElements()) {
            val ze = e.nextElement()
            if (!ze.isDirectory && Util.isImage(ze.name))
                mEntries!!.add(ze)
            else if (!ze.isDirectory && Util.isJson(ze.name))
                mSubtitles!!.add(ze)
        }
        mEntries!!.sortBy { it.name }
    }

    override fun numPages(): Int {
        return mEntries!!.size
    }

    override fun getSubtitles(): List<String> {
        val subtitles = arrayListOf<String>()
        mSubtitles!!.forEach {
            val sub = mZipFile!!.getInputStream(it)

            val reader = BufferedReader(sub.reader())
            val content = StringBuilder()
            reader.use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    content.append(line)
                    line = reader.readLine()
                }
            }
            subtitles.add(content.toString())
        }
        return subtitles
    }

    override fun getPage(num: Int): InputStream? {
        return mZipFile!!.getInputStream(mEntries!![num])
    }

    override fun getType(): String? {
        return "zip"
    }

    override fun destroy() {
        mZipFile!!.close()
    }
}