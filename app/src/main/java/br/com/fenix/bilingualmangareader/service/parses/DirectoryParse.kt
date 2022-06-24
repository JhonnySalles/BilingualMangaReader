package br.com.fenix.bilingualmangareader.service.parses

import br.com.fenix.bilingualmangareader.util.helpers.Util
import java.io.*

class DirectoryParse : Parse {

    private val mFiles = ArrayList<File>()
    private val mSubtitles = ArrayList<File>()

    override fun parse(file: File?) {
        if (file == null)
            throw IOException("Not file informed.")

        if (!file.isDirectory)
            throw IOException("Not a directory: " + file.absolutePath)

        if (file.listFiles() != null) {
            for (f in file.listFiles()) {
                if (f.isDirectory)
                    throw IOException("Probably not a comic directory")

                if (Util.isImage(f.absolutePath))
                    mFiles.add(f)
                else if (Util.isJson(f.absolutePath))
                    mSubtitles.add(f)
            }
        }
        mFiles.sortBy { it.name }
    }

    override fun numPages(): Int {
        return mFiles.size
    }

    override fun getSubtitles(): List<String> {
        val subtitles = arrayListOf<String>()
        mSubtitles.forEach {
            val sub = FileInputStream(it)

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

    private fun getName(file: File): String {
        return file.name
    }

    override fun getPagePath(num: Int): String? {
        if (mFiles.size < num)
            return null
        return getName(mFiles[num])
    }

    override fun getPagePaths(): Map<String, Int> {
        val paths = mutableMapOf<String, Int>()

        for((index, header) in mFiles.withIndex()) {
            val path = Util.getFolderFromPath(getName(header))
            if (path.isNotEmpty() && !paths.containsKey(path))
                paths[path] = index
        }

        return paths
    }

    override fun getPage(num: Int): InputStream {
        return FileInputStream(mFiles[num])
    }

    override fun getType(): String {
        return "dir"
    }

    override fun destroy() {
    }
}