package br.com.fenix.mangareader.parses

import br.com.fenix.mangareader.common.Util
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ZipParse : Parse {

    private var mZipFile: ZipFile? = null
    private var mEntries: ArrayList<ZipEntry>? = null

    override fun parse(file: File?) {
        mZipFile = ZipFile(file?.absolutePath)
        mEntries = ArrayList()
        val e = mZipFile!!.entries()
        while (e.hasMoreElements()) {
            val ze = e.nextElement()
            if (!ze.isDirectory && Util.isImage(ze.name)) {
                mEntries!!.add(ze)
            }
        }
        mEntries!!.sortBy { it.name };
    }

    override fun numPages(): Int {
        return mEntries!!.size
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