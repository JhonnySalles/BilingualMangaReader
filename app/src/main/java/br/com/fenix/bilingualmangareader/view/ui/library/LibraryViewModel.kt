package br.com.fenix.bilingualmangareader.view.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val mMangaRepository: MangaRepository = MangaRepository(application.applicationContext)

    private var mListMangas = MutableLiveData<ArrayList<Manga>>(ArrayList())
    val save: LiveData<ArrayList<Manga>> = mListMangas

    fun save(obj: Manga): Manga {
        if (obj.id == 0L)
            obj.id = mMangaRepository.save(obj)
        else
            mMangaRepository.update(obj)

        return obj
    }

    fun add(manga: Manga, position: Int = -1) {
        if (position > -1)
            mListMangas.value!!.add(position, manga)
        else
            mListMangas.value!!.add(manga)
    }

    fun delete(obj: Manga) {
        mMangaRepository.delete(obj)
        remove(obj)
    }

    fun get(position: Int): Manga? {
        return if (mListMangas.value != null) mListMangas.value!!.removeAt(position) else  null
    }

    fun remove(manga: Manga) {
        if (mListMangas.value != null)
            mListMangas.value!!.remove(manga)
    }

    fun remove(position: Int) {
        if (mListMangas.value != null)
            mListMangas.value!!.removeAt(position)
    }

    fun update() {
        if (mListMangas.value != null) {
            for (manga in mListMangas.value!!) {
                val item = mMangaRepository.get(manga.id!!)
                manga.bookMark = item?.bookMark!!
                manga.favorite = item.favorite
                manga.lastAccess = item.lastAccess
            }
        }
    }

    fun update(list: List<Manga>) {
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (!mListMangas.value!!.contains(manga))
                    mListMangas.value!!.add(manga)
            }
        }
    }

    fun updateList() : ArrayList<Int> {
        val indexes = arrayListOf<Int>()
        val list = mMangaRepository.list() ?: return indexes
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (!mListMangas.value!!.contains(manga)) {
                    mListMangas.value!!.add(manga)
                    indexes.add(mListMangas.value!!.size -1)
                }
            }
        }

        return indexes
    }

    fun list(refreshComplete: () -> (Unit)) {
        val list = mMangaRepository.list()
        if (list != null) {
            if (mListMangas.value == null || mListMangas.value!!.isEmpty())
                mListMangas.value = ArrayList(list)
            else
                update(list)
        } else
            mListMangas.value = ArrayList()
        refreshComplete()
    }

    fun isEmpty() : Boolean = mListMangas.value == null || mListMangas.value!!.isEmpty()
}