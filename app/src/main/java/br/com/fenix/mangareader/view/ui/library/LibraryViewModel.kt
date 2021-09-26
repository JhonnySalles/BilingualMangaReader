package br.com.fenix.mangareader.view.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.mangareader.model.entity.Manga
import br.com.fenix.mangareader.model.entity.Cover
import br.com.fenix.mangareader.service.repository.MangaRepository
import br.com.fenix.mangareader.service.repository.CoverRepository

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val mContext = application.applicationContext
    private val mMangaRepository: MangaRepository = MangaRepository(mContext)
    private val mCoverRepository: CoverRepository = CoverRepository(mContext)

    private var mListMangas = MutableLiveData<ArrayList<Manga>>()
    val save: LiveData<ArrayList<Manga>> = mListMangas

    fun clear() {
        if (mListMangas.value == null)
            mListMangas.value = ArrayList()
        else
            mListMangas.value!!.clear()
    }

    fun save(obj: Manga): Manga {
        if (obj.id == 0L)
            obj.id = mMangaRepository.save(obj)
        else
            mMangaRepository.update(obj)

        return obj
    }

    fun save(obj: Cover): Cover {
        if (obj.id == 0L)
            obj.id = mCoverRepository.save(obj)
        else
            mCoverRepository.update(obj)

        return obj
    }

    fun list() {
        val list = mMangaRepository.list()
        if (list != null)
            mListMangas.value = ArrayList(list)
        else
            mListMangas.value = ArrayList()
    }

    fun updateLastAcess(manga: Manga) {
        mMangaRepository.update(manga)
    }
}