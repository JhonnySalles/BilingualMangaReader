package br.com.fenix.mangareader.view.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.mangareader.model.entity.Book
import br.com.fenix.mangareader.model.entity.Cover
import br.com.fenix.mangareader.service.repository.BookRepository
import br.com.fenix.mangareader.service.repository.CoverRepository
import br.com.fenix.mangareader.service.repository.Storage
import java.io.File

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private val mBookRepository: BookRepository = BookRepository(mContext)
    private val mCoverRepository: CoverRepository = CoverRepository(mContext)

    private var mListBooks = MutableLiveData<ArrayList<Book>>()
    val save: LiveData<ArrayList<Book>> = mListBooks

    fun clear() {
        if (mListBooks.value == null)
            mListBooks.value = ArrayList()
        else
            mListBooks.value!!.clear()
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

    fun list(withCover:Boolean) {
        val list = mBookRepository.list(withCover)
        if (list != null)
            mListBooks.value = ArrayList(list)
        else
            mListBooks.value = ArrayList()
    }

    fun updateLastAcess(book: Book) {
        mBookRepository.update(book)
    }
}