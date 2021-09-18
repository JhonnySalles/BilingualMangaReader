package br.com.fenix.mangareader.service.parses

import android.text.TextUtils
import br.com.fenix.mangareader.model.entity.Book
import java.io.File
import java.util.*

class DirectoryListParse {

    private var mComics: List<Book>? = null
    private var mDirectoryDisplays: List<String>? = null
    private var mLibraryDir: File? = null

    fun DirectoryListingManager(comics: List<Book>?, libraryDir: String?) {
        Collections.sort(comics, Comparator<Any> { lhs, rhs ->
            val leftPath: String = (lhs as Book).file!!.parentFile.absolutePath
            val rightPath: String = (rhs as Book).file!!.parentFile.absolutePath
            leftPath.compareTo(rightPath)
        })
        mComics = comics
        mLibraryDir = File(libraryDir ?: "/")
        val directoryDisplays: MutableList<String> = ArrayList()
        for (comic in mComics!!) {
            val comicDir: File = comic.file!!.parentFile
            if (comicDir == mLibraryDir) {
                directoryDisplays.add("~ (" + comicDir.name + ")")
            } else if (comicDir.parentFile == mLibraryDir) {
                directoryDisplays.add(comicDir.name)
            } else {
                var intermediateDirs: List<String?> = ArrayList()
                var current: File? = comicDir
                while (current != null && current != mLibraryDir) {
                    intermediateDirs += current.name
                    current = current.parentFile
                }
                if (current == null) {
                    directoryDisplays.add(comicDir.name)
                } else {
                    directoryDisplays.add(TextUtils.join(" | ", intermediateDirs))
                }
            }
        }
        mDirectoryDisplays = directoryDisplays
    }

    fun getDirectoryDisplayAtIndex(idx: Int): String? {
        return mDirectoryDisplays!![idx]
    }

    fun getComicAtIndex(idx: Int): Book? {
        return mComics!![idx]
    }

    fun getDirectoryAtIndex(idx: Int): String? {
        return mComics!![idx].file!!.parent
    }

    fun getCount(): Int {
        return mComics!!.size
    }
}