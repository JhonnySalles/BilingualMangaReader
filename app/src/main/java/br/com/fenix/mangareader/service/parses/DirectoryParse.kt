package br.com.fenix.mangareader.service.parses

import br.com.fenix.mangareader.util.helpers.Util
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList

class DirectoryParse : Parse {

    private val mFiles = ArrayList<File>()

    override fun parse(file: File?) {
        if (!file!!.isDirectory) {
            throw IOException("Not a directory: " + file.absolutePath)
        }
        val files = file.listFiles()
        if (files != null) {
            for (f in file.listFiles()) {
                if (f.isDirectory)
                    throw IOException("Probably not a comic directory")

                if (Util.isImage(f.absolutePath))
                    mFiles.add(f)
            }
        }
        mFiles.sortBy { it.name }
    }

    override fun numPages(): Int {
        return mFiles.size
    }

    override fun getSubtitles(): List<String> {
        TODO("Not yet implemented")
    }

    override fun getPagePath(num: Int): String? {
        TODO("Not yet implemented")
    }

    override fun getPage(num: Int): InputStream? {
        return FileInputStream(mFiles[num])
    }

    override fun getType(): String? {
        return "dir"
    }

    override fun destroy() {
    }
}