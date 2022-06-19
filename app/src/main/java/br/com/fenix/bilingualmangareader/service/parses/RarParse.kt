package br.com.fenix.bilingualmangareader.service.parses

import br.com.fenix.bilingualmangareader.util.helpers.Util
import com.github.junrar.Archive
import com.github.junrar.exception.RarException
import com.github.junrar.rarfile.FileHeader
import java.io.*

class RarParse : Parse {

    private val mHeaders = ArrayList<FileHeader>()
    private var mArchive: Archive? = null
    private var mCacheDir: File? = null
    private var mSolidFileExtracted = false
    private var mSubtitles = ArrayList<FileHeader>()

    override fun parse(file: File?) {
        mArchive = try {
            Archive(file)
        } catch (e: RarException) {
            throw IOException("Unable to open archive")
        }
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
        mHeaders.sortBy { getName(it) }
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

    override fun getPagePath(num: Int): String? {
        if (mHeaders.size < num)
            return null
        return getName(mHeaders[num])
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

    override fun destroy() {
        if (mCacheDir != null) {
            for (f in mCacheDir?.listFiles()!!)
                f.delete()

            mCacheDir!!.delete()
        }
        mArchive!!.close()
        mArchive = null
    }

    override fun getType(): String {
        return "rar"
    }

    fun setCacheDirectory(cacheDirectory: File?) {
        mCacheDir = cacheDirectory
        if (!mCacheDir!!.exists())
            mCacheDir!!.mkdirs()

        if (mCacheDir!!.listFiles() != null) {
            for (f in mCacheDir?.listFiles()!!)
                f.delete()
        }
    }
}