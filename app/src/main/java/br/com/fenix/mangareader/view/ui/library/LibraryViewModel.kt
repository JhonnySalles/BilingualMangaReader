package br.com.fenix.mangareader.view.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.model.entity.Cover
import br.com.fenix.mangareader.service.repository.BookRepository
import br.com.fenix.mangareader.service.repository.CoverRepository
import java.io.File

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private val mBookRepository: BookRepository = BookRepository(mContext)
    private val mCoverRepository: CoverRepository = CoverRepository(mContext)

    private var mListBooks = MutableLiveData<ArrayList<Book>>()
    val save: LiveData<ArrayList<Book>> = mListBooks

    fun readFiles(libraryPath: String) {
        if (libraryPath.isEmpty())
            return

        mListBooks.value = ArrayList()
        var file = File(libraryPath)
        file.walk()
            .filterNot { it.isDirectory }.forEach {
                if (it.name.endsWith(".rar") ||
                    it.name.endsWith(".zip") ||
                    it.name.endsWith(".cbr") ||
                    it.name.endsWith(".cbz")
                ) {
                    var book: Book? = mBookRepository.findByFileName(it.name)
                    if (book == null) {
                        book = save(
                            Book(
                                null,
                                it.nameWithoutExtension,
                                "",
                                it.path,
                                it.name,
                                it.extension,
                                1
                            )
                        )
                        book!!.file = it
                    }
                    save.value?.add(book)
                }
            }
    }

    fun save(obj: Book): Book {
        if (obj.id == 0L)
            obj.id = mBookRepository.save(obj)
        else
            mBookRepository.update(obj)

        return obj
    }

    fun save(obj: Cover): Cover {
        if (obj.id == 0L)
            obj.id = mCoverRepository.save(obj)
        else
            mCoverRepository.update(obj)

        return obj
    }

}