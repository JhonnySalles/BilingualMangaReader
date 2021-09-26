package br.com.fenix.mangareader.service.parses

import br.com.fenix.mangareader.util.helpers.Util
import com.github.junrar.Archive
import com.github.junrar.exception.RarException
import com.github.junrar.rarfile.FileHeader
import java.io.*
import java.util.*

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
            throw IOException("unable to open archive")
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
        return if (header.isUnicode) header.fileNameW else header.fileNameString
    }

    override fun numPages(): Int {
        return mHeaders.size
    }

    override fun getSubtitles(): List<String> {
        val subtitles = arrayListOf<String>()
        mSubtitles!!.forEach {
            val sub = mArchive!!.getInputStream(it)
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

    override fun getPageName(num: Int): String? {
        if (mHeaders.size < num)
            return null
        return getName(mHeaders[num])
    }

    override fun getPagePath(num: Int): String? {
        if (mHeaders.size < num)
            return null
        val name = mHeaders[num].fileName
        return mHeaders[num].fileNameString
    }

    override fun getPage(num: Int): InputStream? {
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

    private fun getPageStream(header: FileHeader): InputStream? {
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
            throw IOException("unable to parse rar")
        }
    }

    override fun destroy() {
        if (mCacheDir != null) {
            for (f in mCacheDir?.listFiles()!!)
                f.delete()

            mCacheDir!!.delete()
        }
        mArchive!!.close()
    }

    override fun getType(): String? {
        return "rar"
    }

    fun setCacheDirectory(cacheDirectory: File?) {
        mCacheDir = cacheDirectory
        if (!mCacheDir!!.exists()) {
            mCacheDir!!.mkdir()
        }
        if (mCacheDir!!.listFiles() != null) {
            for (f in mCacheDir?.listFiles()!!) {
                f.delete()
            }
        }
    }
}