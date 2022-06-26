package br.com.fenix.bilingualmangareader.view.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import br.com.fenix.bilingualmangareader.model.entity.Manga
import br.com.fenix.bilingualmangareader.service.repository.MangaRepository

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val mMangaRepository: MangaRepository = MangaRepository(application.applicationContext)

    private var mListMangas = MutableLiveData<ArrayList<Manga>>(ArrayList())
    val listMangas: LiveData<ArrayList<Manga>> = mListMangas

    fun list() {
        val list = mMangaRepository.listHistory()
        if (list != null)
            mListMangas.value = ArrayList(list)
        else
            mListMangas.value = ArrayList()
    }

    fun list(refreshComplete: (Int) -> (Unit)) {
        val list = mMangaRepository.listHistory()
        if (list != null) {
            if (mListMangas.value == null || mListMangas.value!!.isEmpty())
                mListMangas.value = ArrayList(list)
            else
                update(list)
        } else
            mListMangas.value = ArrayList()

        refreshComplete(mListMangas.value!!.size - 1)
    }

    fun update(list: List<Manga>) {
        if (list.isNotEmpty()) {
            for (manga in list) {
                if (!mListMangas.value!!.contains(manga))
                    mListMangas.value!!.add(manga)
            }
        }
    }

    fun updateLastAccess(manga: Manga) {
        mMangaRepository.update(manga)
    }

    fun clear(manga: Manga?) {
        if (manga != null) {
            save(manga)
            if (mListMangas.value!!.contains(manga))
                mListMangas.value!!.remove(manga)
        }
    }

    fun deletePermanent(manga: Manga?) {
        if (manga != null)
            mMangaRepository.deletePermanent(manga)
    }

    fun save(manga: Manga?) {
        manga ?: return

        if (manga.id == 0L)
            manga.id = mMangaRepository.save(manga)
        else
            mMangaRepository.update(manga)
    }

    fun remove(manga: Manga) {
        if (mListMangas.value != null && mListMangas.value!!.contains(manga))
            mListMangas.value!!.remove(manga)
    }

    fun add(manga: Manga, index: Int) {
        if (mListMangas.value != null)
            mListMangas.value!!.add(index, manga)
    }

    fun getAndRemove(position: Int): Manga? {
        return if (mListMangas.value != null) mListMangas.value!!.removeAt(position) else null
    }

}