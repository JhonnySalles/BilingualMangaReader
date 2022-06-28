package br.com.fenix.bilingualmangareader.view.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val mMangaRepository: MangaRepository = MangaRepository(application.applicationContext)

    private var mListMangas = MutableLiveData<MutableList<Manga>>(mutableListOf())
    val listMangas: LiveData<MutableList<Manga>> = mListMangas

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

    fun getAndRemove(position: Int): Manga? {
        return if (mListMangas.value != null) mListMangas.value!!.removeAt(position) else null
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
                if (item != null) {
                    manga.bookMark = item.bookMark
                    manga.favorite = item.favorite
                    manga.lastAccess = item.lastAccess
                }
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

    fun updateListAdd(): ArrayList<Int> {
        val indexes = arrayListOf<Int>()
        val list = mMangaRepository.list() ?: return indexes
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (!mListMangas.value!!.contains(manga)) {
                    mListMangas.value!!.add(manga)
                    indexes.add(mListMangas.value!!.size - 1)
                }
            }
        }

        return indexes
    }

    fun updateListRem(): ArrayList<Int> {
        val indexes = arrayListOf<Int>()
        val list = mMangaRepository.listDeleted() ?: return indexes
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (mListMangas.value!!.contains(manga)) {
                    indexes.add(mListMangas.value!!.indexOf(manga))
                    mListMangas.value!!.remove(manga)
                }
            }
        }

        return indexes
    }

    fun setList(list: ArrayList<Manga>) {
        mListMangas.value = list
    }

    fun addList(manga: Manga): Int {
        var index = -1
        if (!mListMangas.value!!.contains(manga)) {
            index = mListMangas.value!!.size
            mListMangas.value!!.add(manga)
        }

        return index
    }

    fun remList(manga: Manga): Int {
        var index = -1

        if (mListMangas.value!!.contains(manga)) {
            index = mListMangas.value!!.indexOf(manga)
            mListMangas.value!!.remove(manga)
        }

        return index
    }

    fun list(refreshComplete: (Boolean) -> (Unit)) {
        val list = mMangaRepository.list()
        if (list != null) {
            if (mListMangas.value == null || mListMangas.value!!.isEmpty())
                mListMangas.value = list.toMutableList()
            else
                update(list)
        } else
            mListMangas.value = mutableListOf()

        refreshComplete(mListMangas.value!!.isNotEmpty())
    }

    fun isEmpty(): Boolean =
        mListMangas.value == null || mListMangas.value!!.isEmpty()

    fun getLastIndex(): Int =
        if (mListMangas.value == null) 0 else mListMangas.value!!.size - 1

    fun sorted(list: MutableList<Manga>) {
        mListMangas.value = list
    }
}