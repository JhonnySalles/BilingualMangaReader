package br.com.fenix.bilingualmangareader.service.parses

import br.com.fenix.bilingualmangareader.util.helpers.Util
import com.github.junrar.Archive
import com.github.junrar.exception.RarException
import com.github.junrar.exception.UnsupportedRarV5Exception
import com.github.junrar.rarfile.FileHeader
import java.io.*

class RarParse : Parse {

    private val mHeaders = ArrayList<FileHeader>()
    private var mArchive: Archive? = null
    private var mCacheDir: File? = null
    private var mSolidFileExtracted = false
    private var mSubtitles = ArrayList<FileHeader>()

    override fun parse(file: File?) {
        mArchive = Archive(file)

        var header = mArchive!!.nextFileHeader()
        while (header != null) {
            if (!header.isDirectory) {
                val name = getName(header)
                if (Util.isImage(name))
                    mHeaders.add(header)
                else if (Util.isJson(name))
                    mSubtitles.add(header)
            }
            header = mArchive!!.nextFileHeader()
        }

        mHeaders.sortWith(compareBy<FileHeader> { Util.getFolderFromPath(it.fileName) }.thenComparing { a, b ->
            Util.getNormalizedNameOrdering(a.fileName).compareTo(Util.getNormalizedNameOrdering(b.fileName))
        })
    }

    private fun getName(header: FileHeader): String {
        return header.fileName
    }

    override fun numPages(): Int {
        return mHeaders.size
    }

    override fun getSubtitles(): List<String> {
        val subtitles = arrayListOf<String>()
        mSubtitles.forEach {
            val sub = mArchive!!.getInputStream(it)
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

    override fun hasSubtitles(): Boolean {
        return mSubtitles.isNotEmpty()
    }

    override fun getSubtitlesNames(): Map<String, Int> {
        val paths = mutableMapOf<String, Int>()

        for ((index, header) in mSubtitles.withIndex()) {
            val path = Util.getNameFromPath(getName(header))
            if (path.isNotEmpty() && !paths.containsKey(path))
                paths[path] = index
        }

        return paths
    }

    override fun getPagePath(num: Int): String? {
        if (mHeaders.isEmpty() || mHeaders.size < num)
            return null
        return getName(mHeaders[num])
    }

    override fun getPagePaths(): Map<String, Int> {
        val paths = mutableMapOf<String, Int>()

        for ((index, header) in mHeaders.withIndex()) {
            val path = Util.getFolderFromPath(getName(header))
            if (path.isNotEmpty() && !paths.containsKey(path))
                paths[path] = index
        }

        return paths
    }

    override fun getPage(num: Int): InputStream {
        if (mArchive!!.mainHeader.isSolid) {
            synchronized(this) {
                if (!mSolidFileExtracted) {
                    for (h in mArchive!!.fileHeaders) {
                        if (!h.isDirectory && Util.isImage(getName(h))) {
                            getPageStream(h)
                        }
                    }
                    mSolidFileExtracted = true
                }
            }
        }
        return getPageStream(mHeaders[num])
    }

    private fun getPageStream(header: FileHeader): InputStream {
        return try {
            if (mCacheDir != null) {
                val name = getName(header)
                val cacheFile = File(mCacheDir, Util.MD5(name))
                if (cacheFile.exists())
                    return FileInputStream(cacheFile)

                synchronized(this) {
                    if (!cacheFile.exists()) {
                        val os = FileOutputStream(cacheFile)
                        try {
                            mArchive!!.extractFile(header, os)
                        } catch (e: Exception) {
                            os.close()
                            cacheFile.delete()
                            throw e
                        }
                        os.close()
                    }
                }
                return FileInputStream(cacheFile)
            }
            mArchive!!.getInputStream(header)
        } catch (e: RarException) {
            throw IOException("Unable to parse rar")
        }
    }

    override fun destroy(isClearCache: Boolean) {
        if (isClearCache) {
            if (mCacheDir != null) {
                mCacheDir?.listFiles()?.let {
                    for (f in it)
                        f.delete()
                }
                mCacheDir?.delete()
            }
        }
        mHeaders.clear()
        mArchive?.close()
        mArchive = null
    }

    override fun getType(): String {
        return "rar"
    }

    fun setCacheDirectory(cacheDirectory: File?) {
        mCacheDir = cacheDirectory
        mCacheDir?.let {
            if (!it.exists())
                it.mkdirs()

            if (it.listFiles() != null) {
                for (f in it.listFiles()!!)
                    f.delete()
            }
        }
    }
}