package br.com.fenix.mangareader.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.mangareader.model.Book
import br.com.fenix.mangareader.model.Cover
import br.com.fenix.mangareader.repository.BookRepository
import br.com.fenix.mangareader.repository.CoverRepository
import java.io.File

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private val mBookRepository: BookRepository = BookRepository(mContext)
    private val mCoverRepository: CoverRepository = CoverRepository(mContext)

    private var mListBooks = MutableLiveData<ArrayList<Book>>()
    val save: LiveData<ArrayList<Book>> = mListBooks

    fun readFiles(libraryPath : String) {
        if (libraryPath.isEmpty())
            return

        mListBooks.value = ArrayList()
        var file = File(libraryPath)
        file.walk()
            .filterNot { it.isDirectory() }.forEach {
                if (it.name.endsWith(".rar") ||
                    it.name.endsWith(".zip") ||
                    it.name.endsWith(".cbr") ||
                    it.name.endsWith(".cbz")
                ){
                    var book : Book? = mBookRepository.findByFileName(it.name)

                    if (book == null){
                        book = Book(0, file.nameWithoutExtension, "", file, file.extension)
                        book.id = mBookRepository.save(book)
                    }
                    mListBooks.value?.add(book)
                }
            }
    }

    fun save(obj: Book) {
        if (obj.id == 0L)
            mBookRepository.save(obj)
        else
            mBookRepository.update(obj)
    }

    fun save(obj: Cover) {
        if (obj.id == 0L)
            mCoverRepository.save(obj)
        else
            mCoverRepository.update(obj)
    }


}